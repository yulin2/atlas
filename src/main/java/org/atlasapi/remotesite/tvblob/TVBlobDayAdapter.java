package org.atlasapi.remotesite.tvblob;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Playlist;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;

public class TVBlobDayAdapter implements SiteSpecificAdapter<Playlist> {

    private static final Pattern URL_PATTERN = Pattern
                    .compile("http://epgadmin.tvblob.com/api/(\\w+)/programmes/schedules/(.+)(\\.\\w*)?");

    @Override
    public boolean canFetch(String uri) {
        return URL_PATTERN.matcher(uri).matches();
    }

    @Override
    public Playlist fetch(String uri) {
        Matcher matcher = URL_PATTERN.matcher(uri);
        String channelSlug = null;
        if (matcher.matches()) {
            channelSlug = matcher.group(1);
        }

        InputStream is = null;
        ContentExtractor<InputStream, Playlist> extractor = new TVBlobDayExtractor(channelSlug);
        Playlist playlist = null;
        try {
            is = new URL(uri).openStream();
            playlist = extractor.extract(is);
        } catch (Exception e) {
            throw new FetchException("Unable to retrieve source "+uri, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        
        return playlist;
    }
}
