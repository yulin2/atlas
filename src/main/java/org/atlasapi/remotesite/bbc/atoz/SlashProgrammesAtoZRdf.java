package org.atlasapi.remotesite.bbc.atoz;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atlasapi.media.vocabulary.PO;
import org.atlasapi.media.vocabulary.RDF;

import com.google.inject.internal.Lists;

@XmlRootElement(name="RDF", namespace=RDF.NS)
public class SlashProgrammesAtoZRdf {

    @XmlElement(namespace=PO.NS, name="Programme")
    List<BbcProgramme> programmes;
    
    List<BbcProgramme> programmes() {
        return programmes;
    }
    
    public List<String> programmeIds() {
        List<String> pids = Lists.newArrayList();
        if (programmes != null) {
            for (BbcProgramme programme: programmes) {
                pids.add(programme.pid());
            }
        }
        return pids;
    }
    
    static class BbcProgramme {
        @XmlElement(namespace=PO.NS, name="pid")
        String pid;
        
        public String pid() {
            return pid;
        }
    }
}
