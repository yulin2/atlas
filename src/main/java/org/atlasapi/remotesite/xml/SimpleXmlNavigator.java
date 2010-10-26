package org.atlasapi.remotesite.xml;

import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

public class SimpleXmlNavigator {
    
    private final Document doc;

    public SimpleXmlNavigator(Document doc) {
        this.doc = doc;
    }

    public SimpleXmlNavigator(Reader in) {
        try {
            this.doc = domFor(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public SimpleXmlNavigator(String content) {
        this(new StringReader(content));
    }

    private static Document domFor(Reader in) throws Exception {
        SAXBuilder builder = new SAXBuilder("org.ccil.cowan.tagsoup.Parser");
        Document doc = builder.build(in);
        removeNamespaces(doc);
        return doc;
    }

    @SuppressWarnings("unchecked")
    private static void removeNamespaces(Document doc) {
        Element root = doc.getRootElement();
        Iterator<Element> elements = root.getDescendants(new ElementFilter());
        while (elements.hasNext()) {
            Element element = elements.next();
            removeNamespace(element);
        }
        removeNamespace(root);
    }

    private static void removeNamespace(Element element) {
        if (element.getNamespace() != null) {
            element.setNamespace(null);
        }
    }

    public Element firstElementOrNull(String xpath) {
        return firstElementOrNull(xpath, doc);
    }

    public Element firstElementOrNull(String xpath, Object fromNode) {
        try {
            List<Element> nodes = allElementsMatching(xpath, fromNode);
            if (nodes == null || nodes.isEmpty()) {
                return null;
            } else {
                return nodes.get(0);
            }
        } catch (JaxenException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Element> allElementsMatching(String xpath) throws JaxenException {
        return allElementsMatching(xpath, doc);
    }
    
    @SuppressWarnings("unchecked")
    public List<Element> allElementsMatching(String xpath, Object fromNode) throws JaxenException {
        return new JDOMXPath(xpath).selectNodes(fromNode);
    }
}
