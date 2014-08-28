package org.atlasapi.remotesite.knowledgemotion.topics.spotlight;

import java.util.List;

import org.atlasapi.remotesite.knowledgemotion.topics.WikipediaKeyword;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SpotlightResourceParser {

    /**
     *  from dbpedia: DBpedia data set is denoted by a URI-based reference of the form http://dbpedia.org/resource/Name, 
        where Name is derived from the URL of the source Wikipedia article, which has the form http://en.wikipedia.org/wiki/Name. 
        Thus, each DBpedia entity is tied directly to a Wikipedia article.
     */
    private static final String WIKIPEDIA_URL_PATTERN = "http://en.wikipedia.org/wiki/%s";
    private static final Splitter splitter = Splitter.on("/").omitEmptyStrings();

    public List<WikipediaKeyword> parse(String content) {
        ImmutableList.Builder<WikipediaKeyword> keywords = new ImmutableList.Builder<WikipediaKeyword>();
        JsonObject parse = (JsonObject) new JsonParser().parse(content);

        JsonArray resources = (JsonArray) parse.get("Resources");
        if (resources == null) {
            return ImmutableList.of();
        }

        for (JsonElement elem : resources) {
            keywords.add(createKeyword(elem.getAsJsonObject()));
        }
        return keywords.build();
    }

    private WikipediaKeyword createKeyword(JsonObject elem) {
        WikipediaKeyword.Builder keyword = WikipediaKeyword.builder();
        String resourceName = extractResourceName(elem.get("@URI").getAsString());
        keyword.withWikipediaUrl(String.format(WIKIPEDIA_URL_PATTERN, resourceName));
        keyword.withArticleTitle(resourceName);

        return keyword.build();
    }

    private String extractResourceName(String uri) {
        return Iterables.getLast(splitter.split(uri));
    }

}
