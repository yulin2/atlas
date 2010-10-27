package org.atlasapi.remotesite.channel4;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.ContentType;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.query.content.PerPublisherCurieExpander;
import org.atlasapi.remotesite.ContentExtractor;
import org.joda.time.DateTime;

import com.google.common.base.Preconditions;
import com.metabroadcast.common.time.DateTimeZones;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;

public class C4BrandBasicDetailsExtractor implements ContentExtractor<Feed, Brand> {

	@Override
	public Brand extract(Feed source) {
		
		Preconditions.checkArgument(C4AtomApi.isABrandFeed(source), "Not a brand feed");
		
		String brandUri = brandUriFrom(source);
		
		Preconditions.checkArgument(brandUri != null && C4AtomApi.isACanonicalBrandUri(brandUri), "URI of feed is not a canonical Brand URI, got: " + brandUri);

		Brand brand = new Brand(brandUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(brandUri), Publisher.C4);

		brand.addAlias(brandUri + "/4od");
		brand.addAlias(source.getId());

		brand.setTitle(source.getTitle());
		brand.setDescription(source.getSubtitle().getValue());
		
		brand.setLastUpdated(new DateTime(source.getUpdated(), DateTimeZones.UTC));
		brand.setContentType(ContentType.VIDEO);
		
		C4AtomApi.addImages(brand, source.getLogo());
		
		return brand;
	}

	private String brandUriFrom(Feed source) {
		if (source.getAlternateLinks().isEmpty()) {
			return null;
		}
		return ((Link) source.getAlternateLinks().get(0)).getHref();
	}
}
