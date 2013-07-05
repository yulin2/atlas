package org.atlasapi.remotesite.btfeatured;

import static com.metabroadcast.common.http.SimpleHttpRequest.httpRequestFrom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.atlasapi.http.AbstractHttpResponseTransformer;
import org.atlasapi.persistence.system.RemoteSiteClient;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.http.SimpleHttpClient;

/**
 * Retrieves an XML Document from a given BT Featured content Uri
 * 
 * @author andrewtoone
 *
 */
public class BtFeaturedClient implements RemoteSiteClient<Document> {

    private final SimpleHttpClient client;
    private final Builder builder;
    
    public BtFeaturedClient(SimpleHttpClient client) {
        this(client, new Builder());
    }

    public BtFeaturedClient(SimpleHttpClient client, Builder builder) {
        this.client = client;
        this.builder = builder;
    }

    @Override
    public Document get(final String uri) throws Exception {
        return client.get(httpRequestFrom(uri, new AbstractHttpResponseTransformer<Document>() {

            @Override
            protected Document transform(InputStreamReader bodyReader) throws Exception {
                return readDocument(uri, bodyReader, builder);
            }
            
            @Override
            protected Set<Integer> acceptableResponseCodes() {
                return ImmutableSet.of(200);
            }
        }));
    }

    static Document readDocument(final String uri, InputStreamReader bodyReader, Builder builder)
            throws IOException, ParsingException, ValidityException {
        BufferedReader reader = new BufferedReader(bodyReader);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine())!= null) {
            sb.append(line);
        }
        
        int start = sb.indexOf("<");
        if (start < 0) {
            start = 0;
        }     

        return builder.build(sb.substring(start), uri);
    }

}
