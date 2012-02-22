package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.media.entity.Container;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.ContentGroup;
import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.media.entity.simple.ContentQueryResult;
import org.atlasapi.output.simple.ContainerModelSimplifier;
import org.atlasapi.output.simple.ContentGroupModelSimplifier;
import org.atlasapi.output.simple.ItemModelSimplifier;
import org.atlasapi.output.simple.TopicModelSimplifier;

import com.google.common.base.Optional;

/**
 * {@link AtlasModelWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SimpleContentModelWriter extends TransformingModelWriter<QueryResult<Content,? extends Identified>, ContentQueryResult> {

    private final ItemModelSimplifier itemModelSimplifier;
    private final ContainerModelSimplifier containerModelSimplifier;
    private final ContentGroupModelSimplifier contentGroupSimplifier;
    private final TopicModelSimplifier topicSimplifier;

	public SimpleContentModelWriter(AtlasModelWriter<ContentQueryResult> outputter, ItemModelSimplifier itemModelSimplifier, ContainerModelSimplifier containerModelSimplifier, TopicModelSimplifier topicSimplifier) {
	    super(outputter);
	    this.itemModelSimplifier = itemModelSimplifier;
		this.containerModelSimplifier = containerModelSimplifier;
        this.topicSimplifier = topicSimplifier;
		this.contentGroupSimplifier = new ContentGroupModelSimplifier();
	}
	
	@Override
	protected ContentQueryResult transform(QueryResult<Content,? extends Identified> fullGraph, Set<Annotation> annotations) {
	    ContentQueryResult outputGraph = new ContentQueryResult();
		for (Described described : fullGraph.getContent()) {
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
		
		Optional<? extends Identified> possibleContext = fullGraph.getContext();
        if(possibleContext.isPresent()) {
		     Identified context = possibleContext.get();
		     if (context instanceof Topic) {
                Topic topic = (Topic) context;
                outputGraph.setContext(topicSimplifier.simplify(topic, annotations));
            }
		}
		
		return outputGraph;
	}

}
