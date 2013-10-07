package org.atlasapi.remotesite.bbc.nitro.v1;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

import org.atlasapi.remotesite.bbc.nitro.NitroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.BackOff;
import com.google.api.client.util.ExponentialBackOff;
import com.google.common.net.HostSpecifier;
import com.google.common.net.MediaType;
import com.google.common.reflect.TypeToken;


public class HttpNitroClient implements NitroClient {
    
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String FORMATS_RESOURCE = "formats";
    private static final String GENRE_GROUPS_RESOURCE = "genre_groups";
    private static final String HTTP_SCHEME = "http";
    private static final String NITRO_V1_RESOURCE_TEMPLATE = "/nitro/api/v1/episodes/%s/%s/";
    private static final String API_KEY_PARAM_NAME = "api_key";
    
    private static final TypeToken<NitroResponse<NitroGenreGroup>> genreGroupResultType
        = new TypeToken<NitroResponse<NitroGenreGroup>>(){};
    private static final TypeToken<NitroResponse<NitroFormat>> formatResultType
        = new TypeToken<NitroResponse<NitroFormat>>(){};
        
    private final HostSpecifier host;
    private final String apiKey;
    private final HttpRequestFactory requestFactory;

    public HttpNitroClient(HostSpecifier host, String apiKey) {
        this.host = checkNotNull(host);
        this.apiKey = checkNotNull(apiKey);
        this.requestFactory = new ApacheHttpTransport()
            .createRequestFactory(new HttpRequestInitializer() {
                
                private final ExponentialBackOff.Builder BACK_OFF = new ExponentialBackOff.Builder()
                    .setInitialIntervalMillis(500);

                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(new JacksonFactory()));
                    request.getHeaders().setAccept(MediaType.JSON_UTF_8.toString());
                    request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(backOff()));
                    request.setUnsuccessfulResponseHandler(new HttpBackOffUnsuccessfulResponseHandler(backOff()));
                }
                
                private BackOff backOff() {
                    return BACK_OFF.build();
                }
            });
    }
    
    @Override
    public List<NitroGenreGroup> genres(String pid) throws NitroException {
        return getUrl(createUrl(pid, GENRE_GROUPS_RESOURCE), genreGroupResultType);
    }

    @Override
    public List<NitroFormat> formats(String pid) throws NitroException {
        return getUrl(createUrl(pid, FORMATS_RESOURCE), formatResultType);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getUrl(GenericUrl url, TypeToken<NitroResponse<T>> resultType) 
            throws NitroException {
        try {
            log.debug("{}", url);
            HttpRequest req = requestFactory.buildGetRequest(url);
            Object parsed = req.execute().parseAs(resultType.getType());
            return ((NitroResponse<T>) parsed).getNitro().getResults().getItems();
        } catch (IOException ioe) {
            throw new NitroException(url.toString(), ioe);
        }
    }

    private GenericUrl createUrl(String pid, String resource) {
        GenericUrl url = new GenericUrl();
        url.setScheme(HTTP_SCHEME);
        url.setHost(host.toString());
        url.setRawPath(String.format(NITRO_V1_RESOURCE_TEMPLATE, pid, resource));
        url.put(API_KEY_PARAM_NAME, apiKey);
        return url;
    }
    
}
