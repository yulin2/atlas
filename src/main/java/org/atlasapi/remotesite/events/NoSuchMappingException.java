package org.atlasapi.remotesite.events;


public class NoSuchMappingException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public NoSuchMappingException(String message) {
        super(message);
    }

}
