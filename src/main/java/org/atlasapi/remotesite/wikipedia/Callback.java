package org.atlasapi.remotesite.wikipedia;

public interface Callback<T> {
    /** As in "here, have this thing". */
    void have(T thing);
}
