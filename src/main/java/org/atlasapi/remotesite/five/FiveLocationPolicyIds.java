package org.atlasapi.remotesite.five;


public class FiveLocationPolicyIds {

    private long iosServiceId;
    private long webServiceId;
    private long demand5PlayerId;
    
    public static Builder builder() {
        return new Builder();
    }
    
    private FiveLocationPolicyIds(long iosServiceId, long webServiceId, long demand5PlayerId) {
        this.iosServiceId = iosServiceId;
        this.webServiceId = webServiceId;
        this.demand5PlayerId = demand5PlayerId;
    }
    
    public long getIosServiceId() {
        return iosServiceId;
    }
    
    public long getWebServiceId() {
        return webServiceId;
    }
    
    public long getDemand5PlayerId() {
        return demand5PlayerId;
    }
    
    public static class Builder {
    
        private long iosServiceId;
        private long webServiceId;
        private long demand5PlayerId;
        
        private Builder() {
            
        }
        
        public Builder withIosServiceId(long iosServiceId) {
            this.iosServiceId = iosServiceId;
            return this;
        }
        
        public Builder withWebServiceId(long webServiceId) {
            this.webServiceId = webServiceId;
            return this;
        }
        
        public Builder withDemand5PlayerId(long demand5PlayerId) {
            this.demand5PlayerId = demand5PlayerId;
            return this;
        }
        
        public FiveLocationPolicyIds build() {
            return new FiveLocationPolicyIds(iosServiceId, webServiceId, demand5PlayerId);
        }
    }
}
