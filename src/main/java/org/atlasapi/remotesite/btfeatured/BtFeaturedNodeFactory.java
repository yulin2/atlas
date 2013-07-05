package org.atlasapi.remotesite.btfeatured;

import nu.xom.Element;
import nu.xom.NodeFactory;


public class BtFeaturedNodeFactory extends NodeFactory {

    private static final String PRODUCT = "product";

    @Override
    public Element startMakingElement(String name, String namespace) {
        if (PRODUCT.equalsIgnoreCase(name)) {
            return new BtFeaturedProductElement(name, namespace);
        }
        return super.startMakingElement(name, namespace);
    }
}
