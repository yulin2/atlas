package org.atlasapi.remotesite.redux;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.atlasapi.remotesite.HttpClients;
import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

import com.google.common.collect.ImmutableList;
import com.google.common.net.HostSpecifier;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpClientBuilder;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.security.UsernameAndPassword;

public class HttpBackedReduxClient implements ReduxClient {

    public static final Builder reduxClientForHost(HostSpecifier host) {
        return new Builder(host);
    }
    
    public static final class Builder {
        
        private HostSpecifier host;
        private UsernameAndPassword credentials;
        private String basePath = "/";

        public Builder(HostSpecifier host) {
            this.host = host;
        }
        
        public Builder withCredentials(UsernameAndPassword credentials) {
            this.credentials = credentials;
            return this;
        }
        
        public Builder withBasePath(String basePath) {
            this.basePath = basePath;
            return this;
        }
        
        public HttpBackedReduxClient build() {
            return new HttpBackedReduxClient(new SimpleHttpClientBuilder()
                .withUserAgent(HttpClients.ATLAS_USER_AGENT)
                .withPreemptiveBasicAuth(credentials)
                .withAcceptHeader(MimeType.APPLICATION_JSON).build(), "http://" + host.toString() + basePath);
        }
    }
    
    private final SimpleHttpClient httpClient;
    private final String requestBase;
    
    private final Gson gson;
    
    public HttpBackedReduxClient(SimpleHttpClient httpClient, String requestBase) {
        this.httpClient = httpClient;
        this.requestBase = requestBase;
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }
    
    @Override
    public FullReduxProgramme programmeFor(final String diskRef) throws HttpException, Exception {
        return getAsType(String.format("%sprogramme/%s", requestBase, diskRef), TypeToken.get(FullReduxProgramme.class));
    }

    private <T> T getAsType(final String url, final TypeToken<T> type) throws HttpException, Exception {
        return httpClient.get(SimpleHttpRequest.httpRequestFrom(url, 
            new HttpResponseTransformer<T>() {
                @Override
                public T transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                    if(HttpStatusCode.OK.code() == prologue.statusCode()) {
                        return gson.fromJson(new InputStreamReader(body), type.getType());
                    }
                    throw new HttpException(String.format("Request %s failed: %s %s", url, prologue.statusCode(), prologue.statusLine()), prologue);
                }
            })
        );
    }

    @Override
    public List<BaseReduxProgramme> programmesForDay(LocalDate date) throws HttpException, Exception {
        String formattedDate = date.toString(ISODateTimeFormat.date());
        List<BaseReduxProgramme> programmes = getAsType(String.format("%sday/%s", requestBase, formattedDate), new TypeToken<List<BaseReduxProgramme>>() {});
        return ImmutableList.copyOf(programmes);
    }

    @Override
    public PaginatedBaseProgrammes latest(Selection selection) throws HttpException, Exception {
        return getAsType(selection.appendToUrl(requestBase + "latest"), TypeToken.get(PaginatedBaseProgrammes.class));
    }
    
}
