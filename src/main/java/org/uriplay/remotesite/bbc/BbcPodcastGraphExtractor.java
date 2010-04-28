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

import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jherd.beans.BeanGraphExtractor;
import org.jherd.beans.BeanGraphFactory;
import org.jherd.beans.Representation;
import org.jherd.beans.id.IdGenerator;
import org.jherd.beans.id.IdGeneratorFactory;
import org.springframework.beans.MutablePropertyValues;
import org.uriplay.media.entity.Brand;
import org.uriplay.remotesite.synd.GenericPodcastGraphExtractor;
import org.uriplay.remotesite.synd.SyndicationSource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * {@link BeanGraphExtractor} that processes the entries of a BBC Podcast RSS/Atom feed, 
 * and constructs a {@link Representation}, which may later be handled
 * by the {@link BeanGraphFactory}.
 *
 * @author Robert Chatley (robert@metabroadcast.com)
 */
public class BbcPodcastGraphExtractor extends GenericPodcastGraphExtractor implements BeanGraphExtractor<SyndicationSource> {

	public BbcPodcastGraphExtractor(IdGeneratorFactory idGeneratorFactory) {
		super(idGeneratorFactory);
	}

	@Override
	protected String addAndReturnUriOf(Representation representation, IdGenerator idGenerator, SyndEntry entry) {
		String uri = removeExtensionFrom(entry.getLink());
		representation.addUri(uri);
		return uri;
	}
	
	private String removeExtensionFrom(String url) {
		int index = url.lastIndexOf('.');
		if (index < 0) {
			return url;
		} else {
			return url.substring(0, index);
		}
	}

	@Override
	protected String publisher() {
		return "bbc.co.uk";
	}
	
	@Override
	public Representation extractFrom(SyndicationSource source) {
		Representation representation = super.extractFrom(source);
		
		MutablePropertyValues mpvs = new MutablePropertyValues();
		mpvs.addPropertyValue("publisher", BbcProgrammeGraphExtractor.BBC_PUBLISHER);
		representation.addValues(source.getUri(), mpvs);

//		Don't include a /programmes link this creates duplicate aliases
//		TODO: Reinstate this code
//		String slashProgrammesLink = slashProgrammesLinkFor(source.getFeed());
//		if (slashProgrammesLink != null) {
//			representation.addAliasFor(source.getUri(), slashProgrammesLink);
//		}
		
		return representation;
	}


	@Override
	protected MutablePropertyValues extractCollectionPropertyValuesFrom(Set<String> episodes, SyndFeed feed, String feedUri) {
		MutablePropertyValues mpvs = super.extractCollectionPropertyValuesFrom(episodes, feed, feedUri);
		mpvs.addPropertyValue("uri", feedUri);

		return mpvs;
	}

	@SuppressWarnings("unchecked")
	private String slashProgrammesLinkFor(SyndFeed feed) {
		
		List<Element> foreignMarkup = (List<Element>) feed.getForeignMarkup();
		
		for (Element element : foreignMarkup) {
			if (element.getName().equals("systemRef") &&
				element.getNamespacePrefix().equals("ppg") &&
				element.getAttributeValue("systemId").equals("pid.brand")) {
				
				String pid = element.getAttributeValue("key");
				
				return String.format("http://www.bbc.co.uk/programmes/%s", pid);
			}
		}
	
		return null;
	}

	@Override
	protected Class<?> collectionType() {
		return Brand.class;
	}
	
}
