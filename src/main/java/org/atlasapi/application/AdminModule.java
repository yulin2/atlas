package org.atlasapi.application;

import org.atlasapi.application.model.Application;
import org.atlasapi.application.model.SourceReadEntry;
import org.atlasapi.application.model.deserialize.IdDeserializer;
import org.atlasapi.application.model.deserialize.PublisherDeserializer;
import org.atlasapi.application.model.deserialize.SourceReadEntryDeserializer;
import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.writers.ApplicationListWriter;
import org.atlasapi.application.writers.ApplicationQueryResultWriter;
import org.atlasapi.content.criteria.attribute.Attributes;
import org.atlasapi.input.GsonModelReader;
import org.atlasapi.input.ModelReader;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.entity.Publisher;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.metabroadcast.common.webapp.serializers.JodaDateTimeSerializer;
import org.atlasapi.application.persistence.ApplicationIdProvider;
import org.atlasapi.application.persistence.ApplicationStore;
import org.joda.time.DateTime;

// TODO merge with ApplicationModule
@Configuration
public class AdminModule {

    private @Autowired ApplicationConfigurationFetcher configFetcher;
    private @Autowired ApplicationStore deerApplicationsStore;
    private @Autowired ApplicationIdProvider applicationIdProvider;
    private final NumberToShortStringCodec idCodec = SubstitutionTableNumberCodec.lowerCaseOnly();;
    
    private final JsonDeserializer<Id> ID_DESERIALIZER = new IdDeserializer(idCodec);
    private final JsonDeserializer<DateTime> DATETIME_DESERIALIZER = new JodaDateTimeSerializer();
    private final JsonDeserializer<SourceReadEntry> READS_DESERIALIZER = new SourceReadEntryDeserializer();
    private final JsonDeserializer<Publisher> PUBLISHER_DESERIALIZER = new PublisherDeserializer();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DateTime.class, DATETIME_DESERIALIZER)
            .registerTypeAdapter(Id.class, ID_DESERIALIZER)
            .registerTypeAdapter(SourceReadEntry.class, READS_DESERIALIZER)
            .registerTypeAdapter(Publisher.class, PUBLISHER_DESERIALIZER)
            .create();

    @Bean
    public ApplicationAdminController applicationAdminController() {
        return new ApplicationAdminController(
                applicationQueryParser(),
                applicationQueryExecutor(),
                new ApplicationQueryResultWriter(applicationListWriter()),
                gsonModelReader(),
                applicationUpdater());
    }
    
    @Bean 
    public SourcesController sourcesController() {
        return new SourcesController(applicationUpdater());
    }
    
    @Bean 
    protected ApplicationUpdater applicationUpdater() {
        return new ApplicationUpdater(deerApplicationsStore,
                applicationIdProvider);
    }

    @Bean
    protected ModelReader gsonModelReader() {
        return new GsonModelReader(gson);
    }

    @Bean
    protected EntityListWriter<Application> applicationListWriter() {
        return new ApplicationListWriter(idCodec);
    }

    @Bean
    ResourceAnnotationIndex applicationAnnotationIndex() {
        return ResourceAnnotationIndex.builder(Resource.APPLICATION, Annotation.all()).build();
    }

    @Bean
    protected QueryExecutor<Application> applicationQueryExecutor() {
        return new ApplicationQueryExecutor(deerApplicationsStore);
    }

    @Bean
    SelectionBuilder selectionBuilder() {
        return Selection.builder().withDefaultLimit(50).withMaxLimit(100);
    }

    private StandardQueryParser<Application> applicationQueryParser() {
        QueryContextParser contextParser = new QueryContextParser(configFetcher,
                new IndexAnnotationsExtractor(applicationAnnotationIndex()), selectionBuilder());

        return new StandardQueryParser<Application>(Resource.APPLICATION,
                new QueryAttributeParser(ImmutableList.of(
                        QueryAtomParser.valueOf(Attributes.ID,
                                AttributeCoercers.idCoercer(idCodec))
                        )),
                idCodec, contextParser);
    }

}
