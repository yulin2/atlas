package org.atlasapi.query.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.tvanytime.TvAnytimeGenerator;
import org.atlasapi.feeds.youview.statistics.FeedStatistics;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.content.mongo.LastUpdatedContentFinder;
import org.atlasapi.persistence.logging.AdapterLog;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.media.MimeType;
import com.metabroadcast.common.time.DateTimeZones;

/**
 * Produces an output feed a certain provider, given a certain publisher's content.
 * <p>
 * Currently, the only feed supported is the TVAnytime output for YouView
 * 
 * @author Oliver Hall (oli@metabroadcast.com)
 *
 */
// TODO this bypasses the normal atlas writing framework (as it's not writing atlas classes)
// this needs a less hacky solution as to how we choose to output non-atlas classes through the atlas
// writing framework.
// have made it use Iterable<FeedStatistics> for now
@RequestMapping(value = "/3.0/feeds/youview/{publisher}")
@Controller
public class ContentFeedController extends BaseController<Iterable<FeedStatistics>> {

    private static final DateTime START_OF_TIME = new DateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZones.UTC);
    private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException())
            .withMessage("You require an API key to view this data")
            .withErrorCode("Api Key required")
            .withStatusCode(HttpStatusCode.FORBIDDEN);
    
    private final DateTimeFormatter fmt = ISODateTimeFormat.dateHourMinuteSecond().withZone(DateTimeZones.UTC);
    private final TvAnytimeGenerator feedGenerator;
    private final LastUpdatedContentFinder contentFinder;
    private final ContentResolver contentResolver;
    
    public ContentFeedController(ApplicationConfigurationFetcher configFetcher, AdapterLog log, 
            AtlasModelWriter<Iterable<FeedStatistics>> outputter, TvAnytimeGenerator feedGenerator, 
            LastUpdatedContentFinder contentFinder, ContentResolver contentResolver) {
        super(configFetcher, log, outputter);
        this.feedGenerator = checkNotNull(feedGenerator);
        this.contentFinder = checkNotNull(contentFinder);
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
    @RequestMapping(value=".xml", method = RequestMethod.GET)
    public void generateFeed(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("publisher") String publisherStr,
            @RequestParam(value = "lastUpdated", required = false) String lastUpdated,
            @RequestParam(value = "uri", required = false) String uri) throws IOException {
        try {
            Publisher publisher = Publisher.valueOf(publisherStr.trim().toUpperCase());
            ApplicationConfiguration appConfig = appConfig(request);
            if (!appConfig.isEnabled(publisher)) {
                errorViewFor(request, response, FORBIDDEN);
            }

            Optional<String> since = Optional.fromNullable(lastUpdated);
            Optional<String> possibleUri = Optional.fromNullable(uri);
            feedGenerator.generateXml(getContent(publisher, since, possibleUri), response.getOutputStream());

            response.setContentType(MimeType.APPLICATION_XML.toString());
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
    
    private Iterable<Content> getContent(Publisher publisher, Optional<String> since, Optional<String> possibleUri) {
        if (possibleUri.isPresent()) {
            ResolvedContent resolvedContent = contentResolver.findByCanonicalUris(ImmutableList.of(possibleUri.get()));
            Collection<Identified> resolved = resolvedContent.asResolvedMap().values();
            return ImmutableList.of((Content) Iterables.getOnlyElement(resolved));
        } else {
            DateTime start = since.isPresent() ? fmt.parseDateTime(since.get()) : START_OF_TIME;
            return ImmutableList.copyOf(contentFinder.updatedSince(publisher, start));
        }
    }
}
