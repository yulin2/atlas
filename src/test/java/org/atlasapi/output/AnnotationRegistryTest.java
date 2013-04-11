package org.atlasapi.output;

import static org.atlasapi.output.Annotation.DESCRIPTION;
import static org.atlasapi.output.Annotation.EXTENDED_DESCRIPTION;
import static org.atlasapi.output.Annotation.EXTENDED_ID;
import static org.atlasapi.output.Annotation.ID;
import static org.atlasapi.output.Annotation.ID_SUMMARY;
import static org.atlasapi.output.Annotation.SERIES_REFERENCE;
import static org.atlasapi.output.Annotation.SERIES_SUMMARY;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.atlasapi.media.entity.Item;
import org.atlasapi.output.AnnotationRegistry.AnnotationSet;
import org.atlasapi.output.annotation.DescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedDescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedIdentificationAnnotation;
import org.atlasapi.output.annotation.IdentificationAnnotation;
import org.atlasapi.output.annotation.IdentificationSummaryAnnotation;
import org.atlasapi.output.annotation.OutputAnnotation;
import org.atlasapi.output.annotation.SeriesReferenceAnnotation;
import org.atlasapi.output.annotation.SeriesSummaryAnnotation;
import org.atlasapi.persistence.output.ContainerSummaryResolver;
import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;

public class AnnotationRegistryTest {

    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();
    private final IdentificationSummaryAnnotation idSum = new IdentificationSummaryAnnotation(
        idCodec);
    private final IdentificationAnnotation ident = new IdentificationAnnotation();
    private final ExtendedIdentificationAnnotation extIdent = new ExtendedIdentificationAnnotation(idCodec);
    private final DescriptionAnnotation desc = new DescriptionAnnotation();
    private final ExtendedDescriptionAnnotation extDesc = new ExtendedDescriptionAnnotation();
    private final SeriesReferenceAnnotation seriesRef = new SeriesReferenceAnnotation(idCodec);
    private final SeriesSummaryAnnotation seriesSum = new SeriesSummaryAnnotation(mock(ContainerSummaryResolver.class));
    
    private final AnnotationRegistry registry = AnnotationRegistry.builder()
        .register(ID_SUMMARY, idSum)
        .register(ID, ident, ImmutableSet.of(ID_SUMMARY))
        .register(EXTENDED_ID, extIdent, ImmutableSet.of(ID))
        .register(SERIES_REFERENCE, seriesRef, ImmutableSet.of(ID_SUMMARY))
        .register(SERIES_SUMMARY, seriesSum, ImmutableSet.of(ID_SUMMARY), ImmutableSet.of(SERIES_REFERENCE))
        .register(DESCRIPTION, desc, ImmutableSet.of(ID, SERIES_REFERENCE))
        .register(EXTENDED_DESCRIPTION, extDesc, ImmutableSet.of(DESCRIPTION, EXTENDED_ID))
        .build();

    @Test
    public void testMapsToOnlyTopLevelOutputAnnotation() {

        AnnotationSet set = registry.activeAnnotations(ImmutableSet.of(ID_SUMMARY));
        List<OutputAnnotation<? super Item>> mapped = set.map(Item.class, ID_SUMMARY);

        assertTrue("mapped annotation should be id summary", idSum.equals(mapped.get(0)));

    }

    @Test
    public void testMapsImpliedAnnotations() {

        AnnotationSet set = registry.activeAnnotations(ImmutableSet.of(ID));
        List<OutputAnnotation<? super Item>> mapped = set.map(Item.class, ID);

        assertTrue("1st mapped annotation should be id summary", idSum.equals(mapped.get(0)));
        assertTrue("2nd mapped annotation should be identification", ident.equals(mapped.get(1)));

    }

    @Test
    public void testTransitivelyImpliedAnnotations() {

        AnnotationSet set = registry.activeAnnotations(ImmutableSet.of(EXTENDED_DESCRIPTION));
        List<OutputAnnotation<? super Item>> mapped = set.map(Item.class, ID);

        assertTrue("1st mapped annotation should be id summary", idSum.equals(mapped.get(0)));
        assertTrue("2nd mapped annotation should be identification", ident.equals(mapped.get(1)));
        assertTrue("3rd mapped annotation should be ext ident", extIdent.equals(mapped.get(2)));
        assertTrue("4th mapped annotation should be desc", desc.equals(mapped.get(3)));
        assertTrue("5th mapped annotation should be ext desc", extDesc.equals(mapped.get(4)));
        assertTrue("6th mapped annotation should be series ref", seriesRef.equals(mapped.get(5)));

    }

    @Test
    public void testOverridesAnnotations() {
        
        AnnotationSet set = registry.activeAnnotations(ImmutableSet.of(EXTENDED_DESCRIPTION,SERIES_SUMMARY));
        List<OutputAnnotation<? super Item>> mapped = set.map(Item.class, ID);
        
        assertTrue("1st mapped annotation should be id summary", idSum.equals(mapped.get(0)));
        assertTrue("2nd mapped annotation should be identification", ident.equals(mapped.get(1)));
        assertTrue("3rd mapped annotation should be ext ident", extIdent.equals(mapped.get(2)));
        assertTrue("4th mapped annotation should be desc", desc.equals(mapped.get(3)));
        assertTrue("5th mapped annotation should be ext desc", extDesc.equals(mapped.get(4)));
        assertTrue("6th mapped annotation should be series summary", seriesSum.equals(mapped.get(5)));
        
    }

    @Test
    public void testOverridesWithMultipleImplyingAnnotations() {
        
        ImmutableSet<Annotation> annotations = ImmutableSet.of(EXTENDED_DESCRIPTION,SERIES_SUMMARY, DESCRIPTION);
        for (List<Annotation> annotationList : Collections2.permutations(annotations)) {
            AnnotationSet set = registry.activeAnnotations(annotationList);
            List<OutputAnnotation<? super Item>> mapped = set.map(Item.class, ID);
            
            assertTrue("1st mapped annotation should be id summary", idSum.equals(mapped.get(0)));
            assertTrue("2nd mapped annotation should be identification", ident.equals(mapped.get(1)));
            assertTrue("3rd mapped annotation should be ext ident", extIdent.equals(mapped.get(2)));
            assertTrue("4th mapped annotation should be desc", desc.equals(mapped.get(3)));
            assertTrue("5th mapped annotation should be ext desc", extDesc.equals(mapped.get(4)));
            assertTrue("6th mapped annotation should be series summary not "+mapped.get(5), seriesSum.equals(mapped.get(5)));
        }
        
    }

}
