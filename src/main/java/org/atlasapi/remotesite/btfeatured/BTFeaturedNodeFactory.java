package org.atlasapi.remotesite.btfeatured;

import nu.xom.Element;
import nu.xom.NodeFactory;


public class BTFeaturedNodeFactory extends NodeFactory {

    private static final String PRODUCT = "product";

    @Override
    public Element startMakingElement(String name, String namespace) {
        if (PRODUCT.equalsIgnoreCase(name)) {
            return new BTFeaturedProductElement(name, namespace);
        }
        return super.startMakingElement(name, namespace);
    }
}
