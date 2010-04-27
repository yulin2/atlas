package org.uriplay.remotesite.channel4;

import java.util.List;

import org.jherd.remotesite.FetchException;
import org.jherd.remotesite.SiteSpecificAdapter;
import org.jherd.remotesite.http.RemoteSiteClient;
import org.jherd.remotesite.timing.RequestTimer;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Playlist;

import com.google.common.collect.Lists;

public abstract class BaseC4PlaylistClient implements SiteSpecificAdapter<Playlist> {

	
	private final RemoteSiteClient<BrandListingPage> brandListClient;
	private final SiteSpecificAdapter<Brand> brandClient;

	public BaseC4PlaylistClient(RemoteSiteClient<BrandListingPage> brandListClient, SiteSpecificAdapter<Brand> brandClient) {
		this.brandListClient = brandListClient;
		this.brandClient = brandClient;
	}

	@Override
	public final Playlist fetch(String uri, RequestTimer timer) {
		try {
			List<HtmlBrandSummary> brandList = Lists.newArrayList();
			
			BrandListingPage brandListingPage = brandListClient.get(uri);
			brandList.addAll(brandListingPage.getBrandList());

			while (brandListingPage.hasNextPageLink()) {
				brandListingPage = brandListClient.get(brandListingPage.getNextPageLink());
				brandList.addAll(brandListingPage.getBrandList());
			}
			
			Playlist playlist = new Playlist();
			playlist.setCanonicalUri(uri);
			
			for (HtmlBrandSummary brandRef : brandList) {
				playlist.addPlaylist(brandClient.fetch(brandRef.getBrandPage(), timer));
			}
			return playlist;
			
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from: " + uri, e);
		}
	}
}
