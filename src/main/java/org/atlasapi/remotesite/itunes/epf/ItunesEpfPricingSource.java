package org.atlasapi.remotesite.itunes.epf;

import org.atlasapi.remotesite.itunes.epf.model.EpfPricing;

import com.metabroadcast.common.intl.Country;

public class ItunesEpfPricingSource {

    private final EpfPricing row;
    private final Country country;
    
    public ItunesEpfPricingSource(EpfPricing row, Country country) {
        this.row = row;
        this.country = country;
    }
    
    public EpfPricing getRow() {
        return this.row;
    }
    public Country getCountry() {
        return this.country;
    }
    
}
