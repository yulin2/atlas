package org.atlasapi.query.v4.search;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.query.IpCheckingApiKeyConfigurationFetcher;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.ErrorResultWriter;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.query.v2.ParameterChecker;
import org.atlasapi.query.v4.topic.TopicController;
import org.atlasapi.search.model.SearchQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.text.MoreStrings;

@Controller
public class SearchController {

    private static Logger log = LoggerFactory.getLogger(TopicController.class);

    private static final String QUERY_PARAM = "q";
    private static final String SPECIALIZATION_PARAM = "specialization";
    private static final String PUBLISHER_PARAM = "publisher";
    private static final String TITLE_WEIGHTING_PARAM = "titleWeighting";
    private static final String BROADCAST_WEIGHTING_PARAM = "broadcastWeighting";
    private static final String CATCHUP_WEIGHTING_PARAM = "catchupWeighting";
    private static final float DEFAULT_TITLE_WEIGHTING = 1.0f;
    private static final float DEFAULT_BROADCAST_WEIGHTING = 0.2f;
    private static final float DEFAULT_CATCHUP_WEIGHTING = 0.15f;
    
    private final SearchResolver searcher;
    private final ApplicationConfigurationFetcher configFetcher;
    private final QueryResultWriter<Content> resultWriter;

    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    
    private final ParameterChecker paramChecker = new ParameterChecker(ImmutableSet.of(IpCheckingApiKeyConfigurationFetcher.API_KEY_QUERY_PARAMETER, Selection.LIMIT_REQUEST_PARAM,
            Selection.START_INDEX_REQUEST_PARAM, QUERY_PARAM, SPECIALIZATION_PARAM, PUBLISHER_PARAM, TITLE_WEIGHTING_PARAM, BROADCAST_WEIGHTING_PARAM, CATCHUP_WEIGHTING_PARAM));

    public SearchController(SearchResolver searcher, ApplicationConfigurationFetcher configFetcher, QueryResultWriter<Content> resultWriter) {
        this.searcher = searcher;
        this.configFetcher = configFetcher;
        this.resultWriter = resultWriter;
    }

    @RequestMapping({"/4.0/search.*", "/4.0/search"})
    public void search(@RequestParam(QUERY_PARAM) String q,
            @RequestParam(value = SPECIALIZATION_PARAM, required = false) String specialization,
            @RequestParam(value = PUBLISHER_PARAM, required = false) String publisher,
            @RequestParam(value = TITLE_WEIGHTING_PARAM, required = false) String titleWeightingParam,
            @RequestParam(value = BROADCAST_WEIGHTING_PARAM, required = false) String broadcastWeightingParam,
            @RequestParam(value = CATCHUP_WEIGHTING_PARAM, required = false) String catchupWeightingParam, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseWriter writer = null;
        try {
            writer = writerResolver.writerFor(request, response);
            paramChecker.checkParameters(request);

            if (Strings.isNullOrEmpty(q)) {
                throw new IllegalArgumentException("You must specify a query parameter");
            }

            Selection selection = Selection.builder().build(request);
            if (!selection.hasLimit()) {
                throw new IllegalArgumentException("You must specify a limit parameter");
            }

            float titleWeighting = getFloatParam(titleWeightingParam, DEFAULT_TITLE_WEIGHTING);
            float broadcastWeighting = getFloatParam(broadcastWeightingParam, DEFAULT_BROADCAST_WEIGHTING);
            float catchupWeighting = getFloatParam(catchupWeightingParam, DEFAULT_CATCHUP_WEIGHTING);

            ApplicationConfiguration appConfig = configFetcher.configurationFor(request).valueOrDefault(ApplicationConfiguration.DEFAULT_CONFIGURATION);
            Set<Specialization> specializations = specializations(specialization);
            Set<Publisher> publishers = publishers(publisher, appConfig);
            List<Identified> content = searcher.search(new SearchQuery(q, selection, specializations, publishers, titleWeighting, broadcastWeighting, catchupWeighting), appConfig);
            resultWriter.write(QueryResult.listResult(Iterables.filter(content, Content.class), new QueryContext(appConfig, Annotation.defaultAnnotations(), selection)), writer);
        } catch (Exception e) {
            log.error("Request exception " + request.getRequestURI(), e);
            ErrorSummary summary = ErrorSummary.forException(e);
            new ErrorResultWriter().write(summary, writer, request, response);
        }
    }

    private Set<Publisher> publishers(String publisher, ApplicationConfiguration appConfig) {
        return Sets.intersection(ImmutableSet.copyOf(Publisher.fromCsv(publisher)), appConfig.getEnabledSources());
    }

    private float getFloatParam(String stringValue, float defaultValue) {
        if (!Strings.isNullOrEmpty(stringValue)) {
            if (MoreStrings.containsOnlyDecimalCharacters(stringValue)) {
                return Float.parseFloat(stringValue);
            }
        }
        return defaultValue;
    }
    
    protected Set<Specialization> specializations(String specializationString) {
        if (specializationString != null) {
            ImmutableSet.Builder<Specialization> specializations = ImmutableSet.builder();
            for (String s : Splitter.on(",").omitEmptyStrings().trimResults().split(specializationString)) {
                Maybe<Specialization> specialization = Specialization.fromKey(s);
                if (specialization.hasValue()) {
                    specializations.add(specialization.requireValue());
                }
            }
            return specializations.build();
        } else {
            return Sets.newHashSet();
        }
    }
}
