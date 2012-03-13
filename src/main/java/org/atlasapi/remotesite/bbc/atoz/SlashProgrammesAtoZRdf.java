package org.atlasapi.remotesite.bbc.atoz;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.atlasapi.media.vocabulary.DC;
import org.atlasapi.media.vocabulary.PO;
import org.atlasapi.media.vocabulary.RDF;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

@XmlRootElement(name="RDF", namespace=RDF.NS)
public class SlashProgrammesAtoZRdf {

    @XmlElement(namespace=PO.NS, name="Programme")
    List<BbcProgramme> programmes;
    
    List<BbcProgramme> programmes() {
        return programmes;
    }
    
    public List<String> programmeIds() {
        return ImmutableList.copyOf(Iterables.transform(programmes, new Function<BbcProgramme, String>() {

            @Override
            public String apply(BbcProgramme input) {
                return input.pid();
            }
        }));
    }
    
    static class BbcProgramme {
        @XmlElement(namespace=PO.NS, name="pid")
        String pid;
        
        @XmlElement(namespace=DC.NS, name="title")
        String title;
        
        public String pid() {
            return pid;
        }
        
        public String title() {
            return title;
        }
    }
}
