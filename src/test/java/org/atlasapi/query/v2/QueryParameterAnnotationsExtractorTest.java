package org.atlasapi.query.v2;

import static org.junit.Assert.*;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.atlasapi.output.Annotation;
import org.atlasapi.query.common.AnnotationsExtractor;
import org.atlasapi.query.common.QueryParameterAnnotationsExtractor;
import org.junit.Test;

import com.google.common.base.Optional;
import com.metabroadcast.common.servlet.StubHttpServletRequest;

public class QueryParameterAnnotationsExtractorTest {

    private final QueryParameterAnnotationsExtractor contextlessExtractor = new QueryParameterAnnotationsExtractor();
    private final AnnotationsExtractor contentContextExtractor = new QueryParameterAnnotationsExtractor("content");

    @Test
    public void testExtractsAbsentWhenParameterMissing() {
        Optional<Set<Annotation>> extracted = contextlessExtractor.extractFromKeys(new StubHttpServletRequest());
        assertFalse(extracted.isPresent());
    }
    
    @Test
    public void testExtractsAnnotationsFromKeys() {
        HttpServletRequest request = requestWithAnnotationParameter("description,extended_description");
        Optional<Set<Annotation>> extracted = contextlessExtractor.extractFromKeys(request);
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidAnnotationKey() {
        HttpServletRequest request = requestWithAnnotationParameter("descroption,extended_annotation");
        contextlessExtractor.extractFromKeys(request);
    }

    @Test
    public void testExtractsAnnotationsFromNamesWithContext() {
        HttpServletRequest request = requestWithAnnotationParameter("description,extended_description");
        Optional<Set<Annotation>> extracted = contentContextExtractor.extractFromRequest(request);
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidAnnotationNameWithContext() {
        HttpServletRequest request = requestWithAnnotationParameter("descroption,extended_annotation");
        contentContextExtractor.extractFromRequest(request);
    }

    @Test
    public void testExtractsAnnotationsFromNamesWithoutContext() {
        HttpServletRequest request = requestWithAnnotationParameter("content.description,content.extended_description");
        Optional<Set<Annotation>> extracted = contextlessExtractor.extractFromRequest(request);
        assertTrue(extracted.isPresent());
        assertTrue(extracted.get().contains(Annotation.DESCRIPTION));
        assertTrue(extracted.get().contains(Annotation.EXTENDED_DESCRIPTION));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testThrowsExceptionForInvalidAnnotationNameWithoutContext() {
        HttpServletRequest request = requestWithAnnotationParameter("content.descroption,content.extended_annotation");
        contextlessExtractor.extractFromRequest(request);
    }
    
    private HttpServletRequest requestWithAnnotationParameter(String string) {
        return new StubHttpServletRequest().withParam("annotations", string);
    }

}
