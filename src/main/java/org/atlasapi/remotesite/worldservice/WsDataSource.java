package org.atlasapi.remotesite.worldservice;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class WsDataSource implements Closeable {

    public static final WsDataSource sourceForFile(WsDataFile file, InputStream data) {
        return new WsDataSource(file, data);
    }

    private final WsDataFile file;
    private final InputStream data;

    public WsDataSource(WsDataFile file, InputStream data) {
        this.file = file;
        this.data = data;
    }

    public WsDataFile file() {
        return file;
    }
    
    public InputStream data() {
        return data;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof WsDataSource) {
            WsDataSource other = (WsDataSource) that;
            return this.file == other.file;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Data source for %s", file);
    }

    @Override
    public void close() throws IOException {
        data.close();
    }

}
