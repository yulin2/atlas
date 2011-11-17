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

    public static class SlashProgrammesProgramme {
        
        private List<SlashProgrammesRelatedLink> links;

        public List<SlashProgrammesRelatedLink> getLinks() {
            return links;
        }

        public void setLinks(List<SlashProgrammesRelatedLink> links) {
            this.links = links;
        }

    }
    
}
