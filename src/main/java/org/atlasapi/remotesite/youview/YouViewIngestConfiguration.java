package org.atlasapi.remotesite.youview;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.atlasapi.media.entity.Publisher;

import com.google.common.collect.ImmutableMap;


/**
 * 
 * @author tom
 *
 */
public class YouViewIngestConfiguration {

    private Map<String, Publisher> aliasPrefixToPublisherMap;
    private String aliasNamespacePrefix;
    
    public YouViewIngestConfiguration(Map<String, Publisher> aliasPrefixToPublisher, 
            String aliasNamespacePrefix) {

        this.aliasPrefixToPublisherMap = ImmutableMap.copyOf(aliasPrefixToPublisher);
        this.aliasNamespacePrefix = checkNotNull(aliasNamespacePrefix);
    }
    
    public Map<String, Publisher> getAliasPrefixToPublisherMap() {
        return aliasPrefixToPublisherMap;
    }
    
    public String getAliasNamespacePrefix() {
        return aliasNamespacePrefix;
    }
}
