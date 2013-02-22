package org.atlasapi.remotesite.bbc;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChannelAndPid {

    private final String channel;
    private final String pid;

    public ChannelAndPid(String channel, String pid) {
        this.channel = checkNotNull(channel);
        this.pid = checkNotNull(pid);
    }

    public String channel() {
        return this.channel;
    }

    public String pid() {
        return this.pid;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof ChannelAndPid) {
            ChannelAndPid other = (ChannelAndPid) that;
            return pid.equals(other.pid);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return pid.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("%s %s", channel, pid);
    }
}
