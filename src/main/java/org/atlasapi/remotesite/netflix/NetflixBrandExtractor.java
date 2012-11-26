package org.atlasapi.remotesite.netflix;

import java.util.Set;

import nu.xom.Element;

import org.atlasapi.media.entity.Brand;

import com.google.common.collect.ImmutableSet;

public class NetflixBrandExtractor extends NetflixContentExtractor<Brand> {

    public static final String BRAND_URL_PREFIX = "http://gb.netflix.com/shows/";

    @Override
    Set<Brand> extract(Element source, int id) {
        Brand brand = new Brand();

        brand.setCanonicalUri(BRAND_URL_PREFIX + id);

        brand.setTitle(getTitle(source));
        brand.setYear(getYear(source));
        brand.setDescription(getDescription(source));
        brand.setGenres(getGenres(source));
        brand.setPeople(getPeople(source));
        brand.setCertificates(getCertificates(source));
        brand.addAlias(getAlias(source));
        brand.setPublisher(getPublisher());

        return ImmutableSet.<Brand>builder().add(brand).build();
    }
}
