package org.atlasapi.remotesite.itv.interlinking;

public enum InterlinkingType {
    BRAND("brand"),
    SERIES("series"),
    SUBSERIES("subseries"),
    EPISODE("episode"),
    ONDEMAND("ondemand"),
    BROADCAST("broadcast");
    
    private final String key;

    private InterlinkingType(String key) {
        this.key = key;
    }
    
    public static InterlinkingType fromKey(String key) {
        for (InterlinkingType type : InterlinkingType.values()) {
            if (type.key.equals(key)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No type found for key " + key);
    }
}