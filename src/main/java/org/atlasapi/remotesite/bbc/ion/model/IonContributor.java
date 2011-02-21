package org.atlasapi.remotesite.bbc.ion.model;

import org.joda.time.DateTime;

public class IonContributor {

    private String characterName;
    private Integer episodeCount;
    private String familyName;
    private String givenName;
    private String searchUrl;
    private String roleName;
    private DateTime updated;
    private String id;
    private String type;
    private String role;
    public String getCharacterName() {
        return characterName;
    }
    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }
    public Integer getEpisodeCount() {
        return episodeCount;
    }
    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }
    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    public String getGivenName() {
        return givenName;
    }
    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }
    public String getSearchUrl() {
        return searchUrl;
    }
    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public DateTime getUpdated() {
        return updated;
    }
    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    
}
