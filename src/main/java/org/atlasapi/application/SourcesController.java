package org.atlasapi.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.SourceStatus.SourceState;
import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.persistence.application.ApplicationStore;
import org.atlasapi.query.common.Query;
import org.atlasapi.query.common.QueryExecutionException;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.QueryParseException;
import org.atlasapi.query.common.QueryResult;
import org.atlasapi.query.common.StandardQueryParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.metabroadcast.common.ids.NumberToShortStringCodec;

@Controller
public class SourcesController {
    private final StandardQueryParser<Publisher> queryParser;
    private final QueryExecutor<Publisher> queryExecutor;
    private final QueryResultWriter<Publisher> resultWriter;
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final NumberToShortStringCodec idCodec;
    private final SourceIdCodec sourceIdCodec;
    private final ApplicationStore applicationStore;
    
    public SourcesController(StandardQueryParser<Publisher> queryParser,
            QueryExecutor<Publisher> queryExecutor,
            QueryResultWriter<Publisher> resultWriter,
            NumberToShortStringCodec idCodec,
            SourceIdCodec sourceIdCodec,
            ApplicationStore applicationStore) {
        this.queryParser = queryParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
        this.idCodec = idCodec;
        this.sourceIdCodec = sourceIdCodec;
        this.applicationStore = applicationStore;
    }

    /**
     * POST /4.0/sources/:sourceId/applications Updates permission for a source.
     * Post with app id and permission (read/write) required Params: "id":
     * "abc", "permission": "read"
     * @throws NotFoundException 
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications", method = RequestMethod.POST)
    public void writeSourceForApplication(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String permission) throws NotFoundException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        Optional<Publisher> source = sourceIdCodec.decode(sourceId);
        if (source.isPresent()) {
            Id applicationId = Id.valueOf(idCodec.decode(id));
            Permission permissionType = Permission.valueOf(permission.toUpperCase());
            Application existing = applicationStore.applicationFor(applicationId).get();
            Application modified = null;
            if (permissionType.equals(Permission.READ)) {
                modified = existing.enableSource(source.get());
            } else if (permissionType.equals(Permission.WRITE)) {
                modified = existing.addWrites(source.get());
            }
            if (modified != null) {
                applicationStore.updateApplication(modified);
            }
        } else {
            throw new NotFoundException(null);
        }
    }

    /**
     * DELETE /4.0/sources/:sourceId/applications Removes a permission
     * (read/write) from an app on a source. Post with app id and permission
     * needed.
     * @throws QueryExecutionException 
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications", method = RequestMethod.DELETE)
    public void deleteSourceForApplication(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sourceId,
            @RequestParam String id,
            @RequestParam String permission) throws QueryExecutionException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        Optional<Publisher> source = sourceIdCodec.decode(sourceId);
        if (source.isPresent()) {
            Id applicationId = Id.valueOf(idCodec.decode(id));
            Permission permissionType = Permission.valueOf(permission.toUpperCase());
            Application existing = applicationStore.applicationFor(applicationId).get();
            Application modified = null;
            if (permissionType.equals(Permission.READ)) {
                modified = existing.disableSource(source.get());
            } else if (permissionType.equals(Permission.WRITE)) {
                modified = existing.removeWrites(source.get());
            }
            if (modified != null) {
                applicationStore.updateApplication(modified);
            }
        } else {
            throw new QueryExecutionException("No source with id " + sourceId);
        }
    }

    /**
     * POST /4.0/sources/:sourceId/applications/readers/:appId/state Changes
     * state of app for source, e.g. "available", "requested". Params: "state":
     * "available"
     * @throws Exception 
     */
    @RequestMapping(value = "/4.0/sources/{sourceId}/applications/readers/{id}/state", method = RequestMethod.POST)
    public void changeSourceStateForApplication(HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable String sourceId,
            @PathVariable String id,
            @RequestParam String state) throws QueryExecutionException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        Optional<Publisher> source = sourceIdCodec.decode(sourceId);
        if (source.isPresent()) {
            Id applicationId = Id.valueOf(idCodec.decode(id));
            SourceState requestedState = SourceState.valueOf(state.toUpperCase());
            Application existing = applicationStore.applicationFor(applicationId).get();
            applicationStore.updateApplication(
                    existing.changeReadSourceState(source.get(), requestedState));
        } else {
            throw new QueryExecutionException("No source with id " + sourceId);
        }
    }
    
    @RequestMapping({"/4.0/sources/{sid}.*", "/4.0/sources.*"})
    public void listSources(HttpServletRequest request,
            HttpServletResponse response) throws QueryParseException, QueryExecutionException, IOException {
        ResponseWriter writer = writerResolver.writerFor(request, response);
        Query<Publisher> sourcesQuery = queryParser.parse(request);
        QueryResult<Publisher> queryResult = queryExecutor.execute(sourcesQuery);
        resultWriter.write(queryResult, writer);
    }
}
