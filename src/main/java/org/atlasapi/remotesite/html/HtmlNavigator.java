/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.html;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jaxen.JaxenException;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Lists;

/**
 * XPath-based navigator for finding elements in an HTML document.
 * Uses tagsoup parser to make the document more well-formed.
 *  
 * @author John Ayres (john@metabroadcast.com)
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class HtmlNavigator {

	private final Document doc;

	public HtmlNavigator(Document doc) {
		this.doc = doc;
	}

	public HtmlNavigator(Reader in) {
		try {
			this.doc = domFor(in);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public HtmlNavigator(String content) {
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
	
	public String metaTagContents(String metaTagName) {
		Element elem = firstElementOrNull("/html/head/meta[@name='" + metaTagName + "']");
		if (elem == null) {
			return null;
		} else {
			return elem.getAttributeValue("content");
		}
	}
	
	public String titleTagContents() {
		Element elem = firstElementOrNull("/html/head/title");
		if (elem == null) {
			return null;
		} else {
			return elem.getText();
		}
	}
	
	public String linkTarget(String rel) {
		Element elem = firstElementOrNull("//link[@rel='" + rel + "']");
		if (elem == null) {
			return null;
		} else {
			return elem.getAttributeValue("href");
		}
	}

	public List<String> optionValuesWithinSelect(String selectId) {
		
		Element selectElem = firstElementOrNull("//select[@id='" + selectId + "']");
		List<Element> optionElems;

		try {
			optionElems = allElementsMatching("//option", selectElem);
		} catch (JaxenException e) {
			return Collections.emptyList();
		}
		
		List<String> uris = Lists.newArrayList();
		for (Element option : optionElems) {
			String value = option.getAttributeValue("value");
			if (value != null) {
				uris.add(value);
			}
		}
		
		return uris;
	}
}
