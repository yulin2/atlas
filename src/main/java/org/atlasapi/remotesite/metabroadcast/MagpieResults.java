//// This class is copied from Magpie directly, if it is changed on Magpie it should be changed here
//package org.atlasapi.remotesite.metabroadcast;
//
//import java.util.HashMap;
//import java.util.List;
//
//import com.google.common.collect.ImmutableList;
//
//public class MagpieResults {
//    private final List<MagpieScheduleItem> results;
//    
//    public static class MagpieResultsBuider {
//        private HashMap<String, MagpieScheduleItem>  results;
//        public MagpieResultsBuider() {
//            this.results = new HashMap<String, MagpieScheduleItem>();
//        }
//
//        public void addResult(MagpieScheduleItem item) {
//            if (!this.results.containsKey(item.getUri())) {
//                results.put(item.getUri(), item);
//            }
//        }
//        
//        public boolean isPresent(String uri) {
//            return this.results.containsKey(uri);
//        }
//        
//        public HashMap<String, MagpieScheduleItem> getResults() {
//            return results;
//        }
//
//        public void setResults(HashMap<String, MagpieScheduleItem> results) {
//            this.results = results;
//        }
//        
//        public MagpieResults build() {
//            return new MagpieResults(this);
//        }
//    }
//    
//    private MagpieResults(MagpieResultsBuider builder) {
//        this.results = ImmutableList.copyOf(builder.getResults().values());
//    }
//
//    public List<MagpieScheduleItem> getResults() {
//        return results;
//    }
//    
//    public static MagpieResultsBuider builder() {
//        return new MagpieResultsBuider();
//    }
//}
