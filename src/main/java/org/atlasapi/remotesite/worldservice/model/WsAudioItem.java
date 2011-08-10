package org.atlasapi.remotesite.worldservice.model;

public class WsAudioItem {

    public static Builder wsAudioItemBuilder() {
        return new Builder();
    }

    public static class Builder {

        private String audioItemId;
        private String barcode;
        private String title;
        private String audioDescription;
        private String otherInfo;
        private String recDate;
        private String origTapeNo;
        private String origMediaFilename;
        private String duration;
        private String monoFlag;
        private String noOfBands;
        private String ibmsHouseMedia;
        private String linkAudioBroadcastQuality;
        private String linkAudioThumbnail;
        private String noOfRecSheetFiles;
        private String inputDatetime;
        private String inputByUserId;
        private String allowDownloadFrom;
        private String batchId;
        private String status;
        private String lastAmendTimestamp;
        private String timestamp;

        public Builder withAudioItemId(String audioItemId) {
            this.audioItemId = audioItemId;
            return this;
        }

        public Builder withBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withAudioDescription(String audioDescription) {
            this.audioDescription = audioDescription;
            return this;
        }

        public Builder withOtherInfo(String otherInfo) {
            this.otherInfo = otherInfo;
            return this;
        }

        public Builder withRecDate(String recDate) {
            this.recDate = recDate;
            return this;
        }

        public Builder withOrigTapeNo(String origTapeNo) {
            this.origTapeNo = origTapeNo;
            return this;
        }

        public Builder withOrigMediaFilename(String origMediaFilename) {
            this.origMediaFilename = origMediaFilename;
            return this;
        }

        public Builder withDuration(String duration) {
            this.duration = duration;
            return this;
        }

        public Builder withMonoFlag(String monoFlag) {
            this.monoFlag = monoFlag;
            return this;
        }

        public Builder withNoOfBands(String noOfBands) {
            this.noOfBands = noOfBands;
            return this;
        }

        public Builder withIbmsHouseMedia(String ibmsHouseMedia) {
            this.ibmsHouseMedia = ibmsHouseMedia;
            return this;
        }

        public Builder withLinkAudioBroadcastQuality(String linkAudioBroadcastQuality) {
            this.linkAudioBroadcastQuality = linkAudioBroadcastQuality;
            return this;
        }

        public Builder withLinkAudioThumbnail(String linkAudioThumbnail) {
            this.linkAudioThumbnail = linkAudioThumbnail;
            return this;
        }

        public Builder withNoOfRecSheetFiles(String noOfRecSheetFiles) {
            this.noOfRecSheetFiles = noOfRecSheetFiles;
            return this;
        }

        public Builder withInputDatetime(String inputDatetime) {
            this.inputDatetime = inputDatetime;
            return this;
        }

        public Builder withInputByUserId(String inputByUserId) {
            this.inputByUserId = inputByUserId;
            return this;
        }

        public Builder withAllowDownloadFrom(String allowDownloadFrom) {
            this.allowDownloadFrom = allowDownloadFrom;
            return this;
        }

        public Builder withBatchId(String batchId) {
            this.batchId = batchId;
            return this;
        }

        public Builder withStatus(String status) {
            this.status = status;
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

        public WsAudioItem build() {
            WsAudioItem audioItem = new WsAudioItem();
            audioItem.audioItemId = audioItemId;
            audioItem.barcode = barcode;
            audioItem.title = title;
            audioItem.audioDescription = audioDescription;
            audioItem.otherInfo = otherInfo;
            audioItem.recDate = recDate;
            audioItem.origTapeNo = origTapeNo;
            audioItem.origMediaFilename = origMediaFilename;
            audioItem.duration = duration;
            audioItem.monoFlag = monoFlag;
            audioItem.noOfBands = noOfBands;
            audioItem.ibmsHouseMedia = ibmsHouseMedia;
            audioItem.linkAudioBroadcastQuality = linkAudioBroadcastQuality;
            audioItem.linkAudioThumbnail = linkAudioThumbnail;
            audioItem.noOfRecSheetFiles = noOfRecSheetFiles;
            audioItem.inputDatetime = inputDatetime;
            audioItem.inputByUserId = inputByUserId;
            audioItem.allowDownloadFrom = allowDownloadFrom;
            audioItem.batchId = batchId;
            audioItem.status = status;
            audioItem.lastAmendTimestamp = lastAmendTimestamp;
            audioItem.timestamp = timestamp;
            return audioItem;
        }

    }

    private WsAudioItem() {
        // TODO Auto-generated constructor stub
    }

    private String audioItemId;
    private String barcode;
    private String title;
    private String audioDescription;
    private String otherInfo;
    private String recDate;
    private String origTapeNo;
    private String origMediaFilename;
    private String duration;
    private String monoFlag;
    private String noOfBands;
    private String ibmsHouseMedia;
    private String linkAudioBroadcastQuality;
    private String linkAudioThumbnail;
    private String noOfRecSheetFiles;
    private String inputDatetime;
    private String inputByUserId;
    private String allowDownloadFrom;
    private String batchId;
    private String status;
    private String lastAmendTimestamp;
    private String timestamp;

    public String getAudioItemId() {
        return audioItemId;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getTitle() {
        return title;
    }

    public String getAudioDescription() {
        return audioDescription;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public String getRecDate() {
        return recDate;
    }

    public String getOrigTapeNo() {
        return origTapeNo;
    }

    public String getOrigMediaFilename() {
        return origMediaFilename;
    }

    public String getDuration() {
        return duration;
    }

    public String getMonoFlag() {
        return monoFlag;
    }

    public String getNoOfBands() {
        return noOfBands;
    }

    public String getIbmsHouseMedia() {
        return ibmsHouseMedia;
    }

    public String getLinkAudioBroadcastQuality() {
        return linkAudioBroadcastQuality;
    }

    public String getLinkAudioThumbnail() {
        return linkAudioThumbnail;
    }

    public String getNoOfRecSheetFiles() {
        return noOfRecSheetFiles;
    }

    public String getInputDatetime() {
        return inputDatetime;
    }

    public String getInputByUserId() {
        return inputByUserId;
    }

    public String getAllowDownloadFrom() {
        return allowDownloadFrom;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getStatus() {
        return status;
    }

    public String getLastAmendTimestamp() {
        return lastAmendTimestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
