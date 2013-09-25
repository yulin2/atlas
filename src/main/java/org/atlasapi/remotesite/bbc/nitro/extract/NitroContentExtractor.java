package org.atlasapi.remotesite.bbc.nitro.extract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.atlasapi.media.entity.Alias;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Image;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.remotesite.ContentExtractor;
import org.atlasapi.remotesite.bbc.BbcFeeds;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.atlas.glycerin.model.Synopses;

/**
 * Template extractor for extracting {@link Content} from Nitro sources.
 * 
 * Concrete implementations must override methods to create the intended type of
 * {@code Content} and project required fields out of the source.
 * 
 * @param <SOURCE> - the Nitro source type.
 * @param <CONTENT> - the {@link Content} type to be extracted. 
 */
public abstract class NitroContentExtractor<SOURCE, CONTENT extends Content>
    implements ContentExtractor<SOURCE, CONTENT> {

    private static final String PID_NAMESPACE = "gb:bbc:pid";
    private static final String URI_NAMESPACE = "uri";
    
    private final NitroImageExtractor imageExtractor
        = new NitroImageExtractor("1024x576");
    
    @Override
    public final CONTENT extract(SOURCE source) {
        CONTENT content = createContent(source);
        String pid = extractPid(source);
        content.setCanonicalUri(BbcFeeds.nitroUriForPid(pid));
        content.setPublisher(Publisher.BBC_NITRO);
        content.setTitle(extractTitle(source));
        content.setAliases(ImmutableSet.of(
            new Alias(PID_NAMESPACE, pid),
            new Alias(URI_NAMESPACE, content.getCanonicalUri())
        ));
        Synopses synposes = extractSynopses(source);
        if (synposes != null) {
            content.setDescription(longestSynopsis(synposes));
            content.setShortDescription(synposes.getShort());
            content.setMediumDescription(synposes.getMedium());
            content.setLongDescription(synposes.getLong());
        }
        com.metabroadcast.atlas.glycerin.model.Image srcImage = extractImage(source);
        if (srcImage != null && !Strings.isNullOrEmpty(srcImage.getTemplateUrl())) {
            Image image = imageExtractor.extract(srcImage);
            content.setImage(image.getCanonicalUri());
            content.setImages(ImmutableSet.of(image));
        }
        //TODO: brand.setPresentationChannel(BbcIonServices.get(source.getMasterBrand()));
        //TODO: genres
        extractAdditionalFields(source, content);
        return content;
    }

    /**
     * Creates a the raw {@code Content} object to be extracted.
     * 
     * @param source
     *            - the source data, this can be used to determine the right type of
     *            {@code Content} to create.
     * @return - returns a {@link Content} object.
     */
    protected abstract @Nonnull CONTENT createContent(SOURCE source);
    
    /**
     * Projects the PID of the source data.
     * 
     * @param source
     *            - the source data
     * @return - the PID of the source data, must not be {@code null}.
     */
    protected abstract @Nonnull String extractPid(SOURCE source);
    
    /**
     * Projects the title of the source data.
     * 
     * @param source
     *            - the source data
     * @return - the title of the source data, or {@code null} if there is none.
     */
    protected abstract @Nullable String extractTitle(SOURCE source);
    
    /**
     * Projects the {@link Synopses} of the source data.
     * 
     * @param source
     *            - the source data
     * @return - the synopses of the source data, or {@code null} if there is
     *         none.
     */
    protected abstract @Nullable Synopses extractSynopses(SOURCE source);
    
    /**
     * Projects the {@link com.metabroadcast.atlas.glycerin.model.Image Image}
     * of the source data.
     * 
     * @param source
     *            - the source data
     * @return - the image of the source data, or {@code null} if there is none.
     */
    protected abstract @Nullable com.metabroadcast.atlas.glycerin.model.Image extractImage(SOURCE source);
    
    /**
     * Concrete implementations can override this method to perform additional
     * configuration of the extracted content from the source.
     * 
     * @param source - the source data.
     * @param content - the extracted content.
     */
    protected void extractAdditionalFields(SOURCE source, CONTENT content) {
        
    }

    private String longestSynopsis(Synopses synopses) {
        return Strings.emptyToNull(
            Objects.firstNonNull(synopses.getLong(), 
                Objects.firstNonNull(synopses.getMedium(), 
                    Objects.firstNonNull(synopses.getShort(), ""))));
    }

}
