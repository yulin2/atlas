package org.atlasapi.remotesite;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.remotesite.archiveorg.ArchiveOrgModule;
import org.atlasapi.remotesite.bbc.BbcModule;
import org.atlasapi.remotesite.channel4.C4Module;
import org.atlasapi.remotesite.five.FiveModule;
import org.atlasapi.remotesite.itv.ItvModule;
import org.atlasapi.remotesite.itv.interlinking.ItvInterlinkingModule;
import org.atlasapi.remotesite.pa.PaModule;
import org.atlasapi.remotesite.preview.PreviewNetworksModule;
import org.atlasapi.remotesite.redux.ReduxModule;
import org.atlasapi.remotesite.seesaw.SeesawModule;
import org.atlasapi.remotesite.tvblob.TVBlobModule;
import org.atlasapi.remotesite.worldservice.WorldServicesModule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.metabroadcast.common.properties.Configurer;
import com.metabroadcast.common.properties.Parameter;

public class RemoteSiteModuleConfigurer {

    private static Log logger = LogFactory.getLog(RemoteSiteModuleConfigurer.class);

    private Map<String, Class<?>> moduleMap = ImmutableMap.<String, Class<?>>builder()
        .put("bbc",     BbcModule.class)
        .put("itv",     ItvModule.class)
        .put("c4",      C4Module.class)
        .put("five",    FiveModule.class)
 //       .put("ict",     ICTomorrowModule.class)
        .put("aorg",    ArchiveOrgModule.class)
 //       .put("hulu",    HuluModule.class)
 //       .put("hbo",     HboModule.class)
 //       .put("itunes",  ItunesModule.class)
 //       .put("msn",     MsnVideoModule.class)
        .put("pa",      PaModule.class)
        .put("seesaw",  SeesawModule.class)
  //      .put("youtube", YouTubeModule.class)
        .put("tvblob",  TVBlobModule.class)
        .put("preview", PreviewNetworksModule.class)
        .put("itvinterlinking", ItvInterlinkingModule.class)
        .put("worldservice", WorldServicesModule.class)
        .put("redux", ReduxModule.class)
    .build();

    public Iterable<? extends Class<?>> enabledModules() {
        if(Configurer.get("updaters.all.enabled").toBoolean()) {
            return moduleMap.values();
        }
        
        List<Class<?>> enabledModules = Lists.newArrayList();
        
        for (Entry<String,Class<?>> moduleEntry : moduleMap.entrySet()) {
            String moduleName = moduleEntry.getKey();
            
            Parameter moduleParam = Configurer.get("updaters."+moduleName+".enabled");
            
            if(moduleParam != null && moduleParam.toBoolean()) {
                enabledModules.add(moduleEntry.getValue());
                logger.info("Including module " + moduleName);
            } else {
                logger.info("Not including module " + moduleName);
            }
        }
        
        return enabledModules;
    }
 
    
}
