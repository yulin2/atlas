/* Copyright 2009 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.remotesite.bbc;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.SiteSpecificAdapter;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesClip;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSameAs;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesTopic;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;
import org.atlasapi.remotesite.bbc.ion.BbcExtendedDataContentAdapter;
import org.atlasapi.remotesite.bbc.ion.IonService.MediaSetsToPoliciesFunction;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class BbcProgrammeAdapter implements SiteSpecificAdapter<Identified> {

    private final BbcSlashProgrammesRdfClient<SlashProgrammesRdf> episodeClient;
    private final ContentExtractor<BbcProgrammeSource, Item> itemExtractor;
    private final BbcBrandExtractor brandExtractor;
    
    private final BbcSlashProgrammesRdfClient<SlashProgrammesVersionRdf> versionClient;

    private final Log oldLog = LogFactory.getLog(getClass());

    private final BbcSlashProgrammesRdfClient<SlashProgrammesRdf> clipClient;

	private final ContentWriter writer;
    private final BbcSlashProgrammesRdfClient<SlashProgrammesRdf> topicClient;

    public BbcProgrammeAdapter(ContentWriter writer, BbcExtendedDataContentAdapter extendedDataAdapter, 
            MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction, AdapterLog log) {
        this(writer, 
                new BbcSlashProgrammesRdfClient<SlashProgrammesRdf>(SlashProgrammesRdf.class), 
                new BbcSlashProgrammesRdfClient<SlashProgrammesVersionRdf>(SlashProgrammesVersionRdf.class), 
                new BbcSlashProgrammesRdfClient<SlashProgrammesRdf>(SlashProgrammesRdf.class), 
                new BbcSlashProgrammesRdfClient<SlashProgrammesRdf>(SlashProgrammesRdf.class), 
                extendedDataAdapter, mediaSetsToPoliciesFunction, log);
    }

    public BbcProgrammeAdapter(ContentWriter writer, 
            BbcSlashProgrammesRdfClient<SlashProgrammesRdf> episodeClient, 
            BbcSlashProgrammesRdfClient<SlashProgrammesVersionRdf> versionClient, 
            BbcSlashProgrammesRdfClient<SlashProgrammesRdf> clipClient, 
            BbcSlashProgrammesRdfClient<SlashProgrammesRdf> topicClient, 
            BbcExtendedDataContentAdapter extendedDataAdapter, 
            MediaSetsToPoliciesFunction mediaSetsToPoliciesFunction, AdapterLog log) {
        this.writer = writer;
		this.versionClient = versionClient;
        this.episodeClient = episodeClient;
        this.clipClient = clipClient;
        this.topicClient = topicClient;
        BbcProgrammeGraphExtractor graphExtractor = 
                new BbcProgrammeGraphExtractor(extendedDataAdapter, mediaSetsToPoliciesFunction, log);
        this.itemExtractor = graphExtractor;
        this.brandExtractor = new BbcBrandExtractor(this, writer, graphExtractor, extendedDataAdapter, log);
    }

    public boolean canFetch(String uri) {
        return BbcFeeds.isACanonicalSlashProgrammesUri(uri);
    }

    public Identified fetch(String uri) {
    	return createOrUpdate(uri, null);
    }
    
    public Identified createOrUpdate(String uri, Brand parentBrand) {
    	if (!canFetch(uri)) {
    		throw new IllegalArgumentException("URI " + uri + " is not a canonical /programmes URI");
    	}
        try {
            SlashProgrammesRdf content = readSlashProgrammesDataForEpisode(uri);
            if (content == null) {
            	// Nothing to write
                return null;
            }
            if (content.clip() != null) {
                return null; //don't fetch clips as top-level content.
            }
            if (content.episode() != null) {
                return createOrUpdateTopLevelItem(uri, content);
            }
//            SlashProgrammesSeriesContainer rdfSeries = content.series();
//			if (rdfSeries != null) {
//            	return brandExtractor.writeSeries(rdfSeries, parentBrand);
//            }
            if (content.brand() != null) {
                return brandExtractor.writeBrand(content.brand());
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Identified createOrUpdateTopLevelItem(String uri, SlashProgrammesRdf content) {
		Item item = fetchItem(uri, content);
		if (item instanceof Episode) {
		    throw new IllegalArgumentException("Not top level: " + uri);
		}
		writer.createOrUpdate(item);
		return item;
	}
    
    Item fetchItem(String uri) {
    	SlashProgrammesRdf content = readSlashProgrammesDataForEpisode(uri);
        if (content == null) {
        	throw new IllegalArgumentException("No data for uri " + uri);
        }
        if (content.episode() == null) {
        	throw new IllegalArgumentException("URI not an ite, " + uri);
        }
        return fetchItem(uri, content);
    }

	private Item fetchItem(String uri, SlashProgrammesRdf content) {
		List<SlashProgrammesVersionRdf> versions = null;
		if (content.episode().versions() != null && !content.episode().versions().isEmpty()) {
		    versions = ImmutableList.copyOf(Iterables.transform(content.episode().versions(), new Function<SlashProgrammesVersion,SlashProgrammesVersionRdf>(){
				@Override
				public SlashProgrammesVersionRdf apply(SlashProgrammesVersion input) {
					return readSlashProgrammesDataForVersion(input);
				}}));
		}
		
		Set<SlashProgrammesClip> clipRefs = content.episode().clips();
		Set<BbcProgrammeSource.ClipAndVersion> clips = Sets.newHashSet();
		if (clipRefs != null && !clipRefs.isEmpty()) {
		    for (SlashProgrammesClip clipRef: clipRefs) {
		        SlashProgrammesRdf clip = readSlashProgrammesDataForClip(clipRef);
		        
		        SlashProgrammesVersionRdf clipVersion = null;
		        if (clip.clip().versions() != null && ! clip.clip().versions().isEmpty()) {
		            clipVersion = readSlashProgrammesDataForVersion(clip.clip().versions().get(0));
		        }
		        
		        clips.add(new BbcProgrammeSource.ClipAndVersion(clip, clipVersion));
		    }
		}
		
		Set<SlashProgrammesTopic> subjects = content.episode().subjects() != null ? content.episode().subjects() : ImmutableSet.<SlashProgrammesTopic>of();
		Set<SlashProgrammesTopic> people = content.episode().people() != null ? content.episode().people() : ImmutableSet.<SlashProgrammesTopic>of();
		Set<SlashProgrammesTopic> places = content.episode().places() != null ? content.episode().places() : ImmutableSet.<SlashProgrammesTopic>of();
		ImmutableMap.Builder<SlashProgrammesTopic,String> topicUriMap = ImmutableMap.builder();
		for (SlashProgrammesTopic topic : Iterables.concat(subjects, people, places)) {
            String resolvedTopicSameAs = resolveTopic(topic);
            if(resolvedTopicSameAs != null) {
                topicUriMap.put(topic, resolvedTopicSameAs);
            }
        }
		
		BbcProgrammeSource source = new BbcProgrammeSource(uri, uri, content, versions, clips, topicUriMap.build());
		Item item = itemExtractor.extract(source);
		return item;
	}

    private String resolveTopic(SlashProgrammesTopic topic) {
        String topicUri = "http://www.bbc.co.uk" + topic.resourceUri().substring(0, topic.resourceUri().indexOf("#"));
        SlashProgrammesRdf topicRdf = getSlashProgrammesDataForTopic(topicUri);
        
        if(topicRdf == null) {
            return null;
        }
        if (topicRdf.description() != null && topicRdf.description().getSameAs() != null) {
            for (SlashProgrammesSameAs sameAs : topicRdf.description().getSameAs()) {
                String resourceUri = sameAs.resourceUri();
                if(resourceUri.startsWith("http://dbpedia.org")) {
                    return resourceUri;
                }
            }
        }
        return null;
    }

    private SlashProgrammesRdf getSlashProgrammesDataForTopic(String topicUri) {
        try {
            return topicClient.get(topicUri + ".rdf");
        } catch (Exception e) {
            oldLog.warn(e);
            return null;
        }
    }

    SlashProgrammesVersionRdf readSlashProgrammesDataForVersion(SlashProgrammesVersion slashProgrammesVersion) {
        try {
            return versionClient.get(slashProgrammesUri(slashProgrammesVersion));
        } catch (Exception e) {
            oldLog.warn(e);
            return null;
        }
    }

    private SlashProgrammesRdf readSlashProgrammesDataForEpisode(String episodeUri) {
        try {
            return episodeClient.get(episodeUri + ".rdf");
        } catch (Exception e) {
            oldLog.warn(e);
            return null;
        }
    }
    
    SlashProgrammesRdf readSlashProgrammesDataForClip(SlashProgrammesClip slashProgrammesClip) {
        try {
            return clipClient.get(slashProgrammesUri(slashProgrammesClip));
        } catch (Exception e) {
            oldLog.warn(e);
            return null;
        }
    }

    private String slashProgrammesUri(SlashProgrammesVersion slashProgrammesVersion) {
        return "http://www.bbc.co.uk" + slashProgrammesVersion.resourceUri().replace("#programme", "") + ".rdf";
    }
    
    private String slashProgrammesUri(SlashProgrammesClip slashProgrammesClip) {
        return "http://www.bbc.co.uk" + slashProgrammesClip.resourceUri().replace("#programme", "") + ".rdf";
    }
}
