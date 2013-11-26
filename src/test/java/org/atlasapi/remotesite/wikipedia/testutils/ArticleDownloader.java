package org.atlasapi.remotesite.wikipedia.testutils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.atlasapi.remotesite.wikipedia.Article;
import org.atlasapi.remotesite.wikipedia.EnglishWikipediaClient;

import com.google.api.client.util.Charsets;
import com.google.common.io.Files;

public class ArticleDownloader {
    private static final EnglishWikipediaClient ewc = new EnglishWikipediaClient();
    
    public static void main(String... args) throws IOException {
        File titleList = new File(args[0]);
        String outputDir = System.getProperty("user.home") + "/atlasTestCaches/wikipedia/films";
        List<String> readLines = Files.readLines(titleList, Charsets.UTF_8);
        for (String title : readLines) {
            System.out.println(title);
            Article fetchArticle = ewc.fetchArticle(title);
            String safeTitle = title.replaceAll("/", "-");
            Files.write(fetchArticle.getMediaWikiSource(), new File(outputDir + "/" + safeTitle + ".mediawiki"), Charsets.UTF_8);
        }
    }
}
