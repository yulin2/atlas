package org.atlasapi.query.v2;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.common.Id;
import org.atlasapi.media.content.Content;
import org.atlasapi.media.topic.Topic;
import org.atlasapi.output.ErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.output.QueryResult;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.query.Selection;

@Controller
public class TopicController extends BaseController<Iterable<Topic>> {


    private final TopicQueryResolver topicResolver;
    private final TopicContentLister contentLister;
    private final QueryController queryController;

    public TopicController(TopicQueryResolver topicResolver, TopicContentLister contentLister, ApplicationConfigurationFetcher configFetcher, AdapterLog log, AtlasModelWriter<Iterable<Topic>> atlasModelOutputter, QueryController queryController) {
        super(configFetcher, log, atlasModelOutputter);
        this.topicResolver = topicResolver;
        this.contentLister = contentLister;
        this.queryController = queryController;
    }

    @RequestMapping(value={"3.0/topics.*","/topics.*"})
    public void topics(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
        try {
            ContentQuery query = builder.build(req);
            modelAndViewFor(req, resp, topicResolver.topicsFor(query), query.getConfiguration());
        } catch (Exception e) {
            errorViewFor(req, resp, ErrorSummary.forException(e));
        }
    }
    
    @RequestMapping(value={"3.0/topics/{id}.*","/topics/{id}.*"})
    public void topic(HttpServletRequest req, HttpServletResponse resp, @PathVariable("id") String id) throws IOException {
        
        ContentQuery query = builder.build(req);
        
        Maybe<Topic> topicForUri = topicResolver.topicForId(Id.valueOf(idCodec.decode(id)));
        
        if(topicForUri.isNothing()) {
            outputter.writeError(req, resp, new ErrorSummary(new NullPointerException(), "TOPIC_NOT_FOUND", HttpStatusCode.NOT_FOUND, "Topic " + id + " not found"));
            return;
        }
        
        Topic topic = topicForUri.requireValue();
        
        if(!query.allowsSource(topic.getPublisher())) {
            outputter.writeError(req, resp, new ErrorSummary(new NullPointerException(),"TOPIC_UNAVAILABLE",HttpStatusCode.FORBIDDEN, "Topic " + id + " unavailable"));
            return;
        }
        
        
        modelAndViewFor(req, resp, ImmutableSet.<Topic>of(topicForUri.requireValue()), query.getConfiguration());
    }
    
    @RequestMapping(value={"3.0/topics/{id}/content.*", "/topics/{id}/content"})
    public void topicContents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("id") String id) throws IOException {
        ContentQuery query = builder.build(req);

        Id decodedId = Id.valueOf(idCodec.decode(id));
        Maybe<Topic> topicForUri = topicResolver.topicForId(decodedId);
        
        if(topicForUri.isNothing()) {
            outputter.writeError(req, resp, new ErrorSummary(new NullPointerException(), "TOPIC_NOT_FOUND", HttpStatusCode.NOT_FOUND, "Topic " + id + " not found"));
            return;
        }
        
        Topic topic = topicForUri.requireValue();
        
        if(!query.allowsSource(topic.getPublisher())) {
            outputter.writeError(req, resp, new ErrorSummary(new NullPointerException(),"TOPIC_UNAVAILABLE",HttpStatusCode.FORBIDDEN, "Topic " + id + " unavailable"));
            return;
        }
        
        try {
            Selection selection = query.getSelection();
            QueryResult<Content, Topic> result = QueryResult.of(query.getSelection().apply(iterable(contentLister.contentForTopic(decodedId, query))), topic);
            queryController.modelAndViewFor(req, resp, result.withSelection(selection), query.getConfiguration());
        } catch (Exception e) {
            errorViewFor(req, resp, ErrorSummary.forException(e));
        }
    }

    private Iterable<Content> iterable(final Iterator<Content> iterator) {
        return new Iterable<Content>() {
            @Override
            public Iterator<Content> iterator() {
                return iterator;
            }
        };
    }
     
}
