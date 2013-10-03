package org.atlasapi.query.content.fuzzy;

import java.lang.reflect.Type;

import org.atlasapi.media.entity.simple.ContentIdentifier;
import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.search.ContentSearcher;
import org.atlasapi.search.model.SearchQuery;
import org.atlasapi.search.model.SearchResults;
import org.atlasapi.search.model.SearchResultsError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.metabroadcast.common.http.HttpResponse;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.url.Urls;

public class RemoteFuzzySearcher implements ContentSearcher {

    private static final Logger log = LoggerFactory.getLogger(RemoteFuzzySearcher.class);
    
    private final SimpleHttpClient client = HttpClients.webserviceClient();
    private final Gson gson = new GsonBuilder()
                                    .registerTypeAdapter(ContentIdentifier.class, new ContentIdentifierDeserializer())
                                    .create();
    
    private final String remoteHost;

    public RemoteFuzzySearcher(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public SearchResults search(SearchQuery query) {
        String queryString = Urls.appendParameters(remoteHost+"/titles",query.toQueryStringParameters());
        try {
            log.trace("Calling remote searcher, request URI is {}", queryString);
            HttpResponse response = client.get(queryString);
            if (HttpStatusCode.OK.is(response.statusCode())) {
                return gson.fromJson(response.body(), SearchResults.class);
            }
            throw new RemoteFuzzySearcherException(gson.fromJson(response.body(), SearchResultsError.class));
        } catch (Exception e) {
            throw new RuntimeException("Query: " + queryString, e);
        }
    }

    static class RemoteFuzzySearcherException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RemoteFuzzySearcherException(SearchResultsError error) {
            super(error.getMessage());
        }
    }
    
    public static class ContentIdentifierDeserializer implements JsonDeserializer<ContentIdentifier> {
        
        @Override
        public ContentIdentifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObj = json.getAsJsonObject();
            String uri = jsonObj.get("uri").getAsString();
            String type = jsonObj.get("type").getAsString();
            JsonElement idElement = jsonObj.get("id");
            String id = idElement != null ? idElement.getAsString() : null;
            
            if ("series".equals(type)) {
                JsonElement seriesElement = jsonObj.get("seriesNumber");
                Integer seriesNumber = seriesElement != null ? seriesElement.getAsInt() : null;
                return ContentIdentifier.seriesIdentifierFrom(uri, id, seriesNumber);
            } else {
                return ContentIdentifier.identifierFrom(id, uri, type);
            }
        }
    }
}
