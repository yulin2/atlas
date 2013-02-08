package org.atlasapi.query.v4.schedule;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.query.common.RequestParameterValidator;
import org.junit.Test;

import com.metabroadcast.common.servlet.StubHttpServletRequest;

public class RequestParameterValidatorTest {

    RequestParameterValidator validator = RequestParameterValidator.builder()
        .withRequiredParameters("from","to","publisher")
        .withOptionalParameters("apiKey","annotations")
        .build();
    
    @Test
    public void testValidatesParameters() {
        validator.validateParameters(requestWithParams("from","to","publisher"));
        validator.validateParameters(requestWithParams("from","to","publisher","apiKey","annotations"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionMissingParameters() {
        validator.validateParameters(requestWithParams("from","to"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionInvalidParameters() {
        validator.validateParameters(requestWithParams("form","to","wibble"));
    }

    private HttpServletRequest requestWithParams(String... params) {
        StubHttpServletRequest request = new StubHttpServletRequest();
        for (String param : params) {
            request.withParam(param, param);
        }
        return request;
    }

}
