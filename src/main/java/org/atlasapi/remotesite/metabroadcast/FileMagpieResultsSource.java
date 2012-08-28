package org.atlasapi.remotesite.metabroadcast;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.time.Timestamp;

public class FileMagpieResultsSource implements RemoteMagpieResultsSource {

    private final File fileDir;
    private final Gson gson;
    private final Function<File, RemoteMagpieResults> TO_RESULTS = new Function<File, RemoteMagpieResults>(){
        @Override
        public RemoteMagpieResults apply(@Nullable File input) {
            try {
                FileReader fileReader = new FileReader(input);
                MagpieResults results = gson.fromJson(fileReader, MagpieResults.class);
                Timestamp modified = Timestamp.of(input.lastModified());
                return RemoteMagpieResults.retrieved(results, modified);
            } catch (Exception e) {
                return RemoteMagpieResults.missing(input.getName(), e);
            }
        }
    };

    public FileMagpieResultsSource(File fileDir) {
        this.fileDir = Preconditions.checkNotNull(fileDir);
        this.gson = new GsonBuilder()
            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
            .create();
    }
    
    @Override
    public Iterable<RemoteMagpieResults> resultsChangeSince(final Timestamp since) {
        FileFilter lastModifiedFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.lastModified() > since.millis();
            }
        };
        File[] files = fileDir.listFiles(lastModifiedFilter);
        return Iterables.transform(ImmutableList.copyOf(files), TO_RESULTS);
    }

}
