package org.atlasapi.remotesite.talktalk;

/**
 * General exception for use when interacting with TalkTalk's VOD API. 
 */
public class TalkTalkException extends Exception {

    private static final long serialVersionUID = 7105827879653054544L;

    public TalkTalkException() {
        super();
    }

    public TalkTalkException(String message, Throwable cause) {
        super(message, cause);
    }

    public TalkTalkException(String message) {
        super(message);
    }

    public TalkTalkException(Throwable cause) {
        super(cause);
    }
 
    
    
}
