package org.atlasapi.remotesite.thesun;

import nu.xom.Element;
import nu.xom.NodeFactory;


public class TheSunTvPicksElementFactory extends NodeFactory {
    @Override
    public Element startMakingElement(String name, String namespace) {
        if (name.equals("item")) {
            return new TheSunRssItemElement(name, namespace);
        }
        if (name.equals("enclosure") || name.equals("tol-text:enclosure")) {
            return new TheSunRssEnclosureElement(name, namespace);
        }
        if (name.equals("tol-text:ooyalaVideo")) {
            return new TheSunRssOoyalaVideo(name, namespace);
        }
        return super.startMakingElement(name, namespace);
    }
}
