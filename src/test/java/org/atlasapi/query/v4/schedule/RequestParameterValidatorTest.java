package org.atlasapi.query.v4.schedule;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.servlet.StubHttpServletRequest;

public class RequestParameterValidatorTest {

    RequestParameterValidator validator = new RequestParameterValidator(
        ImmutableSet.of("from","to","publisher"), ImmutableSet.of("apiKey","annotations")
    );
    
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
        validator.validateParameters(requestWithParams("from","to","wibble"));
    }

    private HttpServletRequest requestWithParams(String... params) {
        StubHttpServletRequest request = new StubHttpServletRequest();
        for (String param : params) {
            request.withParam(param, param);
        }
        return request;
    }

}
