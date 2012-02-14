package org.atlasapi.remotesite.bbc;

import java.util.List;

public class SlashProgrammesContainer {

    private SlashProgrammesProgramme programme;

    public SlashProgrammesProgramme getProgramme() {
        return programme;
    }

    public void setProgramme(SlashProgrammesProgramme programme) {
        this.programme = programme;
    }
    
    public static class SlashProgrammesRelatedLink {
        
        private String type;
        private String title;
        private String url;

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setUrl(String url) {
            this.url = url;
        }

    }
    
    public static class SlashProgrammesCategory {

        private String type;
        private String id;
        private String key;
        private String title;
        private String sameAs;
        
        public String getType() {
            return this.type;
        }
        public String getId() {
            return this.id;
        }
        public String getKey() {
            return this.key;
        }
        public String getTitle() {
            return this.title;
        }
        public String getSameAs() {
            return this.sameAs;
        }
        public void setType(String type) {
            this.type = type;
        }
        public void setId(String id) {
            this.id = id;
        }
        public void setKey(String key) {
            this.key = key;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public void setSameAs(String sameAs) {
            this.sameAs = sameAs;
        }
        
    }

    public static class SlashProgrammesProgramme {
        
        private List<SlashProgrammesRelatedLink> links;
        private List<SlashProgrammesCategory> categories;

        public List<SlashProgrammesRelatedLink> getLinks() {
            return links;
        }

        public void setLinks(List<SlashProgrammesRelatedLink> links) {
            this.links = links;
        }

        public List<SlashProgrammesCategory> getCategories() {
            return categories;
        }

        public void setCategories(List<SlashProgrammesCategory> categories) {
            this.categories = categories;
        }

    }
    
}
