package org.atlasapi.remotesite.thesun;

import nu.xom.Element;
import nu.xom.Node;


public class TheSunRssEnclosureElement extends Element {
    private static final String TOL_TEXT_NS = "http://www.timesonline.co.uk/rss-full-text";

    public TheSunRssEnclosureElement(String name) {
        super(name);
    }

    public TheSunRssEnclosureElement(Element element) {
        super(element);
    }    
    
    @Override
    public Node copy() {
        return new TheSunRssEnclosureElement(this);
    }

    public TheSunRssEnclosureElement(String name, String element) {
        super(name, element);
    }
    
    public String getCaption() {
        return this.getAttributeValue("caption");
    }
    
    public String getImageCredit() {
        return this.getAttributeValue("imagecredit", TOL_TEXT_NS);
    }
    
    public String getUrl() {
        return this.getAttributeValue("url");
    }
    
    public String getLength() {
        return this.getLength();
    }
    
    public String getType() {
        return this.getType();
    }

}
