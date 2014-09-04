package org.atlasapi.remotesite.opta.events;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import org.atlasapi.remotesite.FetchException;
import org.atlasapi.remotesite.opta.events.model.OptaMatch;
import org.atlasapi.remotesite.opta.events.model.OptaSportConfiguration;
import org.atlasapi.remotesite.opta.events.model.OptaSportType;
import org.atlasapi.remotesite.opta.events.model.OptaTeam;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.common.http.HttpException;
import com.metabroadcast.common.http.HttpResponsePrologue;
import com.metabroadcast.common.http.HttpResponseTransformer;
import com.metabroadcast.common.http.SimpleHttpClient;
import com.metabroadcast.common.http.SimpleHttpRequest;
import com.metabroadcast.common.security.UsernameAndPassword;


public final class HttpOptaEventsFetcher<T extends OptaTeam, M extends OptaMatch> implements OptaEventsFetcher<T, M> {
    
    private static final String OPTA_URL_PATTERN = "http://%s/competition.php?feed_type=%%s&competition=%%s&season_id=%%s&user=%s&psw=%s&json";
    
    private final Map<OptaSportType, OptaSportConfiguration> sportConfig;
    private final SimpleHttpClient client;
    private final String urlPattern;
    private final HttpResponseTransformer<Optional<? extends OptaEventsData<T, M>>> transformer;
    
    public HttpOptaEventsFetcher(Map<OptaSportType, OptaSportConfiguration> sportConfig, 
            SimpleHttpClient client, OptaDataTransformer<T, M> dataTransformer, 
            UsernameAndPassword credentials, String baseUrl) {
        this.sportConfig = ImmutableMap.copyOf(sportConfig);
        this.client = checkNotNull(client);
        checkNotNull(credentials);
        this.urlPattern = String.format(OPTA_URL_PATTERN, checkNotNull(baseUrl), credentials.username(), credentials.password());
        this.transformer = createTransformer(checkNotNull(dataTransformer));
    }

    private HttpResponseTransformer<Optional<? extends OptaEventsData<T, M>>> createTransformer(
            final OptaDataTransformer<T, M> optaDataTransformer) {
        return new HttpResponseTransformer<Optional<? extends OptaEventsData<T, M>>>() {
            @Override
            public Optional<? extends OptaEventsData<T, M>> transform(HttpResponsePrologue prologue, InputStream body)
                    throws HttpException, Exception {
                return Optional.fromNullable(optaDataTransformer.<OptaEventsData<T, M>>transform(body));
            }
        };
    }

    @Override
    public Optional<? extends OptaEventsData<T, M>> fetch(OptaSportType sport) {
        try {
            OptaSportConfiguration config = sportConfig.get(sport);
            if (config == null) {
                throw new IllegalArgumentException("No configuration for sport " + sport.name());
            }
            
            String url = String.format(urlPattern, config.feedType(), config.competition(), config.seasonId());
            
            return client.get(SimpleHttpRequest.httpRequestFrom(url, transformer));
        } catch (Exception e) {
            throw new FetchException(e.getMessage(), e);
        }
    }
    
    @Override
    public Set<OptaSportType> sports() {
        return sportConfig.keySet();
    }
}
