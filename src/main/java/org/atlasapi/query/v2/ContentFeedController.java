package org.atlasapi.query.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.tvanytime.TvAnytimeGenerator;
import org.atlasapi.feeds.tvanytime.TvaGenerationException;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import tva.metadata._2010.TVAMainType;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.HttpStatusCode;

/**
 * Produces an output feed a certain provider, given a certain publisher's content.
 * <p>
 * Currently, the only feed supported is the TVAnytime output for YouView
 * 
 * @author Oliver Hall (oli@metabroadcast.com)
 *
 */
@Controller
public class ContentFeedController extends BaseController<JAXBElement<TVAMainType>> {

    private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException())
            .withMessage("You require an API key to view this data")
            .withErrorCode("Api Key required")
            .withStatusCode(HttpStatusCode.FORBIDDEN);
    private static final AtlasErrorSummary NO_URI = new AtlasErrorSummary(new NullPointerException())
            .withMessage("Required parameter 'uri' is missing")
            .withErrorCode("Uri parameter required")
            .withStatusCode(HttpStatusCode.BAD_REQUEST);
    private static final AtlasErrorSummary CONTENT_NOT_FOUND = new AtlasErrorSummary(new NullPointerException())
            .withMessage("Unable to resolve content with the provided uri")
            .withErrorCode("Content not found")
            .withStatusCode(HttpStatusCode.BAD_REQUEST);
    
    private final TvAnytimeGenerator feedGenerator;
    private final ContentResolver contentResolver;
    
    public ContentFeedController(ApplicationConfigurationFetcher configFetcher, AdapterLog log, 
            AtlasModelWriter<JAXBElement<TVAMainType>> outputter, TvAnytimeGenerator feedGenerator, 
            ContentResolver contentResolver) {
        super(configFetcher, log, outputter);
        this.feedGenerator = checkNotNull(feedGenerator);
        this.contentResolver = checkNotNull(contentResolver);
    }
    
    /**
     * Produces an output feed a certain provider, given a certain publisher's content
     * @param uri -         if present, the endpoint will return the xml generated for that 
     *                      particular item else it will check the lastUpdated parameter.                    
     * @param lastUpdated - if present, the endpoint will return a delta feed of all items 
     *                      updated since lastUpdated, otherwise it will return a full 
     *                      bootstrap feed
     * @throws IOException 
     */
    @RequestMapping(value="/3.0/feeds/youview/{publisher}.xml", method = RequestMethod.GET)
    public void generateFeed(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("publisher") String publisherStr,
            @RequestParam(value = "uri", required = true) String uri) throws IOException {
        try {
            Publisher publisher = Publisher.valueOf(publisherStr.trim().toUpperCase());
            ApplicationConfiguration appConfig = appConfig(request);
            if (!appConfig.isEnabled(publisher)) {
                errorViewFor(request, response, FORBIDDEN);
                return;
            }

            Optional<String> possibleUri = Optional.fromNullable(uri);
            if (!possibleUri.isPresent()) {
                errorViewFor(request, response, NO_URI);
                return;
            }
            Optional<Content> content = getContent(publisher, possibleUri.get());
            if (!content.isPresent()) {
                errorViewFor(request, response, CONTENT_NOT_FOUND);
                return;
            }
            JAXBElement<TVAMainType> tva = feedGenerator.generateTVAnytimeFrom(content.get());
            
            modelAndViewFor(request, response, tva, appConfig);
        } catch (TvaGenerationException e) {
            errorViewFor(request, response, tvaGenerationError(e));
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
    
    private AtlasErrorSummary tvaGenerationError(TvaGenerationException e) { 
        return new AtlasErrorSummary(e)
                .withMessage("Unable to generate TVAnytime output for the provided uri")
                .withErrorCode("TVAnytime generation error")
                .withStatusCode(HttpStatusCode.SERVER_ERROR);
    }
    
    private Optional<Content> getContent(Publisher publisher, String uri) {
        ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(ImmutableList.of(uri));
        Collection<Identified> resolved = resolvedContent.asResolvedMap().values();
        return Optional.fromNullable((Content) Iterables.getOnlyElement(resolved, null));
    }
}
