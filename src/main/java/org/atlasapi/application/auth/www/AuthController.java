package org.atlasapi.application.auth.www;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.model.auth.OAuthProvider;
import org.atlasapi.output.NotAcceptableException;
import org.atlasapi.output.QueryResultWriter;
import org.atlasapi.output.ResponseWriter;
import org.atlasapi.output.ResponseWriterFactory;
import org.atlasapi.output.UnsupportedFormatException;
import org.atlasapi.query.common.QueryContext;
import org.atlasapi.query.common.QueryResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AuthController {
    private final ResponseWriterFactory writerResolver = new ResponseWriterFactory();
    private final QueryResultWriter<OAuthProvider> resultWriter;
    
    public AuthController(QueryResultWriter<OAuthProvider> resultWriter) {
        this.resultWriter = resultWriter;
    }
    
    @RequestMapping(value = { "/4.0/auth/providers.*" }, method = RequestMethod.GET) 
    public void listAuthProviders(HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedFormatException, NotAcceptableException, IOException {
        ResponseWriter writer = writerResolver.writerFor(request, response);
        QueryResult<OAuthProvider> queryResult = QueryResult.listResult(OAuthProvider.all(), QueryContext.standard());
        resultWriter.write(queryResult, writer);
    }
}
