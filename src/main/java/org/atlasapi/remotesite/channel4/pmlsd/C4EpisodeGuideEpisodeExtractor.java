package org.atlasapi.remotesite.channel4.pmlsd;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.atlasapi.media.entity.Episode;
import org.jdom.Element;

import com.metabroadcast.common.time.Clock;
import com.sun.syndication.feed.atom.Entry;

final class C4EpisodeGuideEpisodeExtractor extends BaseC4EpisodeExtractor {

    public C4EpisodeGuideEpisodeExtractor(Clock clock) {
        super(clock);
    }
    
    private static final Pattern EPISODE_PAGE_ID_PATTERN = Pattern.compile("^.*/([^\\/]+/episode-guide/series-\\d+/episode-\\d+)$");
    
    @Override
    protected Episode setAdditionalEpisodeFields(Entry entry, Map<String, String> lookup,
            Episode episode) {
        episode.addAliasUrl(hierarchyEpisodeUri(entry));
        episode.addAliasUrl(entry.getId());
        return episode;
    }
    
    private String hierarchyEpisodeUri(Entry source) {
        Matcher matcher = EPISODE_PAGE_ID_PATTERN.matcher(source.getId());
        if (matcher.matches()) {
            return C4AtomApi.WEB_BASE + matcher.group(1);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Element getMedia(Entry source) {
        for (Element element : (List<Element>) source.getForeignMarkup()) {
            if (C4AtomApi.NS_MEDIA_RSS.equals(element.getNamespace())
                && "content".equals(element.getName())) {
                return element;
            }
        }
        return null;
    }
}
