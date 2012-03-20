package org.atlasapi.remotesite.redux;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.content.Item;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;

public class DefaultReduxProgrammeAdapter implements SiteSpecificAdapter<Item> {
    
    private final ReduxClient reduxClient;
    private final ContentExtractor<FullReduxProgramme, Item> contentExtractor;

    public DefaultReduxProgrammeAdapter(ReduxClient reduxClient, ContentExtractor<FullReduxProgramme, Item> contentExtractor) {
        this.reduxClient = reduxClient;
        this.contentExtractor = contentExtractor;
    }
    
    private static final Pattern REDUX_URI_PATTERN = Pattern.compile("http://g.bbcredux.com/programme/(\\d+)");

    @Override
    public Item fetch(String uri) {
        Matcher matcher = REDUX_URI_PATTERN.matcher(uri);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Redux adapter can't fetch " + uri);
        }
        
        try {
            FullReduxProgramme reduxProgramme = reduxClient.programmeFor(matcher.group(1));
            return contentExtractor.extract(reduxProgramme);
        } catch (Exception e) {
            throw new FetchException("Couldn't fetch programme data for " + uri, e);
        }
    }

    @Override
    public boolean canFetch(String uri) {
        return REDUX_URI_PATTERN.matcher(uri).matches();
    }

}
