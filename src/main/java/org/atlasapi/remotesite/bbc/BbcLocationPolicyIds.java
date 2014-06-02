package org.atlasapi.remotesite.bbc;

public class BbcLocationPolicyIds {

    private final Long webServiceId;
    private final Long iPlayerPlayerId;
    
    private BbcLocationPolicyIds(Long webServiceId, Long iPlayerPlayerId) {
        this.webServiceId = webServiceId;
        this.iPlayerPlayerId = iPlayerPlayerId;
    }
    
    public Long getWebServiceId() {
        return webServiceId;
    }
    
    public Long getIPlayerPlayerId() {
        return iPlayerPlayerId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Long webServiceId;
        private Long iPlayerPlayerId;

        private Builder() {
            
        }
        
        public Builder withWebServiceId(long webServiceId) {
            this.webServiceId = webServiceId;
            return this;
        }
        
        public Builder withIPlayerPlayerId(long iPlayerPlayerId) {
            this.iPlayerPlayerId = iPlayerPlayerId;
            return this;
        }
        
        public BbcLocationPolicyIds build() {
            return new BbcLocationPolicyIds(webServiceId, iPlayerPlayerId);
        }
    }
}
