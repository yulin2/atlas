package org.atlasapi.remotesite.worldservice.model;

public class WsAudioItemProgLink {

    public static Builder wsAudioItemProgLinkBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String audioItemId;
        private String progId;
        private String progAudioSeqNo;
        private String lastAmendTimestamp;
        private String timestamp;

        public Builder withAudioItemId(String audioItemId) {
            this.audioItemId = audioItemId;
            return this;
        }

        public Builder withProgId(String progId) {
            this.progId = progId;
            return this;
        }

        public Builder withProgAudioSeqNo(String progAudioSeqNo) {
            this.progAudioSeqNo = progAudioSeqNo;
            return this;
        }

        public Builder withLastAmendTimestamp(String lastAmendTimestamp) {
            this.lastAmendTimestamp = lastAmendTimestamp;
            return this;
        }

        public Builder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public WsAudioItemProgLink build() {
            WsAudioItemProgLink audioItemProgLink = new WsAudioItemProgLink();
            audioItemProgLink.audioItemId = audioItemId;
            audioItemProgLink.progId = progId;
            audioItemProgLink.progAudioSeqNo = progAudioSeqNo;
            audioItemProgLink.lastAmendTimestamp = lastAmendTimestamp;
            audioItemProgLink.timestamp = timestamp;
            return audioItemProgLink;
        }
    }

    private WsAudioItemProgLink() {
    }

    private String audioItemId;
    private String progId;
    private String progAudioSeqNo;
    private String lastAmendTimestamp;
    private String timestamp;

    public String getAudioItemId() {
        return audioItemId;
    }

    public String getProgId() {
        return progId;
    }

    public String getProgAudioSeqNo() {
        return progAudioSeqNo;
    }

    public String getLastAmendTimestamp() {
        return lastAmendTimestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
