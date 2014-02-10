package org.atlasapi.remotesite.rovi;

import java.nio.charset.Charset;

import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;


public class RoviConstants {

    public static final String ENGLISH_LANG = "en";
    
    public static final Splitter LINE_SPLITTER = Splitter.on("|");
    public static final Charset FILE_CHARSET = Charsets.UTF_16LE;
    public static final Publisher DEFAULT_PUBLISHER = Publisher.ROVI_EN_GB;
    
}
