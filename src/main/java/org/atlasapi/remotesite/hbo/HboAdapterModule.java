//package org.atlasapi.remotesite.hbo;
//
//import org.atlasapi.persistence.logging.AdapterLog;
//import org.atlasapi.remotesite.HttpClients;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class HboAdapterModule {
//
//    private @Autowired AdapterLog log;
//    
//    public @Bean HboAdapterHelper hboAdapterHelper() {
//        return new HboAdapterHelper();
//    }
//    
//    public @Bean HboItemAdapter hboEpisodeAdapter() {
//        return new HboItemAdapter(HttpClients.screenScrapingClient(), log, hboAdapterHelper());
//    }
//    
//    public @Bean HboBrandAdapter hboBrandAdapter() {
//        return new HboBrandAdapter(hboEpisodeAdapter(), HttpClients.screenScrapingClient(), log, hboAdapterHelper());
//    }
//    
//}
