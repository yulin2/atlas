package org.atlasapi.remotesite.btvod;

import joptsimple.internal.Strings;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Image;
import org.atlasapi.media.entity.ImageType;
import org.atlasapi.remotesite.btvod.BtVodData.BtVodDataRow;

import com.google.common.collect.ImmutableSet;


public class BtVodDescribedFieldsExtractor {

    private static final String IMAGE_URI_PREFIX = "http://portal.vision.bt.com/btvo/content_providers/images/";
    
    public void setDescribedFieldsFrom(BtVodDataRow row, Described described) {
        described.setDescription(row.getColumnValue(BtVodFileColumn.SYNOPSIS));
        described.setImages(createImages(row));
    }
    
    private Iterable<Image> createImages(BtVodDataRow row) {    
        String packshotFilename = row.getColumnValue(BtVodFileColumn.PACKSHOT);
        if (Strings.isNullOrEmpty(packshotFilename)) {
            return ImmutableSet.of();
        }
        
        Image image = new Image(IMAGE_URI_PREFIX + packshotFilename);
        image.setType(ImageType.PRIMARY);
        return ImmutableSet.of(image);
    }
}
