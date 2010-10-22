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
import java.util.Collections;
import java.util.List;

import org.atlasapi.remotesite.xml.SimpleXmlNavigator;
import org.jaxen.JaxenException;
import org.jdom.Document;
import org.jdom.Element;

import com.google.common.collect.Lists;

public class HtmlNavigator {
    
    private final SimpleXmlNavigator navigator;

	public HtmlNavigator(Document doc) {
		this.navigator = new SimpleXmlNavigator(doc);
	}

	public HtmlNavigator(Reader in) {
		this.navigator = new SimpleXmlNavigator(in);
	}
	
	public HtmlNavigator(String content) {
	    this.navigator = new SimpleXmlNavigator(content);
	}

	public Element firstElementOrNull(String xpath) {
		return navigator.firstElementOrNull(xpath);
	}

	public Element firstElementOrNull(String xpath, Object fromNode) {
	    return navigator.firstElementOrNull(xpath, fromNode);
	}

	public List<Element> allElementsMatching(String xpath) throws JaxenException {
		return navigator.allElementsMatching(xpath);
	}
	
	public List<Element> allElementsMatching(String xpath, Object fromNode) throws JaxenException {
		return navigator.allElementsMatching(xpath, fromNode);
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
