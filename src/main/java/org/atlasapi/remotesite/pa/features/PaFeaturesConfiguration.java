package org.atlasapi.remotesite.pa.features;

import java.util.Map;

import com.google.common.collect.ImmutableMap;


public class PaFeaturesConfiguration {

    private final Map<String, ContentGroupDetails> featureSetMap;
    
    public PaFeaturesConfiguration(Map<String, ContentGroupDetails> featureSetMap) {
        this.featureSetMap = ImmutableMap.copyOf(featureSetMap);
    }
    
    public Map<String, ContentGroupDetails> getFeatureSetMap() {
        return featureSetMap;
    }
}
