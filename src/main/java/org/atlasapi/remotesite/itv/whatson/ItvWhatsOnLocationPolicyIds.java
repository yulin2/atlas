package org.atlasapi.remotesite.itv.whatson;


public class ItvWhatsOnLocationPolicyIds {

    private final Long webServiceId;
    private final Long itvPlayerId;
    
    private ItvWhatsOnLocationPolicyIds(Long webServiceId, Long itvPlayerId) {
        this.webServiceId = webServiceId;
        this.itvPlayerId = itvPlayerId;
    }
    
    public Long getWebServiceId() {
        return webServiceId;
    }
    
    public Long getItvPlayerId() {
        return itvPlayerId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Long webServiceId;
        private Long itvPlayerId;
        
        private Builder() {
            
        }
        
        public Builder withWebServiceId(Long webServiceId) {
            this.webServiceId = webServiceId;
            return this;
        }
        
        public Builder withItvPlayerId(Long itvPlayerId) {
            this.itvPlayerId = itvPlayerId;
            return this;
        }
        
        public ItvWhatsOnLocationPolicyIds build() {
            return new ItvWhatsOnLocationPolicyIds(webServiceId, itvPlayerId);
        }
    }
}
