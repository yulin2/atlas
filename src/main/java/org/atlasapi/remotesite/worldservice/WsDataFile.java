package org.atlasapi.remotesite.worldservice;

public enum WsDataFile {

    AUDIO_ITEM("AudioItem.xml"),
    AUDIO_ITEM_PROG_LINK("AudioItemProgLink.xml"),
    GENRE("Genre.xml"),
    PROGRAMME("Programme.xml"),
    SERIES("Series.xml");
 
    private final String filename;

    WsDataFile(String filename) {
        this.filename = filename;
    }
    
    public String filename() {
        return filename;
    }
}
