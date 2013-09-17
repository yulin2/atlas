package org.atlasapi.remotesite.bbc.nitro;


public class NitroException extends Exception {

    public NitroException() {
    }

    public NitroException(String message) {
        super(message);
    }

    public NitroException(Throwable cause) {
        super(cause);
    }

    public NitroException(String message, Throwable cause) {
        super(message, cause);
    }

}
