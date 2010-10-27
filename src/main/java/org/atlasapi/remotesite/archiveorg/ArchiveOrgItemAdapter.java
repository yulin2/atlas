package org.atlasapi.remotesite.archiveorg;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.atlasapi.genres.GenreMap;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Version;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.SimpleHttpClient;

public class ArchiveOrgItemAdapter implements SiteSpecificAdapter<Item> {
    private static final String ITEM_PREFIX = "http://www.archive.org/details/";
    private static final String JSON_OUTPUT_PARAMETER = "&output=json";

    private static final String ARCHIVE_ORG_EMBED_TEMPLATE = "<object width=\"640\" height=\"506\" classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\"><param value=\"true\" name=\"allowfullscreen\"/><param value=\"always\" name=\"allowscriptaccess\"/><param value=\"high\" name=\"quality\"/><param value=\"true\" name=\"cachebusting\"/><param value=\"#000000\" name=\"bgcolor\"/><param name=\"movie\" value=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" /><param value=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%2$s'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\" name=\"flashvars\"/><embed src=\"http://www.archive.org/flow/flowplayer.commercial-3.2.1.swf\" type=\"application/x-shockwave-flash\" width=\"640\" height=\"506\" allowfullscreen=\"true\" allowscriptaccess=\"always\" cachebusting=\"true\" bgcolor=\"#000000\" quality=\"high\" flashvars=\"config={'key':'#$aa4baff94a9bdcafce8','playlist':['format=Thumbnail?.jpg',{'autoPlay':false,'url':'%2$s'}],'clip':{'autoPlay':true,'baseUrl':'http://www.archive.org/download/%1$s/','scaling':'fit','provider':'h264streaming'},'canvas':{'backgroundColor':'#000000','backgroundGradient':'none'},'plugins':{'controls':{'playlist':false,'fullscreen':true,'height':26,'backgroundColor':'#000000','autoHide':{'fullscreenOnly':true}},'h264streaming':{'url':'http://www.archive.org/flow/flowplayer.pseudostreaming-3.2.1.swf'}},'contextMenu':[{},'-','Flowplayer v3.2.1']}\"> </embed></object>";
    private static final String ARCHIVE_ORG_DOWNLOAD_TEMPLATE = "http://www.archive.org/download/%1$s";

    private static final String GENRE_PREFIX = "http://www.archive.org/search.php?query=subject:%22";
    private static final String GENRE_SUFFIX = "%22";

    private final SimpleHttpClient client;
    private final ObjectMapper jsonMapper;
    private final AdapterLog log;
    private final GenreMap genreMap = new ArchiveOrgGenreMap();

    public ArchiveOrgItemAdapter(SimpleHttpClient client, AdapterLog log) {
        this.client = client;
        this.log = log;
        this.jsonMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Item fetch(String uri) {
        try {
            HttpResponse res = client.get(uri + JSON_OUTPUT_PARAMETER);

            if (res.wasNotFound()) {
                return null;
            }

            Map<String, Object> json = jsonMapper.readValue(res.body(), Map.class);
            Map<String, Object> metadata = (Map<String, Object>) json.get("metadata");

            String identifier = getFirstValue(metadata.get("identifier"));
            String title = getFirstValue(metadata.get("title"));
            String subjects = getFirstValue(metadata.get("subject"));

            Item item = new Item(uri, "arc:" + identifier, Publisher.ARCHIVE_ORG);
            item.setTitle(title);

            if (subjects != null) {
                Set<String> genreUris = getGenreUris(Sets.newHashSet(Splitter.on(";").trimResults().split(subjects)));
                Set<String> genres = genreMap.mapRecognised(genreUris);
                item.setGenres(genres);
            }

            Version version = new Version();

            Encoding encoding = new Encoding();
            String downloadFilename = null;

            Map<String, Object> files = (Map<String, Object>) json.get("files");
            boolean firstThumbnail = true;
            for (String fileName : files.keySet()) {
                Map<String, Object> file = (Map<String, Object>) files.get(fileName);
                String format = (String) file.get("format");
                if (format != null) {
                    if (format.contains("MPEG4")) { // || format.equals("MPEG2") || format.equals("Ogg Video")
                        downloadFilename = fileName.replace("/", "");
                        Location downloadLocation = new Location();
                        downloadLocation.setTransportType(TransportType.DOWNLOAD);
                        downloadLocation.setAvailable(true);
                        downloadLocation.setUri(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, identifier) + fileName);
                        encoding.addAvailableAt(downloadLocation);
                    } else if (format.equals("Thumbnail")) {
                        if (!firstThumbnail && item.getThumbnail() == null) {
                            item.setThumbnail(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, identifier) + fileName);
                            item.setImage(String.format(ARCHIVE_ORG_DOWNLOAD_TEMPLATE, identifier) + fileName);
                        } else {
                            firstThumbnail = false;
                        }
                    }
                }
            }

            if (downloadFilename == null) {
                downloadFilename = identifier + "_512kb.mp4";
            }
            Location embedLocation = new Location();
            embedLocation.setTransportType(TransportType.EMBED);
            embedLocation.setAvailable(true);
            embedLocation.setEmbedCode(String.format(ARCHIVE_ORG_EMBED_TEMPLATE, identifier, downloadFilename));
            encoding.addAvailableAt(embedLocation);

            Location linkLocation = new Location();
            linkLocation.setTransportType(TransportType.LINK);
            linkLocation.setAvailable(true);
            linkLocation.setUri(uri);
            encoding.addAvailableAt(linkLocation);

            version.addManifestedAs(encoding);
            item.setVersions(ImmutableSet.of(version));

            return item;
        } catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withUri(uri).withSource(ArchiveOrgItemAdapter.class));
        }

        return null;
    }

    private Set<String> getGenreUris(Set<String> subjects) {
        Set<String> genreUris = Sets.newHashSetWithExpectedSize(subjects.size());

        for (String subject : subjects) {
            genreUris.add(GENRE_PREFIX + subject.toLowerCase() + GENRE_SUFFIX);
        }

        return genreUris;
    }

    @SuppressWarnings("unchecked")
    private String getFirstValue(Object object) {
        if (object != null) {
            List<String> list = (List<String>) object;
            if (!list.isEmpty()) {
                return list.iterator().next();
            }
        }

        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        return uri.startsWith(ITEM_PREFIX);
    }

}
