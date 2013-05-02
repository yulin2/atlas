//package org.atlasapi.remotesite.metabroadcast;
//
//import java.util.List;
//
//import org.atlasapi.media.entity.simple.KeyPhrase;
//import org.atlasapi.media.entity.simple.TopicRef;
//
//import com.google.common.collect.ImmutableList;
//import com.google.inject.internal.Lists;
//
//public class MagpieScheduleItem {
//    private final String uri;
//    private final String title;
//    private final String description;
//    private final List<KeyPhrase> keyPhrases;
//    private final List<TopicRef> topics;
//    
//    public final static class MagpieScheduleItemBuilder {
//        private String uri;
//        private String title;
//        private String description;
//        private List<KeyPhrase> keyPhrases = Lists.newArrayList();
//        private List<TopicRef> topics = ImmutableList.of();
//        
//        public MagpieScheduleItemBuilder withURI(String uri) {
//            this.uri = uri;
//            return this;
//        }
//        
//        public MagpieScheduleItemBuilder withTitle(String title) {
//            this.title = title;
//            return this;
//        }
//        
//        public MagpieScheduleItemBuilder withDescription(String description) {
//            this.description = description;
//            return this;
//        }
//        
//        public MagpieScheduleItemBuilder withTopics(List<TopicRef> topics) {
//            this.topics = topics;
//            return this;
//        }
//        
//        public MagpieScheduleItemBuilder addKeyPhrase(KeyPhrase keyPhrase) {
//            this.keyPhrases.add(keyPhrase);
//            return this;
//        }
//        
//        public MagpieScheduleItemBuilder addTopic(TopicRef topicRef) {
//            this.topics.add(topicRef);
//            return this;
//        }
//        
//        public String getUri() {
//            return uri;
//        }
//
//        public void setUri(String uri) {
//            this.uri = uri;
//        }
//
//        public String getTitle() {
//            return title;
//        }
//
//        public void setTitle(String title) {
//            this.title = title;
//        }
//
//        public String getDescription() {
//            return description;
//        }
//
//        public void setDescription(String description) {
//            this.description = description;
//        }
//
//        public List<KeyPhrase> getKeyPhrases() {
//            return keyPhrases;
//        }
//
//        public void setKeyPhrases(List<KeyPhrase> keyPhrases) {
//            this.keyPhrases = keyPhrases;
//        }
//        
//        
//
//        public List<TopicRef> getTopics() {
//            return topics;
//        }
//
//        public void setTopics(List<TopicRef> topics) {
//            this.topics = topics;
//        }
//
//        public MagpieScheduleItem build() {
//            this.addKeyPhrase(this.generateHashtag());
//            return new MagpieScheduleItem(this);
//        }
//        
//        public KeyPhrase generateHashtag() {
//            String[] words = this.title.split(" ");
//            int maxCharsToTake = words.length > 3 ? 1 : 50;
//            StringBuilder builder = new StringBuilder("#");
//            for (String word : words) {
//                // transform to lower case then remove non-alpha
//                String subTag = word.toLowerCase().replaceAll("[^a-z^\\d]", "");
//                if (subTag.length() > maxCharsToTake) {
//                    subTag = subTag.substring(0, maxCharsToTake);
//                }
//                builder.append(subTag);
//            }
//            return new KeyPhrase(builder.toString(), null, 1.0D);
//        }
//    }
//    
//    private MagpieScheduleItem(MagpieScheduleItemBuilder builder) {
//        this.uri = builder.getUri();
//        this.title = builder.getTitle();
//        this.description = builder.getDescription();
//        this.keyPhrases = ImmutableList.copyOf(builder.getKeyPhrases());
//        this.topics  = ImmutableList.copyOf(builder.getTopics());
//    }
//    
//    public static MagpieScheduleItemBuilder builder() {
//        return new MagpieScheduleItemBuilder();
//    }
//    
//    public String getUri() {
//        return uri;
//    }
//    
//    public String getTitle() {
//        return title;
//    }
//    
//    public String getDescription() {
//        return description;
//    }    
//
//    public List<KeyPhrase> getKeyPhrases() {
//        return keyPhrases;
//    }
//
//    public List<TopicRef> getTopics() {
//        return topics;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder("URI: ");
//        builder.append(this.uri);
//        builder.append(", TITLE: ");
//        builder.append(this.title);
//        builder.append(", KEYPHRASES: ");
//        builder.append(this.keyPhrases.toString());
//        builder.append(", DESCRIPTION: ");
//        builder.append(this.description);
//        return builder.toString();
//    }
//}
