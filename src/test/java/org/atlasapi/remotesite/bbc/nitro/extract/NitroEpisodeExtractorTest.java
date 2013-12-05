package org.atlasapi.remotesite.bbc.nitro.extract;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Map.Entry;

import org.atlasapi.media.entity.Item;
import org.junit.Test;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.metabroadcast.atlas.glycerin.model.AncestorsTitles;
import com.metabroadcast.atlas.glycerin.model.Availability;
import com.metabroadcast.atlas.glycerin.model.Episode;
import com.metabroadcast.atlas.glycerin.model.PidReference;
import com.metabroadcast.common.time.SystemClock;


public class NitroEpisodeExtractorTest {
    
    private static final ImmutableList<Availability> noAvailability = ImmutableList.<Availability>of();
    private final NitroEpisodeExtractor extractor = new NitroEpisodeExtractor(new SystemClock());

    @Test
    public void testParentRefsForExtractedTopLevelItemAreEmpty() {
        
        Episode tli = new Episode();
        tli.setPid("p01mv8m3");
        tli.setTitle("Pantocracy");
        
        Item extracted = extractor.extract(NitroItemSource.valueOf(tli, noAvailability));
        
        assertFalse(extracted instanceof org.atlasapi.media.entity.Episode);
        assertNull(extracted.getContainer());
        assertThat(extracted.getTitle(), is(tli.getTitle()));
        
    }

    @Test
    public void testParentRefsForExtractedBrandEpisodeAreBrandOnly() {
        
        Episode brandEpisode = new Episode();
        brandEpisode.setPid("p017m2vg");
        brandEpisode.setTitle("01/01/2004");
        brandEpisode.setEpisodeOf(pidRef("brand", "b006m86d"));
        brandEpisode.setAncestorsTitles(ancestorsTitles("b006m86d", "EastEnders"));
        
        Item extracted = extractor.extract(NitroItemSource.valueOf(brandEpisode, noAvailability));
        
        org.atlasapi.media.entity.Episode episode
            = (org.atlasapi.media.entity.Episode) extracted;
        assertThat(episode.getContainer().getUri(), endsWith("b006m86d"));
        assertNull(episode.getSeriesRef());
        assertThat(episode.getTitle(), is(brandEpisode.getTitle()));
    }

    @Test
    public void testParentRefsForExtractedSeriesEpisodeAreSeriesOnly() {
        
        Episode brandEpisode = new Episode();
        brandEpisode.setPid("b012cl84");
        brandEpisode.setTitle("Destiny");
        brandEpisode.setEpisodeOf(pidRef("series", "b00zdhtg"));
        brandEpisode.setAncestorsTitles(ancestorsTitles(null, null,
            ImmutableMap.of("b00zdhtg", "Wonders of the Universe")
        ));
        
        Item extracted = extractor.extract(NitroItemSource.valueOf(brandEpisode, noAvailability));
        
        org.atlasapi.media.entity.Episode episode
        = (org.atlasapi.media.entity.Episode) extracted;
        assertThat(episode.getContainer().getUri(), endsWith("b00zdhtg"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("b00zdhtg"));
        assertThat(episode.getTitle(), is(brandEpisode.getTitle()));
    }

    @Test
    public void testParentRefsForExtractedBrandSeriesEpisodeAreBrandAndSeries() {
        
        Episode brandSeriesEpisode = new Episode();
        brandSeriesEpisode.setPid("p00wqr14");
        brandSeriesEpisode.setTitle("Asylum of the Daleks");
        brandSeriesEpisode.setPresentationTitle("Episode 1");
        brandSeriesEpisode.setEpisodeOf(pidRef("series", "p00wqr12"));
        brandSeriesEpisode.setAncestorsTitles(ancestorsTitles("b006q2x0", "Doctor Who",
            ImmutableMap.of("p00wqr12","Series 7 Part 1")
        ));
        
        Item extracted = extractor.extract(NitroItemSource.valueOf(brandSeriesEpisode, noAvailability));
        
        org.atlasapi.media.entity.Episode episode
            = (org.atlasapi.media.entity.Episode) extracted;
        assertThat(episode.getContainer().getUri(), endsWith("b006q2x0"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("p00wqr12"));
        assertThat(episode.getTitle(), is(brandSeriesEpisode.getTitle()));
    }

    @Test
    public void testParentRefsForExtractedBrandSeriesSeriesEpisodeAreBrandAndHigherLevelSeries() {
        
        Episode brandSeriesSeriesEpisode = new Episode();
        brandSeriesSeriesEpisode.setPid("b01h91l5");
        brandSeriesSeriesEpisode.setTitle("Part 2");
        brandSeriesSeriesEpisode.setPresentationTitle("Part 2");
        brandSeriesSeriesEpisode.setEpisodeOf(pidRef("series", "b01h8xs7"));
        brandSeriesSeriesEpisode.setAncestorsTitles(ancestorsTitles("b007y6k8", "Silent Witness",
            ImmutableMap.of("b01fltqv","Series 15", "b01h8xs7", "Fear")
        ));
        
        Item extracted = extractor.extract(NitroItemSource.valueOf(brandSeriesSeriesEpisode, noAvailability));
        
        org.atlasapi.media.entity.Episode episode
            = (org.atlasapi.media.entity.Episode) extracted;
        assertThat(episode.getContainer().getUri(), endsWith("b007y6k8"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("b01fltqv"));
        assertThat(episode.getTitle(), is("Fear Part 2"));
    }

    @Test
    public void testParentRefsForExtractedSeriesSeriesEpisodeAreBothHigherLevelSeries() {
        
        Episode seriesSeriesEpisode = new Episode();
        seriesSeriesEpisode.setPid("b011pq1v");
        seriesSeriesEpisode.setPresentationTitle("Part 3");
        seriesSeriesEpisode.setEpisodeOf(pidRef("series", "b011s30z"));
        seriesSeriesEpisode.setAncestorsTitles(ancestorsTitles(null, null,
            ImmutableMap.of("b011cdng","The Complete Smiley",
                    "b011s30z","Tinker, Tailor, Soldier, Spy")
        ));
        
        Item extracted = extractor.extract(NitroItemSource.valueOf(seriesSeriesEpisode, noAvailability));
        
        org.atlasapi.media.entity.Episode episode
            = (org.atlasapi.media.entity.Episode) extracted;
        assertThat(episode.getContainer().getUri(), endsWith("b011cdng"));
        assertThat(episode.getSeriesRef().getUri(), endsWith("b011cdng"));
        assertThat(episode.getTitle(), is("Tinker, Tailor, Soldier, Spy Part 3"));
    }

    private AncestorsTitles ancestorsTitles(String brandPid, String brandTitle) {
        return ancestorsTitles(brandPid, brandTitle, ImmutableMap.<String,String>of());
    }

    private AncestorsTitles ancestorsTitles(String brandPid, String brandTitle,
            ImmutableMap<String, String> series) {
        AncestorsTitles titles = new AncestorsTitles();
        if (!Strings.isNullOrEmpty(brandPid) && !Strings.isNullOrEmpty(brandTitle)) {
            AncestorsTitles.Brand brand = new AncestorsTitles.Brand();
            brand.setPid(brandPid);
            brand.setTitle(brandTitle);
            titles.setBrand(brand);
        }
        for (Entry<String, String> sery : series.entrySet()) {
            AncestorsTitles.Series ancestorSeries = new AncestorsTitles.Series();
            ancestorSeries.setPid(sery.getKey());
            ancestorSeries.setTitle(sery.getValue());
            titles.getSeries().add(ancestorSeries);
        }
        return titles;
    }

    private PidReference pidRef(String type, String pid) {
        PidReference pidRef = new PidReference();
        pidRef.setPid(pid);
        pidRef.setResultType(type);
        return pidRef;
    }

}
