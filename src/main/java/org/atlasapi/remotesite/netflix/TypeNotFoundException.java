package org.atlasapi.remotesite.netflix;

import nu.xom.Element;

public class TypeNotFoundException extends Exception {
/**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Element element;
    
    public TypeNotFoundException(String message) {
        super(message);
    }
    
    public TypeNotFoundException(String message, Element element) {
        this(message);
        this.element = element;
    }
    
    public TypeNotFoundException(Element element) {
        this("Type attribute not found on xml element");
        this.element = element;
    }
    
    public Element getElement() {
        return element;
    }
}
