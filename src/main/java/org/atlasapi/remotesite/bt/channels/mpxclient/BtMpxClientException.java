package org.atlasapi.remotesite.bt.channels.mpxclient;


public class BtMpxClientException extends Exception {

    private static final long serialVersionUID = 8235069886447368142L;

    public BtMpxClientException() {
        super();
    }

    public BtMpxClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public BtMpxClientException(String message) {
        super(message);
    }

    public BtMpxClientException(Throwable cause) {
        super(cause);
    }
}
