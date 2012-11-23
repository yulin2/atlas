package org.atlasapi.remotesite.netflix;

import nu.xom.Element;

public class ElementNotFoundException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Element element;
    private String elementName;
    
    public ElementNotFoundException(String message) {
        super(message);
    }
    
    public ElementNotFoundException(String message, Element element, String elementName) {
        this(message);
        this.element = element;
        this.elementName = elementName;
    }

    public ElementNotFoundException(Element element, String elementName) {
        this("Child node with the specified elementName was not found on this xml element");
        this.element = element;
        this.elementName = elementName;
    }
    
    public Element getElement() {
        return element;
    }
    
    public String getElementName() {
        return elementName;
    }
}
