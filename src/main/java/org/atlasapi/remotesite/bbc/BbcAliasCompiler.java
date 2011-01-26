package org.atlasapi.remotesite.bbc;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class BbcAliasCompiler {

    public static Set<String> bbcAliasUrisFor(String episodeUri) {
        String pid = BbcUriCanonicaliser.bbcProgrammeIdFrom(episodeUri);
        HashSet<String> aliases = Sets.newHashSet();
        if (pid != null) {
            aliases.add(String.format("http://www.bbc.co.uk/iplayer/episode/%s", pid));
            aliases.add(String.format("http://www.bbc.co.uk/programmes/%s", pid));
            aliases.add(String.format("http://bbc.co.uk/i/%s/", pid.replaceFirst("b00", "")));
            aliases.remove(episodeUri);
        }
        return aliases;
    }
    
}
