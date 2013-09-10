package org.atlasapi.application;

import java.lang.reflect.Type;
import java.util.Map;

import org.atlasapi.application.model.Application;
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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.metabroadcast.common.ids.NumberToShortStringCodec;
import com.metabroadcast.common.ids.SubstitutionTableNumberCodec;
import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

import org.atlasapi.application.persistence.ApplicationStore2;
import org.atlasapi.application.persistence.MongoApplicationStore2;
import org.joda.time.DateTime;

// TODO merge with ApplicationModule
@Configuration
public class AdminModule {
    
    private @Autowired ApplicationConfigurationFetcher configFetcher;
    private @Autowired ApplicationStore2 deerApplicationsStore;
    
    private final Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new JsonDeserializer<DateTime>() {

        @Override
        public DateTime deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return DateTime.parse(json.getAsJsonPrimitive().getAsString());
        }
        
    }).registerTypeAdapter(Id.class, new JsonDeserializer<Id>() {

        @Override
        public Id deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            NumberToShortStringCodec idCodec = idCodec();
            return Id.valueOf(idCodec.decode(json.getAsJsonPrimitive().getAsString()));
        }
       
    }).registerTypeAdapter(Map.class, new JsonDeserializer<Map<Publisher, SourceStatus>>() {
        @Override
        public Map<Publisher, SourceStatus> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            Map<Publisher, SourceStatus> reads = Maps.newHashMap();
            JsonArray entries = json.getAsJsonArray();
            for (JsonElement entry : entries) {
                JsonObject obj = entry.getAsJsonObject();
                Optional<Publisher> publisher = Publisher.fromPossibleKey(obj.getAsJsonPrimitive("key").getAsString());
                SourceStatus.SourceState sourceState = SourceStatus.SourceState.valueOf(obj.getAsJsonPrimitive("state").getAsString().toUpperCase());
                SourceStatus sourceStatus = new SourceStatus(sourceState, obj.getAsJsonPrimitive("enabled").getAsBoolean());
                reads.put(publisher.get(), sourceStatus);
            }
            return reads;
        }
    }).registerTypeAdapter(Publisher.class, new JsonDeserializer<Publisher>() {
        @Override
        public Publisher deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            Optional<Publisher> publisher = Publisher.fromPossibleKey(json.getAsJsonPrimitive().getAsString());
            return publisher.get();
        }
    }).create();
 
    @Bean
    public ApplicationAdminController applicationAdminController() {
        return new ApplicationAdminController(
                applicationQueryParser(),
                applicationQueryExecutor(),
                new ApplicationQueryResultWriter(applicationListWriter()),
                gsonModelReader()
               );
    }
    
    @Bean 
    protected ModelReader gsonModelReader() {
        return new GsonModelReader(gson);
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
