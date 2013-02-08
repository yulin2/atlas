package org.atlasapi.query.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.content.criteria.attribute.Attribute;

import com.google.common.base.Optional;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;

public final class AttributeLookupTree extends ConcurrentRadixTree<Optional<Attribute<?>>> {

    public AttributeLookupTree() {
        super(new DefaultCharArrayNodeFactory());
    }
    
    public Optional<Attribute<?>> attributeFor(String key) {
        checkNotNull(key, "can't lookup null key");
        if (key.isEmpty()) {
            return Optional.absent();
        }
        
        int matchLen = 0;
        int nodeMatchLen = 0;
        Node prev = null;
        Node node = root;

        while (matchLen < key.length() && node != null) {

            prev = node;
            nodeMatchLen = 0;
            CharSequence edgeChars = node.getIncomingEdge();
            while (nodeMatchLen < edgeChars.length() && matchLen < key.length()) {
                if (edgeChars.charAt(nodeMatchLen) != key.charAt(matchLen)) {
                    return processResult(key, node, matchLen, nodeMatchLen);
                }
                matchLen++;
                nodeMatchLen++;
            }
            
            node = matchLen < key.length() ? node.getOutgoingEdge(key.charAt(matchLen))
                                           : null;
        }
        
        return processResult(key, prev, matchLen, nodeMatchLen);
    }

    @SuppressWarnings("unchecked")
    private Optional<Attribute<?>> processResult(String key, Node curNode, int matchLen,
                                                 int curNodeMatch) {
        int incomingEdgeLength = curNode.getIncomingEdge().length();
        if (matchLen > 0 && matchLen <= key.length() && curNodeMatch == incomingEdgeLength) {
            return (Optional<Attribute<?>>) curNode.getValue();
        }
        return Optional.absent();
    }

    public void put(Attribute<?> attribute) {
        put(attribute.externalName(), Optional.<Attribute<?>>of(attribute));
    }
    
}