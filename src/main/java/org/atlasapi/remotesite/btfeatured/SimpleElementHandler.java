package org.atlasapi.remotesite.btfeatured;

import javax.annotation.Nonnull;

import nu.xom.Element;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;

import com.google.common.base.Optional;

/**
 * Interface defining the object that must handle product elements for BT Featured content
 * @author andrewtoone
 *
 */
public interface SimpleElementHandler {

    /**
     * Handle the element, which may be either a top level product, collection or series, or
     * a child product. If this object is a collection or series, return the id so that it 
     * may be fully retrieved later. Otherwise return null.
     * @param element The element to be processed
     * @param parent The parent element, may be null
     * @return The id of a product to be expanded, or absent
     */
    public Optional<Content> handle(Element element, @Nonnull Optional<Container> parent);

}
