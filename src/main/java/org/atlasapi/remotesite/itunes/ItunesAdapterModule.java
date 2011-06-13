//package org.atlasapi.remotesite.itunes;
//
//import org.atlasapi.persistence.logging.AdapterLog;
//import org.atlasapi.remotesite.HttpClients;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ItunesAdapterModule {
//    
//    private @Autowired AdapterLog log;
//    
//    public @Bean ItunesBrandAdapter itunesBrandAdapter() {
//        return new ItunesBrandAdapter(HttpClients.webserviceClient(), log, itunesAdapterHelper(), itunesSeriesFinder(), itunesEpisodesFinder());
//    }
//    
//    public @Bean ItunesAdapterHelper itunesAdapterHelper() {
//        return new ItunesAdapterHelper();
//    }
//    
//    public @Bean ItunesSeriesFinder itunesSeriesFinder() {
//        return new ItunesSeriesFinder(HttpClients.webserviceClient(), log, itunesAdapterHelper());
//    }
//    
//    public @Bean ItunesEpisodesFinder itunesEpisodesFinder() {
//        return new ItunesEpisodesFinder(HttpClients.webserviceClient(), log, itunesAdapterHelper());
//    }
//}
