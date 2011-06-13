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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Series;
import org.atlasapi.persistence.content.ContentWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesClip;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class BbcProgrammeAdapter  {

    static final Pattern SLASH_PROGRAMMES_URL_PATTERN = Pattern.compile("^http://www\\.bbc\\.co\\.uk/programmes/([pb]00[^/\\.]+)$");

    private final BbcSlashProgrammesEpisodeRdfClient episodeClient;
    private final ContentExtractor<BbcProgrammeSource, Item> itemExtractor;
    private final BbcBrandExtractor brandExtractor;
    
    private final BbcSlashProgrammesVersionRdfClient versionClient;

    private final Log oldLog = LogFactory.getLog(getClass());

    private final BbcSlashProgrammesClipRdfClient clipClient;

	private final ContentWriter writer;

    public BbcProgrammeAdapter(ContentWriter writer, AdapterLog log) {
        this(writer, new BbcSlashProgrammesEpisodeRdfClient(), new BbcSlashProgrammesVersionRdfClient(), new BbcSlashProgrammesClipRdfClient(), new BbcProgrammeGraphExtractor(log), log);
    }

    public BbcProgrammeAdapter(ContentWriter writer, BbcSlashProgrammesEpisodeRdfClient episodeClient, BbcSlashProgrammesVersionRdfClient versionClient, BbcSlashProgrammesClipRdfClient clipClient, ContentExtractor<BbcProgrammeSource, Item> propertyExtractor, AdapterLog log) {
        this.writer = writer;
		this.versionClient = versionClient;
        this.episodeClient = episodeClient;
        this.clipClient = clipClient;
        this.itemExtractor = propertyExtractor;
        this.brandExtractor = new BbcBrandExtractor(this, writer, log);
    }

    public boolean canFetch(String uri) {
        Matcher matcher = SLASH_PROGRAMMES_URL_PATTERN.matcher(uri);
        return matcher.matches();
    }

    public Identified createOrUpdate(String uri) {
    	return createOrUpdate(uri, null);
    }
    
    public Identified createOrUpdate(String uri, Brand parentBrand) {
    	if (!canFetch(uri)) {
    		throw new IllegalArgumentException("URI " + uri + " does not match acceptable URI pattern " + SLASH_PROGRAMMES_URL_PATTERN);
    	}
        try {
            SlashProgrammesRdf content = readSlashProgrammesDataForEpisode(uri);
            if (content == null) {
            	// Nothing to write
                return null;
            }
            if (content.episode() != null) {
                return createOrUpdateTopLevelItem(uri, content);
            }
            SlashProgrammesSeriesContainer rdfSeries = content.series();
			if (rdfSeries != null) {
            	return brandExtractor.writeSeries(rdfSeries, parentBrand);
            }
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
		
		BbcProgrammeSource source = new BbcProgrammeSource(uri, uri, content, versions, clips);
		Item item = itemExtractor.extract(source);
		return item;
	}

    private SlashProgrammesVersionRdf readSlashProgrammesDataForVersion(SlashProgrammesVersion slashProgrammesVersion) {
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
    
    private SlashProgrammesRdf readSlashProgrammesDataForClip(SlashProgrammesClip slashProgrammesClip) {
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
