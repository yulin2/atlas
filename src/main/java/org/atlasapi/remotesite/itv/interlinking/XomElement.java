package org.atlasapi.remotesite.itv.interlinking;

import nu.xom.Element;

import com.google.common.base.Strings;
import com.metabroadcast.common.base.Maybe;

public class XomElement {
    
    public static Maybe<String> getElemValue(Element baseElem, String elemName, String ns) {
        Element valueElement = baseElem.getFirstChildElement(elemName, ns);
        if (valueElement != null) {
            return Maybe.fromPossibleNullValue(Strings.emptyToNull(valueElement.getValue()));
        }
        return Maybe.nothing();
    }
    
    public static Maybe<String> getAttrValue(Element baseElem, String elemName, String elemNs, String attrName) {
        Element valueElement = baseElem.getFirstChildElement(elemName, elemNs);
        if (valueElement != null) {
            return Maybe.fromPossibleNullValue(valueElement.getAttributeValue(attrName));
        }
        return Maybe.nothing();
    }
    
    public static String requireElemValue(Element baseElem, String elemName, String ns) {
        return getElemValue(baseElem, elemName, ns).requireValue();
    }
}
