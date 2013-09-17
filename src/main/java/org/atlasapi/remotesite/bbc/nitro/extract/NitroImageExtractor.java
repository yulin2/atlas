package org.atlasapi.remotesite.bbc.nitro.extract;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.atlasapi.media.entity.Image;
import org.atlasapi.remotesite.ContentExtractor;

import com.google.api.client.repackaged.com.google.common.base.Strings;

/**
 * Extracts an {@link Image} from a
 * {@link com.metabroadcast.atlas.glycerin.model.Image} according to a recipe.
 * 
 */
public class NitroImageExtractor 
    implements ContentExtractor<com.metabroadcast.atlas.glycerin.model.Image, Image> {

    private final String recipe;

    /**
     * Create a new extractor which extracts {@link Image}s with the provided
     * recipe.
     * 
     * @param recipe
     *            - the recipe to use when extract an Image, e.g. "1024x576"
     */
    public NitroImageExtractor(String recipe) {
        checkArgument(!Strings.isNullOrEmpty(recipe), "recipe can't be null or empty");
        this.recipe = recipe;
    }
    
    @Override
    public Image extract(com.metabroadcast.atlas.glycerin.model.Image source) {
        checkNotNull(source, "null image source");
        checkNotNull(source.getTemplateUrl(), "null image template");
        String url = source.getTemplateUrl().replace("$recipe", recipe);
        return new Image(url);
    }

}
