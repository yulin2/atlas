package org.atlasapi.query.common;

import static org.junit.Assert.*;

import org.atlasapi.content.criteria.attribute.Attributes;
import org.junit.Test;


public class AttributeLookupTreeTest {

    @Test
    public void test() {
        
        AttributeLookupTree tree = new AttributeLookupTree();
        
        tree.put(Attributes.ALIASES_NAMESPACE);
        
        assertFalse(tree.attributeFor("").isPresent());
        assertFalse(tree.attributeFor("w").isPresent());
        assertFalse(tree.attributeFor("aliases").isPresent());
        assertFalse(tree.attributeFor("aliases.value").isPresent());
        assertTrue(tree.attributeFor("aliases.namespace").isPresent());
        assertTrue(tree.attributeFor("aliases.namespace.beginning").isPresent());
        assertTrue(tree.attributeFor("aliases.namespace.equals").isPresent());
        
        tree.put(Attributes.ALIASES_VALUE);
        
        assertFalse(tree.attributeFor("").isPresent());
        assertFalse(tree.attributeFor("w").isPresent());
        assertFalse(tree.attributeFor("aliases").isPresent());
        assertFalse(tree.attributeFor("aliases.valu").isPresent());
        assertFalse(tree.attributeFor("waliases.value").isPresent());
        assertTrue(tree.attributeFor("aliases.value").isPresent());
        assertTrue(tree.attributeFor("aliases.namespace").isPresent());
        assertTrue(tree.attributeFor("aliases.namespace.beginning").isPresent());
        assertTrue(tree.attributeFor("aliases.namespace.equals").isPresent());
        
    }

}
