/* Copyright 2009 British Broadcasting Corporation
   Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.uriplay.remotesite.bbc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.uriplay.media.vocabulary.PO;
import org.uriplay.media.vocabulary.RDF;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 */
@XmlRootElement(name="RDF", namespace=RDF.NS)
class SlashProgrammesVersionRdf {
	
	@XmlElement(namespace=PO.NS, name="Broadcast")
	List<BbcBroadcast> broadcasts;
	
	List<BbcBroadcast> broadcastSlots() {
		return broadcasts;
	}
	
	static class BbcBroadcast {
		
		@XmlElement(namespace="http://purl.org/NET/c4dm/event.owl#", name="time")
		Event event;

		@XmlElement(namespace=PO.NS, name="schedule_date")
		String scheduleDate;
		
		@XmlElement(namespace=PO.NS, name="broadcast_on")
		BroadcastOn broadcastOn;
		
		String broadcastTime() {
			 return event.interval.startTime;
		}

		public BbcBroadcast atTime(String time) {
			Interval interval =  new Interval();
			interval.startTime = time;
			return atTime(interval);
		}
		
		public BbcBroadcast atTime(Interval invterval) {
			this.event = new Event();
			this.event.interval = invterval;
			return this;
		}

		public BbcBroadcast onChannel(String channel) {
			broadcastOn = new BroadcastOn();
			broadcastOn.service = new Service();
			broadcastOn.service.resourceUri = channel;
			return this;
		}
		
		public DateTime broadcastDateTime() {
			return new DateTime(broadcastTime());
		}

		public String scheduleDate() {
			return scheduleDate;
		}

		public String broadcastOn() {
			if (broadcastOn != null && broadcastOn.service != null) {
				return broadcastOn.service.resourceUri;
			}
			return null;
		}

		public Integer broadcastDuration() {
			return (int) ((new DateTime(event.interval.endTime).getMillis() - new DateTime(event.interval.startTime).getMillis()) / 1000);
		}
		
		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

	}
	
	static class Event {
		
		@XmlElement(namespace="http://purl.org/NET/c4dm/timeline.owl#", name="Interval")
		Interval interval;

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}
	
	static class BroadcastOn {
		
	    @XmlElement(namespace=PO.NS, name="Service")
		Service service;
		
		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
		
	}
	
	static class Service {
	    @XmlAttribute(namespace=RDF.NS, name="about")
        String resourceUri;
	    
	    @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
	}
	
	static class Interval {
		
		@XmlElement(name="start", namespace="http://purl.org/NET/c4dm/timeline.owl#")
		String startTime;
		
		@XmlElement(name="end", namespace="http://purl.org/NET/c4dm/timeline.owl#")
		String endTime;
		
		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}
	}

	public SlashProgrammesVersionRdf withLastTransmitted(DateTime lastTx, String channel) {
		this.broadcasts = Lists.newArrayList(new BbcBroadcast().atTime(lastTx.toString()).onChannel(channel));
		return this;
	}

	public DateTime lastTransmitted() {
		
		if (broadcasts == null || broadcasts.isEmpty()) { return null; }
		
		Collections.sort(broadcasts, new ByTransmissionDateComparator());
		
		return Iterables.getLast(broadcasts).broadcastDateTime();
	}
	
	static class ByTransmissionDateComparator implements Comparator<BbcBroadcast> {

		public int compare(BbcBroadcast o1, BbcBroadcast o2) {
			return new DateTime(o1.event.interval.startTime).compareTo(new DateTime(o2.event.interval.startTime));
		}
	}
}
