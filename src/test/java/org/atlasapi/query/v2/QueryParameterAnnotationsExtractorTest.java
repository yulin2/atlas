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
        Optional<Set<Annotation>> extracted = extractor.extractFromKeys(new StubHttpServletRequest());
        assertFalse(extracted.isPresent());
    }
    
    @Test
    public void testExtractsAnnotationsFromKeys() {
        HttpServletRequest request = requestWithAnnotationParameter("description,extended_description");
        Optional<Set<Annotation>> extracted = extractor.extractFromKeys(request);
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidAnnotationKey() {
        HttpServletRequest request = requestWithAnnotationParameter("descroption,extended_annotation");
        extractor.extractFromKeys(request);
    }

    @Test
    public void testExtractsAnnotationsFromNamesWithContext() {
        HttpServletRequest request = requestWithAnnotationParameter("description,extended_description");
        Optional<Set<Annotation>> extracted = extractor.extractFromRequest(request, Optional.of("content"));
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidAnnotationNameWithContext() {
        HttpServletRequest request = requestWithAnnotationParameter("descroption,extended_annotation");
        extractor.extractFromRequest(request, Optional.of("content"));
    }

    @Test
    public void testExtractsAnnotationsFromNamesWithoutContext() {
        HttpServletRequest request = requestWithAnnotationParameter("content.description,content.extended_description");
        Optional<Set<Annotation>> extracted = extractor.extractFromRequest(request, Optional.<String>absent());
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidAnnotationNameWithoutContext() {
        HttpServletRequest request = requestWithAnnotationParameter("content.descroption,content.extended_annotation");
        extractor.extractFromRequest(request, Optional.<String>absent());
    }
    
    private HttpServletRequest requestWithAnnotationParameter(String string) {
        return new StubHttpServletRequest().withParam("annotations", string);
    }

}
