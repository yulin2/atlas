package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;


public class RoviConstants {

    // Languages
    public static final String ENGLISH_LANG = "en";
    
    public static final Splitter LINE_SPLITTER = Splitter.on("|");
    public static final Charset FILE_CHARSET = Charsets.UTF_16LE;
    
}
