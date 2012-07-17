package org.atlasapi.remotesite.metabroadcast;

import java.util.Iterator;
import java.util.List;

import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.Topic.Type;

public class ContentWords {

    public static class WordWeighting {

        private String content;
        private int weight;
        private String url;
        private String value;
        private String type;
        
        public WordWeighting(){
        }
        
        public WordWeighting(String content, int weight, String url, String value, String type){
            this.content = content;
            this.weight = weight;
            this.url = url;
            this.value = value;
            this.type = type;
        }
        
        public String getContent() {
            return this.content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getWeight() {
            return this.weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public String getType() {
            return type;
        }

    }

    public static class ContentWordsList implements Iterable<ContentWords> {

        private List<ContentWords> results;

        public List<ContentWords> getResults() {
            return results;
        }

        public void setResults(List<ContentWords> results) {
            this.results = results;
        }

        @Override
        public Iterator<ContentWords> iterator() {
            return results.iterator();
        }

    }

    private String contentId;
    private List<WordWeighting> words;
    private String uri;

    public String getContentId() {
        return this.contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public List<WordWeighting> getWords() {
        return this.words;
    }

    public void setWords(List<WordWeighting> words) {
        this.words = words;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
