package org.atlasapi.remotesite.itunes.epf;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import org.atlasapi.media.TransportSubType;
import org.atlasapi.media.TransportType;
import org.atlasapi.media.entity.Location;
import org.atlasapi.media.entity.Policy;
import org.atlasapi.media.entity.Policy.RevenueContract;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.itunes.epf.model.EpfPricing;

import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.currency.Price;

public class ItunesPricingLocationExtractor implements ContentExtractor<ItunesEpfPricingSource, Maybe<Location>> {

    @Override
    public Maybe<Location> extract(ItunesEpfPricingSource source) {
        BigDecimal sdPrice = source.getRow().get(EpfPricing.SD_PRICE);
        if (sdPrice == null) {
            return Maybe.nothing();
        }
        
        Location location = new Location();
        location.setUri(source.getRow().get(EpfPricing.EPISODE_URL));
        location.setTransportType(TransportType.APPLICATION);
        location.setTransportSubType(TransportSubType.ITUNES);

        Policy policy = new Policy();
        policy.addAvailableCountry(source.getCountry());
        policy.setRevenueContract(RevenueContract.PAY_TO_BUY);

        Currency currency = Currency.getInstance(new Locale("en", source.getCountry().code()));
        policy.setPrice(new Price(currency, sdPrice.movePointRight(currency.getDefaultFractionDigits()).intValue()));

        location.setPolicy(policy);

        return Maybe.just(location);
    }

}
