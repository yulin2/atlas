package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.media.entity.Organisation;
import org.atlasapi.media.entity.simple.OrganisationQueryResult;
import org.atlasapi.output.simple.ModelSimplifier;
import org.atlasapi.persistence.content.ContentResolver;

public class SimpleOrganisationModelWriter extends TransformingModelWriter<Iterable<Organisation>, OrganisationQueryResult> {

    private final ModelSimplifier<Organisation, org.atlasapi.media.entity.simple.Organisation> organisationSimplifier;

    public SimpleOrganisationModelWriter(AtlasModelWriter<OrganisationQueryResult> delegate, 
            ContentResolver contentResolver, 
            ModelSimplifier<Organisation, org.atlasapi.media.entity.simple.Organisation> organisationSimplifier) {
        super(delegate);
        this.organisationSimplifier = organisationSimplifier;
    }
    
    @Override
    protected OrganisationQueryResult transform(Iterable<Organisation> fullOrganisations, Set<Annotation> annotations, 
            ApplicationConfiguration config) {
        OrganisationQueryResult result = new OrganisationQueryResult();
        for (Organisation fullOrganisation : fullOrganisations) {
            result.add(organisationSimplifier.simplify(fullOrganisation, annotations, config));
        }
        return result;
    }

}
