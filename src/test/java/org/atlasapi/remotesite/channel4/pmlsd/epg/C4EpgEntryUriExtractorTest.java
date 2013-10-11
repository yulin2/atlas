package org.atlasapi.remotesite.channel4.pmlsd.epg;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.remotesite.channel4.pmlsd.C4Module;
import org.atlasapi.remotesite.channel4.pmlsd.epg.C4EpgEntryUriExtractor;
import org.atlasapi.remotesite.channel4.pmlsd.epg.model.C4EpgEntry;
import org.atlasapi.remotesite.channel4.pmlsd.epg.model.TypedLink;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class C4EpgEntryUriExtractorTest {

    private final C4EpgEntryUriExtractor extractor = new C4EpgEntryUriExtractor();
    
    @Test
    public void testExtractsBrandFromFullEpisodeAtomUri() {
        extractBrand("http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1/episode-3.atom", 
            "http://www.channel4.com/programmes/the-hoobs");
    }

    @Test
    public void testExtractsBrandFromSeriesAtomUri() {
        extractBrand("http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1.atom", 
                "http://www.channel4.com/programmes/the-hoobs");
    }
    
    @Test
    public void testExtractsBrandFromBrandOnlyAtomUri() {
        extractBrand("http://pmlsc.channel4.com/pmlsd/the-hoobs.atom", 
                "http://www.channel4.com/programmes/the-hoobs");
    }
    
    private void extractBrand(String input, String output) {
        assertThat(extractor.uriForBrand(entryWithRelatedLink(input)), is(Optional.of(output)));
    }

    @Test
    public void testExtractsSeriesFromFullEpisodeAtomUri() {
        C4EpgEntry entry = entryWithRelatedLink("http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1/episode-3.atom");
        extractSeries(entry, "http://www.channel4.com/programmes/the-hoobs/episode-guide/series-1");
    }
    
    @Test
    public void testExtractsSeriesFromSeriesAtomUri() {
        C4EpgEntry entry = entryWithRelatedLink("http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1.atom");
        extractSeries(entry, "http://www.channel4.com/programmes/the-hoobs/episode-guide/series-1");
    }
    
    @Test
    public void testDoesntResolveSeriesFromBrandOnlyAtomUriWhenSeriesNumberIsAbsent() {
        assertFalse(extractor.uriForSeries(entryWithRelatedLink("http://pmlsc.channel4.com/pmlsd/the-hoobs.atom")).isPresent());
    }
    
    @Test
    public void testDoesResolveSeriesFromBrandOnlyAtomUriWhenSeriesNumberIsPresent() {
        C4EpgEntry entry = entryWithRelatedLink("http://pmlsc.channel4.com/pmlsd/the-hoobs.atom");
        entry.withSeriesNumber(1);
        extractSeries(entry, "http://www.channel4.com/programmes/the-hoobs/episode-guide/series-1");
    }
    
    private void extractSeries(C4EpgEntry input, String output) {
        assertThat(extractor.uriForSeries(input), is(Optional.of(output)));
    }
    
    @Test
    public void testExtractsItemId() {
        C4EpgEntry entry = entryWithoutRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438")
                .withProgrammeId("40635/014")
                .withTitle("The Treacle People");
        assertThat(extractor.uriForItemId(entry), is("http://www.channel4.com/programmes/40635/014"));
    }

    @Test
    public void testExtractsItemSynthUriWithNoRelatedLink() {
        C4EpgEntry entry = entryWithoutRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438")
                .withProgrammeId("40635/014")
                .withTitle("The Treacle People");
        assertThat(extractor.uriForItemSynthesized(entry, Optional.<Brand>absent()).get(), 
            is("http://www.channel4.com/programmes/synthesized/the-treacle-people/26424438"));
    }

    @Test
    public void testExtractsItemSynthUriWhenNoRelatedLinkUsingPresentBrandUri() {
        C4EpgEntry entry = entryWithoutRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored");
        Brand brand = new Brand("http://www.channel4.com/programmes/the-treacle-people", "c4:ttp", C4Module.SOURCE);
        assertThat(extractor.uriForItemSynthesized(entry, Optional.of(brand)).get(),
            is("http://www.channel4.com/programmes/synthesized/the-treacle-people/26424438"));
    }
    
    @Test
    public void testExtractsItemHierarchyUriWhenFullRelatedLinkPresent() {
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
            "http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1/episode-3.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored");
        assertThat(extractor.uriForItemHierarchy(entry).get(),
            is("http://www.channel4.com/programmes/the-hoobs/episode-guide/series-1/episode-3"));
    }
    
    @Test
    public void testDoesntExtractItemHierarchyUriWhenSeriesRelatedLinkAndEpisodeNumberPresent() {
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
                "http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored")
                .withEpisodeNumber(3);
        assertFalse(extractor.uriForItemHierarchy(entry).isPresent());
    }
    
    @Test
    public void testDoesntExtractItemHierarchyUriWhenSeriesRelatedLinkAndNoEpisodeNumberPresent(){
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
                "http://pmlsc.channel4.com/pmlsd/the-hoobs/episode-guide/series-1.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored");
        assertFalse(extractor.uriForItemHierarchy(entry).isPresent());
    }
    
    @Test
    public void testDoesntExtractItemHierarchyUriWhenBrandRelatedLinkAndSeriesAndEpisodeNumberPresent() {
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
                "http://pmlsc.channel4.com/pmlsd/the-hoobs.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored")
                .withSeriesNumber(1)
                .withEpisodeNumber(3);
        assertFalse(extractor.uriForItemHierarchy(entry).isPresent());
    }

    @Test
    public void testDoesntExtractItemHierarchyUriWhenBrandRelatedLinkAndNoSeriesOrEpisodeNumberPresent(){
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
                "http://pmlsc.channel4.com/pmlsd/the-hoobs.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored");
        assertFalse(extractor.uriForItemHierarchy(entry).isPresent());
    }

    @Test
    public void testDoesntExtractItemHierarchyUriWhenBrandRelatedLinkAndOnlySeriesNumberPresent(){
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
                "http://pmlsc.channel4.com/pmlsd/the-hoobs.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored")
                .withSeriesNumber(1);
        assertFalse(extractor.uriForItemHierarchy(entry).isPresent());
    }
    
    @Test
    public void testDoesntExtractItemHierarchyUriWhenBrandRelatedLinkAndOnlyEpisodeNumberPresent(){
        C4EpgEntry entry = entryWithIdAndRelatedLink("tag:pmlsc.channel4.com,2009:slot/26424438", 
                "http://pmlsc.channel4.com/pmlsd/the-hoobs.atom")
                .withProgrammeId("40635/014")
                .withTitle("This Title Is Ignored")
                .withEpisodeNumber(3);
        assertFalse(extractor.uriForItemHierarchy(entry).isPresent());
    }

    private C4EpgEntry entryWithRelatedLink(String uri) {
        return entryWithIdAndRelatedLink("noid", uri);
    }
    
    private C4EpgEntry entryWithIdAndRelatedLink(String id, String uri) {
        return new C4EpgEntry(id).withLinks(ImmutableList.of(new TypedLink(uri, "related")));
    }

    private C4EpgEntry entryWithoutRelatedLink(String id) {
        return new C4EpgEntry(id);
    }

}
