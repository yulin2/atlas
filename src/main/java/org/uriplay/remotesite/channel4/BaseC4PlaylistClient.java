package org.uriplay.remotesite.channel4;

import java.util.List;
import java.util.Set;

import org.uriplay.genres.GenreMap;
import org.uriplay.media.entity.Brand;
import org.uriplay.media.entity.Item;
import org.uriplay.media.entity.Playlist;
import org.uriplay.persistence.system.RemoteSiteClient;
import org.uriplay.query.content.PerPublisherCurieExpander;
import org.uriplay.remotesite.FetchException;
import org.uriplay.remotesite.SiteSpecificAdapter;

import com.google.common.collect.Lists;

public abstract class BaseC4PlaylistClient implements SiteSpecificAdapter<Playlist> {

	private final RemoteSiteClient<BrandListingPage> brandListClient;
	private final SiteSpecificAdapter<Brand> brandClient;
	private final GenreMap genreMap = new C4GenreMap();

	public BaseC4PlaylistClient(RemoteSiteClient<BrandListingPage> brandListClient, SiteSpecificAdapter<Brand> brandClient) {
		this.brandListClient = brandListClient;
		this.brandClient = brandClient;
	}

	@Override
	public final Playlist fetch(String uri) {
		try {
			List<HtmlBrandSummary> brandList = Lists.newArrayList();
			
			BrandListingPage brandListingPage = brandListClient.get(uri);
			brandList.addAll(brandListingPage.getBrandList());

			while (brandListingPage.hasNextPageLink()) {
				brandListingPage = brandListClient.get(brandListingPage.getNextPageLink());
				brandList.addAll(brandListingPage.getBrandList());
			}
			
			Playlist playlist = new Playlist(uri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(uri));
			
			for (HtmlBrandSummary brandRef : brandList) {
				Brand brand = brandClient.fetch(brandRef.getBrandPage());
				if (brand != null) {
					if (brandRef.getCategories() != null) {
						Set<String> genres = genreMap.map(brandRef.getCategories());
						brand.setGenres(genres);
						// C4 only expose genres at the brand level
						// so we add them to the items
						for (Item item : brand.getItems()) {
							item.setGenres(genres);
						}
					}
					playlist.addPlaylist(brand);
				}
			}
			return playlist;
			
		} catch (Exception e) {
			throw new FetchException("Problem processing html page from: " + uri, e);
		}
	}

}
