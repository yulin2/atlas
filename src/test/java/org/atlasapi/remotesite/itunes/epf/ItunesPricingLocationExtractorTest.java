package org.atlasapi.remotesite.itunes.epf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import java.util.Currency;
import java.util.Locale;

import junit.framework.TestCase;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.content.Location;
import org.atlasapi.media.content.Policy;
import org.atlasapi.media.content.Policy.RevenueContract;
import org.atlasapi.remotesite.itunes.epf.model.EpfPricing;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.currency.Price;
import com.metabroadcast.common.intl.Countries;

public class ItunesPricingLocationExtractorTest extends TestCase {

    private final ItunesPricingLocationExtractor extractor = new ItunesPricingLocationExtractor();

    @Test
    public void testExtract() {
        
        String locationUri = "http://itunes.apple.com/WebObjects/MZStore.woa/wa/viewTVSeason?uo=5&i=258143336&id=256322090";
        String locationImg = "http://a1382.phobos.apple.com/us/r1000/004/Features/a1/ac/3d/dj.jblxjkcm.133x100-99.jpg";
        EpfPricing pricing = new EpfPricing(ImmutableList.of(
            "Episode 2","","002","Monarchy, Series 1","2004 10 25","Channel 4",locationUri,locationImg,"","","SD","1.49","","","",""
        ));

        Maybe<Location> extractedLocation = extractor.extract(new ItunesEpfPricingSource(pricing, Countries.GB));
        
        assertTrue(extractedLocation.hasValue());
        
        Location location = extractedLocation.requireValue();
        
        assertThat(location.getUri(), is(equalTo(locationUri)));
        assertThat(location.getTransportType(), is(TransportType.APPLICATION));
        assertThat(location.getTransportSubType(), is(TransportSubType.ITUNES));
        
        Policy policy = location.getPolicy();
        
        assertThat(policy.getAvailableCountries().size(), is(1));
        assertThat(policy.getAvailableCountries(), hasItem(Countries.GB));
        assertThat(policy.getRevenueContract(), is(RevenueContract.PAY_TO_BUY));
        assertThat(policy.getPrice(), is(new Price(Currency.getInstance(Locale.UK), 149)));
        
    }

}
