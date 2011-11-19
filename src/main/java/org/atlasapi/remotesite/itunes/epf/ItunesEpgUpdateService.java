package org.atlasapi.remotesite.itunes.epf;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.remotesite.itunes.epf.ArtistTable.ArtistTableRow;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

public class ItunesEpgUpdateService extends AbstractExecutionThreadService {

    private File artists;
    private final ContentWriter writer;
    private final File artistCollection;

    
    public ItunesEpgUpdateService(File artists, File artistCollection, ContentWriter writer) {
        this.artists = artists;
        this.artistCollection = artistCollection;
        this.writer = writer;
    }


    @Override
    protected void run() throws Exception {

        ArtistTable artistTable = new ArtistTable(artists);
        
        final Map<String, Brand> extractedBrands = Maps.newHashMap();
        
        artistTable.processRows(new ArtistTableRowProcessor() {
            
            @Override
            public boolean process(ArtistTableRow row) {
                System.out.println(row);
                Brand extractedBrand = extract(row);
                writer.createOrUpdate(extractedBrand);
                extractedBrands.put(row.getArtistId(), extractedBrand);
                return isRunning();
            }

            private Brand extract(ArtistTableRow row) {
                Brand brand = new Brand("http://itunes.apple.com/artist/id"+row.getArtistId(), "itunes:a-"+row.getArtistId(), Publisher.ITUNES);
                brand.setTitle(row.getName());
                return brand;
            }
        });

        LineProcessor<SetMultimap<Brand,String>> lineProcessor = new LineProcessor<SetMultimap<Brand,String>>() {

            Builder<Brand, String> results = ImmutableSetMultimap.<Brand,String>builder();
            
            private boolean isComment(String line) {
                return line.startsWith("#");
            }

            @Override
            public SetMultimap<Brand,String> getResult() {
                return results.build();
            }

            private final char FIELD_SEPARATOR = (char) 1;
            private Splitter splitter = Splitter.on(FIELD_SEPARATOR);
            
            @Override
            public boolean processLine(String line) throws IOException {
                if(isComment(line)) {
                    return true;
                }
                
                ImmutableList<String> lineParts = ImmutableList.copyOf(splitter.split(line.trim()));
                
                String artistId = lineParts.get(1);
                Brand brand = extractedBrands.get(artistId);
                if(brand != null) {
                    results.put(brand, artistId);
                }
                return true;
            }
        };

        CharStreams.readLines(new InputSupplier<Reader>() {
            @Override
            public Reader getInput() throws IOException {
                return Files.newReader(artistCollection, Charsets.UTF_8);
            }
        }, lineProcessor);
        
        SetMultimap<Brand, String> brandCollectionIdMap = lineProcessor.getResult();
        
    }
    

}
