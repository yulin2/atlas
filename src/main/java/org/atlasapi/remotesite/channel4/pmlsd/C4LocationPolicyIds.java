package org.atlasapi.remotesite.channel4.pmlsd;


public class C4LocationPolicyIds {

    private final Long webServiceId;
    private final Long playerId;
    private final Long iOsServiceId;
    
    private C4LocationPolicyIds(Long webServiceId, Long playerId, Long iOsServiceId) {
        this.webServiceId = webServiceId;
        this.playerId = playerId;
        this.iOsServiceId = iOsServiceId;
    }
    
    public Long getWebServiceId() {
        return webServiceId;
    }
    
    public Long getPlayerId() {
        return playerId;
    }
    
    public Long getIOsServiceId() {
        return iOsServiceId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Long webServiceId;
        private Long playerId;
        private Long iOsServiceId;

        private Builder() {
            
        }
        
        public Builder withWebServiceId(Long webServiceId) {
            this.webServiceId = webServiceId;
            return this;
        }
        
        public Builder withIosServiceId(Long iOsServiceId) {
            this.iOsServiceId = iOsServiceId;
            return this;
        }
        
        public Builder withPlayerId(Long playerId) {
            this.playerId = playerId;
            return this;
        }
        
        public C4LocationPolicyIds build() {
            return new C4LocationPolicyIds(webServiceId, playerId, iOsServiceId);
        }
    }
}
