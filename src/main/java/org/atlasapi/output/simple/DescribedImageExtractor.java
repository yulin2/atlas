package org.atlasapi.output.simple;

import static com.google.common.base.Preconditions.checkArgument;
import static com.metabroadcast.common.media.MimeType.IMAGE_JPG;
import static org.atlasapi.media.entity.ImageAspectRatio.SIXTEEN_BY_NINE;
import static org.atlasapi.media.entity.ImageType.PRIMARY;

import java.util.Set;

import org.atlasapi.media.entity.Described;
import org.atlasapi.media.entity.Image;
import org.atlasapi.media.entity.Publisher;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


public class DescribedImageExtractor {
    
    private final ImmutableMap<Publisher, Image.Builder> defaults
        = ImmutableMap.<Publisher,Image.Builder>builder()
            .put(Publisher.BBC, Image.builder("default")
                    .withWidth(640).withHeight(360)
                    .withAspectRatio(SIXTEEN_BY_NINE)
                    .withType(PRIMARY).withMimeType(IMAGE_JPG))
            .put(Publisher.C4, Image.builder("default")
                    .withWidth(625).withHeight(352)
                    .withAspectRatio(SIXTEEN_BY_NINE)
                    .withType(PRIMARY).withMimeType(IMAGE_JPG))
            .put(Publisher.PREVIEW_NETWORKS, Image.builder("default")
                    .withAspectRatio(SIXTEEN_BY_NINE)
                    .withType(PRIMARY).withMimeType(IMAGE_JPG))
            .build();
    
    public Set<Image> getImages(Described described) {
        if (!described.getImages().isEmpty()) {
            return described.getImages();
        }
        if (Strings.isNullOrEmpty(described.getImage())) {
            return ImmutableSet.of();
        }
        return ImmutableSet.of(createImage(described));
    }

    private Image createImage(Described described) {
        checkArgument(!Strings.isNullOrEmpty(described.getImage()),
                "Can't create Image from null/empty image field on %s", described);
        Image.Builder dflt = defaults.get(described.getPublisher());
        if (dflt == null) {
            Image image = new Image(described.getImage());
            image.setType(PRIMARY);
            return image;
        }
        return dflt.withUri(described.getImage()).build();
    }

}
