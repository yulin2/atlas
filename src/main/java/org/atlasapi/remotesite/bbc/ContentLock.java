package org.atlasapi.remotesite.bbc;

import java.util.Set;

import com.google.common.collect.Sets;

public class ContentLock {

    private Set<String> locked = Sets.newHashSet();
    
    public void lock(String id) throws InterruptedException {
        synchronized (locked) {
            while (locked.contains(id)) {
                locked.wait();
            }
            locked.add(id);
        }
    }
    
    public void unlock(String id) {
        synchronized (locked) {
            locked.remove(id);
            locked.notifyAll();
        }
    }
}
