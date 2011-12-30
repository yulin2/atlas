package org.atlasapi.query.v2;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.content.criteria.ContentQuery;
import org.atlasapi.media.entity.Topic;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.topic.TopicContentLister;
import org.atlasapi.persistence.topic.TopicQueryResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.metabroadcast.common.base.Maybe;
import com.metabroadcast.common.http.HttpStatusCode;

@Controller
public class TopicController extends BaseController<Topic> {

    private final TopicQueryResolver topicResolver;
    private static final AtlasErrorSummary NOT_FOUND = new AtlasErrorSummary(new NullPointerException()).withErrorCode("TOPIC_NOT_FOUND").withStatusCode(HttpStatusCode.NOT_FOUND);
    private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException()).withErrorCode("TOPIC_NOT_FOUND").withStatusCode(HttpStatusCode.FORBIDDEN);
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
            modelAndViewFor(req, resp, ImmutableList.copyOf(topicResolver.topicsFor(builder.build(req))));
        } catch (Exception e) {
            errorViewFor(req, resp, AtlasErrorSummary.forException(e));
        }
    }
    
    @RequestMapping(value={"3.0/topics/{id}.*","/topics/{id}.*"})
    public void topic(HttpServletRequest req, HttpServletResponse resp, @PathVariable("id") String id) throws IOException {
        
        ContentQuery query = builder.build(req);
        
        String topicUri = Topic.topicUriForId(id);
        Maybe<Topic> topicForUri = topicResolver.topicForUri(topicUri);
        
        if(topicForUri.isNothing()) {
            outputter.writeError(req, resp, NOT_FOUND.withMessage("Topic " + topicUri + " not found"));
            return;
        }
        
        Topic topic = topicForUri.requireValue();
        
        //TODO: train wreck: query.allowsPublisher(publisher)?;
        if(Sets.intersection(query.getConfiguration().getEnabledSources(),topic.getPublishers()).isEmpty()) {
            outputter.writeError(req, resp, FORBIDDEN.withMessage("Topic " + topicUri + " unavailable"));
            return;
        }
        
        
        modelAndViewFor(req, resp, ImmutableSet.<Topic>of(topicForUri.requireValue()));
    }
    
    @RequestMapping(value={"3.0/topics/{id}/content.*", "/topics/{id}/content"})
    public void topicContents(HttpServletRequest req, HttpServletResponse resp, @PathVariable("id") String id) throws IOException {
        ContentQuery query = builder.build(req);
        
        String topicUri = Topic.topicUriForId(id);
        Maybe<Topic> topicForUri = topicResolver.topicForUri(topicUri);
        
        if(topicForUri.isNothing()) {
            outputter.writeError(req, resp, NOT_FOUND.withMessage("Topic " + topicUri + " not found"));
            return;
        }
        
        Topic topic = topicForUri.requireValue();
        
        //TODO: train wreck: query.allowsPublisher(publisher)?;
        if(Sets.intersection(query.getConfiguration().getEnabledSources(),topic.getPublishers()).isEmpty()) {
            outputter.writeError(req, resp, FORBIDDEN.withMessage("Topic " + topicUri + " unavailable"));
            return;
        }
        
        try {
            queryController.modelAndViewFor(req, resp, ImmutableList.copyOf(contentLister.contentForTopic(topicUri, query)));
        } catch (Exception e) {
            errorViewFor(req, resp, AtlasErrorSummary.forException(e));
        }
    }
     
}
