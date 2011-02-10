package org.atlasapi.remotesite.channel4.epg;

import nu.xom.Element;
import nu.xom.NodeFactory;

public class C4EpgElementFactory extends NodeFactory {

    @Override
    public Element startMakingElement(String name, String namespace) {
        if(name.equals("entry")) {
            return new C4EpgEntryElement(name, namespace);
        }
        if(name.equals("media:group")) {
            return new C4MediaGroupElement(name, namespace);
        }
        if(name.equals("media:content")) {
            return new C4MediaContentElement(name, namespace);
        }
        return super.startMakingElement(name, namespace);
    }
    
}
