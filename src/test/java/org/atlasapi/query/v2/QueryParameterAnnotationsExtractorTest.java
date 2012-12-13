package org.atlasapi.query.v2;

import static org.junit.Assert.*;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.output.Annotation;
import org.junit.Test;

import com.google.common.base.Optional;
import com.metabroadcast.common.servlet.StubHttpServletRequest;

public class QueryParameterAnnotationsExtractorTest {

    private final QueryParameterAnnotationsExtractor extractor = new QueryParameterAnnotationsExtractor();

    @Test
    public void testExtractsAbsentWhenParameterMissing() {
        Optional<Set<Annotation>> extracted = extractor.extract(new StubHttpServletRequest());
        assertFalse(extracted.isPresent());
    }
    
    @Test
    public void testExtractsAnnotations() {
        HttpServletRequest request = requestWithAnnotationParameter("description,extended_description");
        Optional<Set<Annotation>> extracted = extractor.extract(request);
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidParameterName() {
        HttpServletRequest request = requestWithAnnotationParameter("descroption,extended_annotation");
        extractor.extract(request);
    }
    
    private HttpServletRequest requestWithAnnotationParameter(String string) {
        return new StubHttpServletRequest().withParam("annotations", string);
    }

}
