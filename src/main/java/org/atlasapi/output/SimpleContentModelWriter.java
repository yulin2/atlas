package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.output.simple.ContainerModelSimplifier;
import org.atlasapi.output.simple.ContentGroupModelSimplifier;
import org.atlasapi.output.simple.ItemModelSimplifier;

/**
 * {@link AtlasModelWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SimpleContentModelWriter extends TransformingModelWriter<Iterable<Content>, ContentQueryResult> {

    private final ItemModelSimplifier itemModelSimplifier;
    private final ContainerModelSimplifier containerModelSimplifier;
    private final ContentGroupModelSimplifier contentGroupSimplifier;

	public SimpleContentModelWriter(AtlasModelWriter<ContentQueryResult> outputter, ItemModelSimplifier itemModelSimplifier, ContainerModelSimplifier containerModelSimplifier) {
	    super(outputter);
	    this.itemModelSimplifier = itemModelSimplifier;
		this.containerModelSimplifier = containerModelSimplifier;
		this.contentGroupSimplifier = new ContentGroupModelSimplifier();
	}
	
	@Override
	protected ContentQueryResult transform(Iterable<Content> fullGraph, Set<Annotation> annotations) {
	    ContentQueryResult outputGraph = new ContentQueryResult();
		for (Described described : fullGraph) {
			if (described instanceof Container) {
				outputGraph.add(containerModelSimplifier.simplify((Container) described, annotations));
			}
			if (described instanceof ContentGroup) {
				outputGraph.add(contentGroupSimplifier.simplify((ContentGroup) described, annotations));
			}
			if (described instanceof org.atlasapi.media.entity.Item) {
				outputGraph.add(itemModelSimplifier.simplify((org.atlasapi.media.entity.Item) described, annotations));
			}
		}
		return outputGraph;
	}

}
