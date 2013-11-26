package org.atlasapi.remotesite.wikipedia.testutils;

import com.google.api.client.util.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.atlasapi.remotesite.wikipedia.Article;
import org.atlasapi.remotesite.wikipedia.ArticleFetcher;
import org.joda.time.DateTime;

public class LocallyCachingArticleFetcher implements ArticleFetcher {
    private final ArticleFetcher fetcher;
    private final String outputDirPath;
    
    public LocallyCachingArticleFetcher(ArticleFetcher fetcher, String outputDirPath) {
        this.fetcher = fetcher;
        this.outputDirPath = outputDirPath;
    }
    
    public static String titleToFilename(String title) {
        return title.replaceAll("/", "-") + ".mediawiki";
    }
    
    public void downloadArticle(String title) throws IOException {
        System.out.println(title);
        Article fetchArticle = fetcher.fetchArticle(title);
        Files.write(fetchArticle.getMediaWikiSource(), new File(outputDirPath + "/" + titleToFilename(title)), Charsets.UTF_8);
    }

    @Override
    public Article fetchArticle(final String title) {
        File path = new File(outputDirPath + "/" + titleToFilename(title));
        try {
            if (! path.isFile()) {
                downloadArticle(title);
            }
            final String source = Files.toString(path, Charsets.UTF_8);
            return new Article() {
                @Override
                public DateTime getLastModified() {
                    return new DateTime();
                }

                @Override
                public String getMediaWikiSource() {
                    return source;
                }

                @Override
                public String getTitle() {
                    return title;
                }
            };
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
