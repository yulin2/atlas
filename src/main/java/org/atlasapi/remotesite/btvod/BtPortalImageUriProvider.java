package org.atlasapi.remotesite.btvod;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Elements;

import com.google.api.client.repackaged.com.google.common.base.Throwables;
import com.google.common.base.Optional;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;


public class BtPortalImageUriProvider implements ImageUriProvider {

    private static final String PACKSHOT_ATTRIBUTE = "packshot";
    private static final String ASSET_ELEMENT = "asset";
    private final SimpleHttpClient httpClient;
    private final Builder parser;
    private final HttpResponseTransformer<Optional<Document>> responseTransformer;
    private final String baseUri;
    
    public BtPortalImageUriProvider(SimpleHttpClient httpClient, String baseUri) {
        this.httpClient = checkNotNull(httpClient);
        this.baseUri = checkNotNull(baseUri);
        this.parser = new Builder();
        this.responseTransformer = responseTransformer();
    }
    
    public Optional<String> imageUriFor(String productId) {
        try {
            Optional<Document> document = httpClient.get(SimpleHttpRequest.httpRequestFrom(productUriFor(productId), responseTransformer));
            if (!document.isPresent()) {
                return Optional.absent();
            }
            
            Elements childElements = document.get().getRootElement().getChildElements(ASSET_ELEMENT);
            if (childElements.size() == 0 
                    || childElements.get(0).getAttribute(PACKSHOT_ATTRIBUTE) == null) {
                return Optional.absent();
            }
            
            return Optional.of(baseUri + childElements.get(0).getAttribute(PACKSHOT_ATTRIBUTE).getValue());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private HttpResponseTransformer<Optional<Document>> responseTransformer() {
        return new HttpResponseTransformer<Optional<Document>>() {

            @Override
            public Optional<Document> transform(HttpResponsePrologue prologue, InputStream body)
                    throws HttpException, Exception {
                if (prologue.statusCode() == HttpStatusCode.NOT_FOUND.code()) {
                    return Optional.absent();
                }
                if (prologue.statusCode() != HttpStatusCode.OK.code()) {
                    throw new RuntimeException(prologue.statusLine());
                }
                return Optional.of(parser.build(body));
            }
        };
    }
    
    private String productUriFor(String productId) {
        return baseUri + "xml/product/" + productId + ".xml";
    }
}
