package org.atlasapi.application;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.sources.SourceIdCodec;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.NotAcceptableException;
import org.atlasapi.output.NotFoundException;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.output.UnsupportedFormatException;
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
public class SourceRequestsController {
    private final StandardQueryParser<SourceRequest> queryParser;
    private final QueryExecutor<SourceRequest> queryExecutor;
    private final QueryResultWriter<SourceRequest> resultWriter;
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final SourceRequestManager sourceRequestManager;
    private final NumberToShortStringCodec idCodec;
    private final SourceIdCodec sourceIdCodec;
    
    public SourceRequestsController(StandardQueryParser<SourceRequest> queryParser,
            QueryExecutor<SourceRequest> queryExecutor,
            QueryResultWriter<SourceRequest> resultWriter,
            SourceRequestManager sourceRequestManager,
            NumberToShortStringCodec idCodec,
            SourceIdCodec sourceIdCodec) {
        this.queryParser = queryParser;
        this.queryExecutor = queryExecutor;
        this.resultWriter = resultWriter;
        this.sourceRequestManager = sourceRequestManager;
        this.idCodec = idCodec;
        this.sourceIdCodec = sourceIdCodec;
    }
    
    @RequestMapping(value = {"/4.0/requests.*", "/4.0/requests/{id}.*"}, method = RequestMethod.GET)
    public void listSourceRequests(HttpServletRequest request, 
            HttpServletResponse response) throws IOException, QueryParseException, QueryExecutionException {
        ResponseWriter writer = null;
        writer = writerResolver.writerFor(request, response);
        Query<SourceRequest> sourcesQuery = queryParser.parse(request);
        QueryResult<SourceRequest> queryResult = queryExecutor.execute(sourcesQuery);
        resultWriter.write(queryResult, writer);
    }
  
    @RequestMapping(value = "/4.0/sources/{sid}/requests", method = RequestMethod.POST)
    public void storeSourceRequest(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String sid,
            @RequestParam String appId,
            @RequestParam String appUrl,
            @RequestParam String email,
            @RequestParam String reason,
            @RequestParam String usageType) throws UnsupportedFormatException, NotAcceptableException, IOException {

        response.addHeader("Access-Control-Allow-Origin", "*");
        Optional<Publisher> source =sourceIdCodec.decode(sid);
        if (source.isPresent()) {
            Id applicationId = Id.valueOf(idCodec.decode(appId));
            UsageType usageTypeRequested = UsageType.valueOf(usageType.toUpperCase());
            sourceRequestManager.createOrUpdateRequest(source.get(), usageTypeRequested,
                    applicationId, appUrl, email, reason);
        } else {
            sendError(request, response,  404);
        }      
    }
    
    @RequestMapping(value = "/4.0/requests/{rid}/approve", method = RequestMethod.POST)
    public void storeSourceRequest(HttpServletRequest request, 
            HttpServletResponse response,
            @PathVariable String rid) throws IOException {
        response.addHeader("Access-Control-Allow-Origin", "*");
        Id requestId = Id.valueOf(idCodec.decode(rid));
        try {
            sourceRequestManager.approveSourceRequest(requestId);
        } catch (NotFoundException e) {
            sendError(request, response,  404);
        }
    }
    
    public void sendError(HttpServletRequest request,
            HttpServletResponse response,
            int responseCode) throws IOException {
        response.setStatus(responseCode);
    }

}
