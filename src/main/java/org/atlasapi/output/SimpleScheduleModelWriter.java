package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.media.entity.Schedule.ScheduleChannel;
import org.atlasapi.media.entity.simple.ScheduleQueryResult;
import org.atlasapi.output.simple.ItemModelSimplifier;
import org.atlasapi.persistence.content.ContentResolver;

import com.google.common.collect.ImmutableList;

/**
 * {@link AtlasModelWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class SimpleScheduleModelWriter extends TransformingModelWriter<Iterable<ScheduleChannel>, ScheduleQueryResult> {

    private final ContentResolver contentResolver;
    private final ItemModelSimplifier itemModelSimplifier;

	public SimpleScheduleModelWriter(AtlasModelWriter<ScheduleQueryResult> outputter, ContentResolver contentResolver) {
		super(outputter);
        this.contentResolver = contentResolver;
        this.itemModelSimplifier = new ItemModelSimplifier();
	}
	
	@Override
    protected ScheduleQueryResult transform(Iterable<ScheduleChannel> fullGraph, Set<Annotation> annotations) {
        ScheduleQueryResult outputGraph = new ScheduleQueryResult();
	    for (ScheduleChannel scheduleChannel : fullGraph) {
	        outputGraph.add(scheduleChannelFrom(scheduleChannel, annotations));
	    }
	    return outputGraph;
	}

	org.atlasapi.media.entity.simple.ScheduleChannel scheduleChannelFrom(ScheduleChannel scheduleChannel, Set<Annotation> annotations) {
	    org.atlasapi.media.entity.simple.ScheduleChannel newScheduleChannel = new org.atlasapi.media.entity.simple.ScheduleChannel();
	    newScheduleChannel.setChannelUri(scheduleChannel.channel().uri());
	    newScheduleChannel.setChannelKey(scheduleChannel.channel().key());
	    newScheduleChannel.setChannelTitle(scheduleChannel.channel().title());
	    
	    ImmutableList.Builder<org.atlasapi.media.entity.simple.Item> items = ImmutableList.builder();
	    for (org.atlasapi.media.entity.Item item: scheduleChannel.items()) {
	        items.add(itemModelSimplifier.simplify(item, annotations));
	    }
	    
	    newScheduleChannel.setItems(items.build());
	    return newScheduleChannel;
	}

}
