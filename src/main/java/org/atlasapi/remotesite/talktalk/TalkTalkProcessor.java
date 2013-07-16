package org.atlasapi.remotesite.talktalk;

/**
 * Parent interface for a processor which accumulates a result as it performs
 * its processing.
 * 
 * @param <R>
 */
interface TalkTalkProcessor<R> {
    
    R getResult();
    
}