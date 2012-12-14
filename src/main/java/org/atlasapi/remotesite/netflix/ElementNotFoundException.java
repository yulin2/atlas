package org.atlasapi.remotesite.netflix;

import nu.xom.Element;

public class ElementNotFoundException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Element element;
    private String elementName;
    
    public ElementNotFoundException(String message) {
        super(message);
    }

    public ElementNotFoundException(Element element, String elementName) {
        this("Child node with the elementName " + elementName + " was not found on this xml element: " + element.toString());
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
