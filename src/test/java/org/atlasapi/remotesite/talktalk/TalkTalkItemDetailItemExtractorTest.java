package org.atlasapi.remotesite.talktalk;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import javax.xml.datatype.DatatypeFactory;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.media.entity.Encoding;
import org.atlasapi.media.entity.Episode;
import org.atlasapi.media.entity.Film;
import org.atlasapi.media.entity.Item;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.ParentRef;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Series;
import org.atlasapi.media.entity.Version;
import org.atlasapi.remotesite.talktalk.vod.bindings.AvailabilityType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ChannelType;
import org.atlasapi.remotesite.talktalk.vod.bindings.GenreListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.GenreType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ImageListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ImageType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemDetailType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ItemTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.ProductTypeType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisListType;
import org.atlasapi.remotesite.talktalk.vod.bindings.SynopsisType;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.time.DateTimeZones;


public class TalkTalkItemDetailItemExtractorTest {
    
    private final TalkTalkItemDetailItemExtractor extractor = new TalkTalkItemDetailItemExtractor();

    @Test
    public void testItemIsExtractedWhenBrandAndSeriesAreAbsent() {
        
        Item extracted = extractor.extract(detail(), Optional.<Brand>absent(), Optional.<Series>absent());
        
        assertThat(extracted, is(Item.class));
        assertThat(extracted, is(not(instanceOf(Episode.class))));
    }

    private ItemDetailType detail() {
        ItemDetailType detail = new ItemDetailType();
        detail.setItemType(ItemTypeType.EPISODE);
        return detail;
    }
    

    @Test
    public void testEpisodeIsExtractedWhenBrandIsPresent() {
        Brand brand = new Brand("brand", "brand", Publisher.TALK_TALK);
        Optional<Brand> extractedBrand = Optional.of(brand); 
        Item extracted = extractor.extract(detail(), extractedBrand, Optional.<Series>absent());
        assertThat(extracted, is(Episode.class));
        assertThat(extracted.getContainer(), is(ParentRef.parentRefFrom(brand)));
    }
    
    @Test
    public void testEpisodeIsExtractedWhenSeriesIsPresent() {
        Series series = new Series("series", "series", Publisher.TALK_TALK);
        Optional<Series> extractedSeries = Optional.of(series); 
        Item extracted = extractor.extract(detail(), Optional.<Brand>absent(), extractedSeries);
        assertThat(extracted, is(Episode.class));
        assertThat(extracted.getContainer(), is(ParentRef.parentRefFrom(series)));
        assertThat(((Episode)extracted).getSeriesRef(), is(ParentRef.parentRefFrom(series)));
    }
 
    @Test
    public void testFilmIsExtractedWhenFilmChannelGenreIsPresent() {
        ItemDetailType detail = detail();
        
        ChannelType channel = new ChannelType();
        GenreType genre = new GenreType();
        genre.setGenreCode("CHMOVIES");
        GenreListType genreList = new GenreListType();
        genreList.getGenre().add(genre);
        channel.setChannelGenreList(genreList);
        
        detail.setChannel(channel);
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        assertThat(extracted, is(instanceOf(Film.class)));
    }
    
    @Test
    public void testCanonicalUriIsExtracted() {
        ItemDetailType detail = detail();
        detail.setId("id");
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        assertThat(extracted.getCanonicalUri(), is("http://talktalk.net/episodes/id"));
    }

    @Test
    public void testPublisherIsTalkTalk() {
        Item extracted = extractor.extract(detail(), Optional.<Brand>absent(), Optional.<Series>absent());
        assertThat(extracted.getPublisher(), is(Publisher.TALK_TALK));
    }
    
    @Test
    public void testTitleIsExtracted() {
        ItemDetailType detail = detail();
        detail.setTitle("title");
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        assertThat(extracted.getTitle(), is("title"));
    }

    @Test
    public void testTitleWithEpisodeNumberIsExtractedWithoutEpisodeNumber() {
        ItemDetailType detail = detail();
        detail.setTitle("Ep2: title");
        
        Brand brand = new Brand("brand", "brand", Publisher.TALK_TALK);
        Optional<Brand> extractedBrand = Optional.of(brand); 
        Item extracted = extractor.extract(detail, extractedBrand, Optional.<Series>absent());
        Episode episode = (Episode) extracted;
        
        assertThat(episode.getTitle(), is("title"));
        assertThat(episode.getEpisodeNumber(), is(2));
    }
    
    @Test
    public void testSynopsesAreExtractedAsDescriptions() {
        ItemDetailType detail = detail();
        SynopsisListType synopsisList = new SynopsisListType();
        synopsisList.getSynopsis().add(synopsis("description","LNGSYNPS"));
        synopsisList.getSynopsis().add(synopsis("short-desc","TERSE"));
        synopsisList.getSynopsis().add(synopsis("med-desc","3LNSYNPS"));
        detail.setSynopsisList(synopsisList);
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());

        assertThat(extracted.getDescription(),is("description"));
        assertThat(extracted.getShortDescription(),is("short-desc"));
        assertThat(extracted.getMediumDescription(),is("med-desc"));
        assertThat(extracted.getLongDescription(),is("description"));
    }
    
    private SynopsisType synopsis(String text, String type) {
        SynopsisType synopsis = new SynopsisType();
        synopsis.setText(text);
        synopsis.setType(type);
        return synopsis;
    }
    
    @Test
    public void testCertificateIsExtracted() {
        ItemDetailType detail = detail();
        detail.setRatingCode("12");
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        assertThat(Iterables.getOnlyElement(extracted.getCertificates()).classification(), is("12"));
    }

    @Test
    public void testGenresAreExtracted() {
        ItemDetailType detail = detail();
        GenreListType genreList = new GenreListType();
        GenreType genreType = new GenreType();
        genreType.setGenreCode("DRAMA");
        genreType.setGenreDescription("Drama");
        genreList.getGenre().add(genreType);
        detail.getGenreList().add(genreList);
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        assertThat(Iterables.getOnlyElement(extracted.getGenres()), is("http://talktalk.net/genres/DRAMA"));
    }
    
    @Test
    public void testImagesAreExtracted() {
        ItemDetailType detail = detail();
        ImageListType imageList = new ImageListType();
        ImageType imageType = new ImageType();
        imageType.setFilename("image.png");
        imageList.getImage().add(imageType);
        detail.setImageList(imageList);        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        assertThat(Iterables.getOnlyElement(extracted.getImages()).getCanonicalUri(), is("image.png"));
    }
    
    @Test
    public void testDurationExtractedInVersion() {
        ItemDetailType detail = detail();

        detail.setDuration("0 00:41:00");
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        long fortyTwoMinutesInSeconds = Duration.standardMinutes(41).getStandardSeconds();
        assertThat(Iterables.getOnlyElement(extracted.getVersions()).getDuration().longValue(), is(fortyTwoMinutesInSeconds));
    }

    @Test
    public void testAvailabilityExtractedInPolicy() throws Exception {
        ItemDetailType detail = detail();
        
        detail.setProductType(ProductTypeType.SUBSCRIPTION);
        DateTime start = new DateTime("2013-03-01T00:00:00.000Z", DateTimeZones.UTC);
        DateTime end = new DateTime("2013-09-02T11:59:59.000+01:00", DateTimeZones.UTC);
        
        DatatypeFactory dataTypeFactory = DatatypeFactory.newInstance();
        AvailabilityType availability = new AvailabilityType();
        availability.setStartDt(dataTypeFactory.newXMLGregorianCalendar(start.toGregorianCalendar()));
        availability.setEndDt(dataTypeFactory.newXMLGregorianCalendar(end.toGregorianCalendar()));
        detail.setAvailability(availability);
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());
        
        Policy policy = getOnlyPolicy(extracted);
        
        assertThat(policy.getAvailabilityStart(), is(start));
        assertThat(policy.getAvailabilityEnd(), is(end));
    }
    
    @Test
    public void testExtractsRevenueContract() {
        
        ItemDetailType detail = detail();
        detail.setProductType(ProductTypeType.FREE);
        
        Item extracted = extractor.extract(detail, Optional.<Brand>absent(), Optional.<Series>absent());

        Policy policy = getOnlyPolicy(extracted);
        
        assertThat(policy.getRevenueContract(), is(RevenueContract.FREE_TO_VIEW));
        
    }

    private Policy getOnlyPolicy(Item extracted) {
        Version version = Iterables.getOnlyElement(extracted.getVersions());
        Encoding encoding = Iterables.getOnlyElement(version.getManifestedAs());
        Location location = Iterables.getOnlyElement(encoding.getAvailableAt());
        Policy policy = location.getPolicy();
        return policy;
    }
    
}
