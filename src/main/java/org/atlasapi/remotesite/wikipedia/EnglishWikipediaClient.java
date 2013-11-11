package org.atlasapi.remotesite.wikipedia;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.LinkedList;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnglishWikipediaClient implements ArticleFetcher, FilmArticleTitleSource {
    private static final Logger log = LoggerFactory.getLogger(EnglishWikipediaClient.class);
    
    private static final MediaWikiBot bot = new MediaWikiBot("http://en.wikipedia.org/w/");
    
    private Iterable<String> filmIndexPageTitles() {
        return ImmutableList.of(
            "List of films: numbers",
            "List of films: A",
            "List of films: B",
            "List of films: C",
            "List of films: D",
            "List of films: E",
            "List of films: F",
            "List of films: G",
            "List of films: H",
            "List of films: I",
            "List of films: J–K",
            "List of films: L",
            "List of films: M",
            "List of films: N–O",
            "List of films: P",
            "List of films: Q–R",
            "List of films: S",
            "List of films: T",
            "List of films: U–W",
            "List of films: X–Z"
        );
    }
    
    @Override
    public Article fetchArticle(String title) {
        return bot.getArticle(title);
    }

    @Override
    public Collection<String> getAllFilmArticleTitles() {
        LinkedList<String> result = new LinkedList<String>();
        for(String indexTitle : filmIndexPageTitles()) {
            try {
                result.addAll(FilmIndexScraper.extractNames(fetchArticle(indexTitle).getText()));
            } catch (Exception ex) {
                log.error("Failed to load some of the film article names ("+ indexTitle +") – they'll be skipped!", ex);
            }
        }
        return result;
    }

}
