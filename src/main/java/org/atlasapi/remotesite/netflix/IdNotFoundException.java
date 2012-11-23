package org.atlasapi.remotesite.netflix;

import nu.xom.Element;

public class IdNotFoundException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private Element element;
    
    public IdNotFoundException(String message) {
        super(message);
    }
    
    public IdNotFoundException(String message, Element element) {
        this(message);
        this.element = element;
    }
    
    public IdNotFoundException(Element element) {
        this("Id attribute not found on xml element");
        this.element = element;
    }
    
    public Element getElement() {
        return element;
    }
}
