//package org.atlasapi.remotesite.hulu;
//
//import org.atlasapi.remotesite.ContentWriters;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class HuluAdapterModule {
//    
//    private @Autowired ContentWriters writers;
//
//    public @Bean
//    HuluItemAdapter huluItemAdapter() {
//        HuluItemAdapter huluItemAdapter = new HuluItemAdapter();
//        huluItemAdapter.setContentStore(writers);
//        huluItemAdapter.setBrandAdapter(huluBrandAdapter());
//        return huluItemAdapter;
//    }
//
//    public @Bean HuluBrandAdapter huluBrandAdapter() {
//        HuluBrandAdapter huluBrandAdapter = new HuluBrandAdapter(writers);
//        huluBrandAdapter.setEpisodeAdapter(new HuluItemAdapter());
//        return huluBrandAdapter;
//    }
//}
