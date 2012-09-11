package org.atlasapi.query.v4.search;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.query.IpCheckingApiKeyConfigurationFetcher;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.output.JsonTranslator;
import org.atlasapi.output.QueryResult;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.search.model.SearchQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.text.MoreStrings;
import org.atlasapi.media.entity.Specialization;
import org.atlasapi.persistence.content.SearchResolver;
import org.atlasapi.query.v2.BaseController;
import org.atlasapi.query.v2.ParameterChecker;

@Controller
public class SearchController extends BaseController<QueryResult<Content, ? extends Identified>> {

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
    private final ParameterChecker paramChecker = new ParameterChecker(ImmutableSet.of(IpCheckingApiKeyConfigurationFetcher.API_KEY_QUERY_PARAMETER, Selection.LIMIT_REQUEST_PARAM,
            Selection.START_INDEX_REQUEST_PARAM, QUERY_PARAM, SPECIALIZATION_PARAM, PUBLISHER_PARAM, TITLE_WEIGHTING_PARAM, BROADCAST_WEIGHTING_PARAM, CATCHUP_WEIGHTING_PARAM, JsonTranslator.CALLBACK));

    public SearchController(SearchResolver searcher, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter<QueryResult<Content, ? extends Identified>> outputter) {
        super(configFetcher, log, outputter);
        this.searcher = searcher;
    }

    @RequestMapping("/4.0/search.*")
    public void search(@RequestParam(QUERY_PARAM) String q,
            @RequestParam(value = SPECIALIZATION_PARAM, required = false) String specialization,
            @RequestParam(value = PUBLISHER_PARAM, required = false) String publisher,
            @RequestParam(value = TITLE_WEIGHTING_PARAM, required = false) String titleWeightingParam,
            @RequestParam(value = BROADCAST_WEIGHTING_PARAM, required = false) String broadcastWeightingParam,
            @RequestParam(value = CATCHUP_WEIGHTING_PARAM, required = false) String catchupWeightingParam, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
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

            ApplicationConfiguration appConfig = appConfig(request);
            Set<Specialization> specializations = specializations(specialization);
            Set<Publisher> publishers = publishers(publisher, appConfig);
            List<Identified> content = searcher.search(new SearchQuery(q, selection, specializations, publishers, titleWeighting, broadcastWeighting, catchupWeighting), appConfig);

            modelAndViewFor(request, response, QueryResult.of(Iterables.filter(content, Content.class)), appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }

    private float getFloatParam(String stringValue, float defaultValue) {
        if (!Strings.isNullOrEmpty(stringValue)) {
            if (MoreStrings.containsOnlyDecimalCharacters(stringValue)) {
                return Float.parseFloat(stringValue);
            }
        }
        return defaultValue;
    }
}
