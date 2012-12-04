package org.atlasapi.output;

import static org.atlasapi.output.Annotation.EXTENDED_DESCRIPTION;
import static org.atlasapi.output.Annotation.IDENTIFICATION;
import static org.atlasapi.output.Annotation.IDENTIFICATION_SUMMARY;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.atlasapi.media.entity.Item;
import org.atlasapi.output.annotation.DescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedDescriptionAnnotation;
import org.atlasapi.output.annotation.ExtendedIdentificationAnnotation;
import org.atlasapi.output.annotation.IdentificationAnnotation;
import org.atlasapi.output.annotation.IdentificationSummaryAnnotation;
import org.atlasapi.output.annotation.OutputAnnotation;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;


public class AnnotationRegistryTest {

    private final IdentificationSummaryAnnotation idSum = new IdentificationSummaryAnnotation(SubstitutionTableNumberCodec.lowerCaseOnly());
    private final IdentificationAnnotation ident = new IdentificationAnnotation(idSum);
    private final ExtendedIdentificationAnnotation extIdent = new ExtendedIdentificationAnnotation(ident);
    private final DescriptionAnnotation desc = new DescriptionAnnotation(ident);
    private final ExtendedDescriptionAnnotation extDesc = new ExtendedDescriptionAnnotation(desc, extIdent);
    private final AnnotationRegistry registry = new AnnotationRegistry(ImmutableSet.<OutputAnnotation<?>>of(
        idSum, ident, extIdent, desc, extDesc
    ));

    @Test
    public void testMapsToOnlyTopLevelOutputAnnotation() {
        
        List<OutputAnnotation<? super Item>> mapped
                = registry.map(ImmutableSet.of(IDENTIFICATION_SUMMARY), Item.class);
        
        assertTrue("mapped annotation should be id summary", idSum.equals(mapped.get(0)));

    }

    @Test
    public void testMapsImpliedAnnotations() {
        
        List<OutputAnnotation<? super Item>> mapped
                = registry.map(ImmutableSet.of(IDENTIFICATION), Item.class);
        
        assertTrue("1st mapped annotation should be id summary", idSum.equals(mapped.get(0)));
        assertTrue("2nd mapped annotation should be identification", ident.equals(mapped.get(1)));
        
    }

    @Test
    public void testTransitivelyImpliedAnnotations() {
        
        List<OutputAnnotation<? super Item>> mapped
        = registry.map(ImmutableSet.of(EXTENDED_DESCRIPTION), Item.class);
        System.out.println(mapped);
        assertTrue("1st mapped annotation should be id summary", idSum.equals(mapped.get(0)));
        assertTrue("2nd mapped annotation should be identification", ident.equals(mapped.get(1)));
        assertTrue("3rd mapped annotation should be ext ident", extIdent.equals(mapped.get(2)));
        assertTrue("4th mapped annotation should be desc", desc.equals(mapped.get(3)));
        assertTrue("5th mapped annotation should be ext desc", extDesc.equals(mapped.get(4)));
        
    }

}
