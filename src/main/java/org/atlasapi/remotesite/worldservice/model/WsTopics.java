package org.atlasapi.remotesite.worldservice.model;

import java.util.List;

public class WsTopics {

    public static class TopicWeighting {
        private String topicValue;
        private Float weighting;

        public TopicWeighting(String topicValue, float weighting) {
            this.topicValue = topicValue;
            this.weighting = weighting;
        }

        public String getTopicValue() {
            return this.topicValue;
        }

        public void setTopicValue(String topicValue) {
            this.topicValue = topicValue;
        }

        public Float getWeighting() {
            return this.weighting;
        }

        public void setWeighting(Float weighting) {
            this.weighting = weighting;
        }
    }

    private String id;
    private List<TopicWeighting> topics;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TopicWeighting> getTopics() {
        return this.topics;
    }

    public void setTopics(List<TopicWeighting> topics) {
        this.topics = topics;
    }

}
