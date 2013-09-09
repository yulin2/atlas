package org.atlasapi.application;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.writers.ApplicationListWriter;
import org.atlasapi.application.writers.ApplicationQueryResultWriter;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.EntityListWriter;
import org.atlasapi.query.annotation.ResourceAnnotationIndex;
import org.atlasapi.query.common.AttributeCoercers;
import org.atlasapi.query.common.IndexAnnotationsExtractor;
import org.atlasapi.query.common.QueryAtomParser;
import org.atlasapi.query.common.QueryAttributeParser;
import org.atlasapi.query.common.QueryContextParser;
import org.atlasapi.query.common.QueryExecutor;
import org.atlasapi.query.common.Resource;
import org.atlasapi.query.common.StandardQueryParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

import org.atlasapi.application.persistence.ApplicationStore2;
import org.atlasapi.application.persistence.MongoApplicationStore2;

// TODO merge with ApplicationModule
@Configuration
public class AdminModule {
    
    private @Autowired ApplicationConfigurationFetcher configFetcher;
    private @Autowired ApplicationStore2 deerApplicationsStore;
    @Bean
    public ApplicationAdminController applicationAdminController() {
        return new ApplicationAdminController(
                applicationQueryParser(),
                applicationQueryExecutor(),
                new ApplicationQueryResultWriter(applicationListWriter())
               );
    }
    
    @Bean
    protected EntityListWriter<Application> applicationListWriter() {
        return new ApplicationListWriter();
    }
    
    @Bean
    ResourceAnnotationIndex applicationAnnotationIndex() {
        return ResourceAnnotationIndex.builder(Resource.APPLICATION, Annotation.all()).build();
    }
    
    @Bean
    protected QueryExecutor<Application> applicationQueryExecutor() {
        return new ApplicationQueryExecutor(deerApplicationsStore);
    }
    
    @Bean SelectionBuilder  selectionBuilder() {
        return Selection.builder().withDefaultLimit(50).withMaxLimit(100);
    }
    
    @Bean NumberToShortStringCodec idCodec() {
        return SubstitutionTableNumberCodec.lowerCaseOnly();
    }   
    
    private StandardQueryParser<Application> applicationQueryParser() {
        QueryContextParser contextParser = new QueryContextParser(configFetcher, 
        new IndexAnnotationsExtractor(applicationAnnotationIndex()), selectionBuilder());
        
        return new StandardQueryParser<Application>(Resource.APPLICATION, 
            new QueryAttributeParser(ImmutableList.of(
                QueryAtomParser.valueOf(Attributes.ID, AttributeCoercers.idCoercer(idCodec()))
            )),
            idCodec(), contextParser
        );
    }
 
    
}
