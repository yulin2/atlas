package org.atlasapi.query.v2;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.beans.AtlasErrorSummary;
import org.atlasapi.beans.AtlasModelWriter;
import org.atlasapi.persistence.content.query.KnownTypeQueryExecutor;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
import org.atlasapi.query.content.parser.ApplicationConfigurationIncludingQueryBuilder;
import org.atlasapi.query.content.parser.QueryStringBackedQueryBuilder;
import org.atlasapi.query.content.parser.WebProfileDefaultQueryAttributesSetter;
import org.joda.time.DateTime;

import com.google.common.base.Splitter;
import com.metabroadcast.common.time.DateTimeZones;

public abstract class BaseController {

    protected static final Splitter URI_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
    
    protected final KnownTypeQueryExecutor executor;
    protected final ApplicationConfigurationIncludingQueryBuilder builder;
    
    protected final AdapterLog log;
    protected final AtlasModelWriter outputter;
    
    protected BaseController(KnownTypeQueryExecutor executor, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter outputter) {
        this.executor = executor;
        this.log = log;
        this.outputter = outputter;
        this.builder = new ApplicationConfigurationIncludingQueryBuilder(new QueryStringBackedQueryBuilder(new WebProfileDefaultQueryAttributesSetter()), configFetcher) ;
    }
    
    protected void errorViewFor(HttpServletRequest request, HttpServletResponse response, AtlasErrorSummary ae) throws IOException {
        log.record(new AdapterLogEntry(ae.id(), Severity.ERROR, new DateTime(DateTimeZones.UTC)).withCause(ae.exception()).withSource(this.getClass()));
        outputter.writeError(request, response, ae);
    }
    
    @SuppressWarnings("unchecked")
    protected void modelAndViewFor(HttpServletRequest request, HttpServletResponse response, Collection<?> queryResults) throws IOException {
        if (queryResults == null) {
            errorViewFor(request, response, AtlasErrorSummary.forException(new Exception("Query result was null")));
        }
        outputter.writeTo(request, response, (Collection<Object>) queryResults);
    }
}
