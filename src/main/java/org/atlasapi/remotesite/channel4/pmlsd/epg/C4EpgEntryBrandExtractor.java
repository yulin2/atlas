package org.atlasapi.remotesite.channel4.pmlsd.epg;

import org.atlasapi.media.entity.Brand;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.channel4.pmlsd.C4AtomApi;
import org.atlasapi.remotesite.channel4.pmlsd.C4PmlsdModule;
import org.atlasapi.remotesite.channel4.pmlsd.epg.model.C4EpgEntry;

import com.google.common.base.Optional;

public class C4EpgEntryBrandExtractor implements ContentExtractor<C4EpgEntry, Optional<Brand>> {

    private final C4EpgEntryUriExtractor uriExtractor = new C4EpgEntryUriExtractor();
    
    @Override
    public Optional<Brand> extract(C4EpgEntry source) {
        if (!source.hasRelatedLink()) {
            return Optional.absent();
        }
        
        Optional<String> possibleBrandUri = uriExtractor.uriForBrand(source);
        if (!possibleBrandUri.isPresent()) {
            return Optional.absent();
        }
        
        String brandUri = possibleBrandUri.get();
        
        Brand brand = C4PmlsdModule.contentFactory().createBrand();
        brand.setCanonicalUri(brandUri);
        brand.addAliasUrl(C4AtomApi.hierarchyUriFromCanonical(brandUri));
        
        if (source.brandTitle() != null) {
            brand.setTitle(source.brandTitle());
        } else {
            brand.setTitle(source.title());
        }
        
        return Optional.of(brand);
    }

}
