package org.atlasapi.remotesite.hbo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.content.Episode;
import org.atlasapi.media.content.Publisher;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.xml.SimpleXmlNavigator;

import com.metabroadcast.common.http.SimpleHttpClient;

public class HboItemAdapter implements SiteSpecificAdapter<Episode> {
    
    private final Pattern canFetchPattern = Pattern.compile("http://www.hbo.com/([a-z\\-A-Z0-9]+)/episodes/([0-9]+)/([0-9]+)\\-([a-z\\-A-Z0-9]+)/index.html");
    private final AdapterLog log;
    private final SimpleHttpClient client;
    private final HboAdapterHelper helper;
    
    public HboItemAdapter(SimpleHttpClient client, AdapterLog log, HboAdapterHelper helper) {
        this.client = client;
        this.log = log;
        this.helper = helper;
    }
    
    @Override
    public Episode fetch(String uri) {
        try {
            String contents = client.getContentsOf(helper.getXmlUri(uri));
            SimpleXmlNavigator navigator = new SimpleXmlNavigator(contents);
            
            Matcher matcher = canFetchPattern.matcher(uri);
            matcher.matches();
            String showTitleKey = matcher.group(1);
            String seasonNumber = matcher.group(2);
            String episodeNumber = matcher.group(3);
            String episodeTitleKey = matcher.group(4);
            
            Episode episode = new Episode(uri, getCurie(showTitleKey, seasonNumber, episodeNumber, episodeTitleKey), Publisher.HBO);
            episode.setSeriesNumber(Integer.valueOf(seasonNumber));
            episode.setEpisodeNumber(Integer.valueOf(episodeNumber));
            
            String headingLabel = navigator.firstElementOrNull("//content//heading/label").getValue();
            String episodeTitle = headingLabel.substring(headingLabel.indexOf(": ") + 2);
            episode.setTitle(episodeTitle);
            
            String imageUrl = navigator.firstElementOrNull("//content//imagePath[@width='1024']/path").getValue();
            episode.setImage(imageUrl);
            
            String synopsisContents = client.getContentsOf(helper.getXmlUri(helper.getNavigationLink("synopsis", navigator)));
            SimpleXmlNavigator synopsisNavigator = new SimpleXmlNavigator(synopsisContents);
            
            String formattedSynopsis = synopsisNavigator.firstElementOrNull("//content/Article/article/body").getValue();
            episode.setDescription(helper.removeFormatting(formattedSynopsis));
            
            return episode;
        }
        catch (Exception e) {
            log.record(new AdapterLogEntry(Severity.ERROR).withCause(e).withSource(HboItemAdapter.class).withUri(uri));
        }
        return null;
    }

    @Override
    public boolean canFetch(String uri) {
        return canFetchPattern.matcher(uri).matches();
    }
    
    private String getCurie(String showTitle, String seriesNumber, String episodeNumber, String episodeTitleKey) {
        return "hbo:" + showTitle + "-" + seriesNumber + "-" + episodeNumber + "-" + episodeTitleKey;
    }
}
