package org.atlasapi.remotesite.channel4.pmlsd;


public class C4LocationPolicyIds {

    private final Long serviceId;
    private final Long playerId;
    
    private C4LocationPolicyIds(Long serviceId, Long playerId) {
        this.serviceId = serviceId;
        this.playerId = playerId;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public Long getPlayerId() {
        return playerId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        
        private Long serviceId;
        private Long playerId;

        private Builder() {
            
        }
        
        public Builder withServiceId(long serviceId) {
            this.serviceId = serviceId;
            return this;
        }
        
        public Builder withPlayerId(long playerId) {
            this.playerId = playerId;
            return this;
        }
        
        public C4LocationPolicyIds build() {
            return new C4LocationPolicyIds(serviceId, playerId);
        }
    }
}
