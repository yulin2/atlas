package org.atlasapi.remotesite.voila;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static org.atlasapi.http.HttpBackedRemoteSiteClient.httpRemoteSiteClient;
import static org.atlasapi.http.HttpResponseTransformers.gsonResponseTransformer;
import static org.atlasapi.persistence.logging.AdapterLogEntry.errorEntry;
import static org.atlasapi.remotesite.HttpClients.webserviceClient;

import java.util.List;

import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.system.RemoteSiteClient;
import org.atlasapi.remotesite.voila.CannonTwitterTopicsUpdater.ContentWordsIdList;
import org.atlasapi.remotesite.voila.ContentWords.ContentWordsList;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.net.HostSpecifier;
import com.google.gson.GsonBuilder;
import com.metabroadcast.common.http.SimpleHttpClient;

public class CannonTwitterTopicsClient {

    private final Joiner joiner = Joiner.on(',');
    
    private final String idListRequestUri;
    private final String contentWordsRequestBase;
    
    private final RemoteSiteClient<ContentWordsIdList> idListClient;
    private final RemoteSiteClient<ContentWordsList> contentWordsClient;
    private final AdapterLog log;

    public CannonTwitterTopicsClient(SimpleHttpClient httpClient, HostSpecifier cannonHost, Optional<Integer> cannonPort, AdapterLog log) {
        GsonBuilder gson = new GsonBuilder().setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES);
        this.idListClient = httpRemoteSiteClient(httpClient, gsonResponseTransformer(gson, ContentWordsIdList.class));
        this.contentWordsClient = httpRemoteSiteClient(webserviceClient(), gsonResponseTransformer(gson, ContentWordsList.class));

        this.idListRequestUri = String.format("http://%s%s/contentWordsList", cannonHost, cannonPort.isPresent() ? ":"+cannonPort.get():"");
        this.contentWordsRequestBase = String.format("http://%s%s/contentWords?ids=", cannonHost, cannonPort.isPresent() ? ":"+cannonPort.get() : "");
        
        this.log = log;
    }
    
    public Optional<ContentWordsIdList> getIdList() {
        try {
            return Optional.of(idListClient.get(idListRequestUri));
        } catch (Exception e) {
            log.record(errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to get contentWordsList"));
            return Optional.absent();
        }
    }
    
    public Optional<ContentWordsList> getContentWordsForIds(List<String> contentIds) {
        try {
            return Optional.of(contentWordsClient.get(joiner.appendTo(new StringBuilder(contentWordsRequestBase), contentIds).toString()));
        } catch (Exception e) {
            log.record(AdapterLogEntry.errorEntry().withCause(e).withSource(getClass()).withDescription("Failed to get words for %s",joiner.join(contentIds)));
            return Optional.absent();
        }
    }
}
