package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.media.entity.ChannelSchedule;
import org.atlasapi.media.entity.simple.ScheduleQueryResult;
import org.atlasapi.output.simple.ChannelSimplifier;
import org.atlasapi.media.entity.simple.Channel;
import org.atlasapi.media.entity.simple.ScheduleChannel;
import org.atlasapi.output.simple.ItemModelSimplifier;

import com.google.common.collect.ImmutableList;

/**
 * {@link AtlasModelWriter} that translates the full URIplay object model
 * into a simplified form and renders that as XML.
 *  
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@Deprecated
public class SimpleScheduleModelWriter extends TransformingModelWriter<ChannelSchedule, ScheduleChannel> {

    private final ItemModelSimplifier itemModelSimplifier;
    private final ChannelSimplifier channelSimplifier;

	public SimpleScheduleModelWriter(AtlasModelWriter<ScheduleChannel> outputter, ItemModelSimplifier itemModelSimplifier, ChannelSimplifier channelSimplifier) {
		super(outputter);
        this.itemModelSimplifier = itemModelSimplifier;
        this.channelSimplifier = channelSimplifier;
	}
	
	@Override
    protected ScheduleChannel transform(ChannelSchedule channelSchedule, Set<Annotation> annotations, ApplicationConfiguration config) {
	    return scheduleChannelFrom(channelSchedule, annotations, config);
	}

	org.atlasapi.media.entity.simple.ScheduleChannel scheduleChannelFrom(ChannelSchedule scheduleChannel, Set<Annotation> annotations, ApplicationConfiguration config) {
	    org.atlasapi.media.entity.simple.ScheduleChannel newScheduleChannel = new org.atlasapi.media.entity.simple.ScheduleChannel();
	    
	    if (annotations.contains(Annotation.CHANNEL)) {
	        newScheduleChannel.setChannel(channelSimplifier.simplify(scheduleChannel.channel(), false, false, false));
	    }
	    
	    ImmutableList.Builder<org.atlasapi.media.entity.simple.Item> items = ImmutableList.builder();
	    for (org.atlasapi.media.entity.Item item: scheduleChannel.items()) {
	        items.add(itemModelSimplifier.simplify(item, annotations, config));
	    }
	    
	    newScheduleChannel.setItems(items.build());
	    return newScheduleChannel;
	}

}
