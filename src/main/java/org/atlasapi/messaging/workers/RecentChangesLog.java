package org.atlasapi.messaging.workers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.JsonTranslator;
import org.atlasapi.persistence.messaging.event.EntityUpdatedEvent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;

@Controller
public class RecentChangesLog extends AbstractWorker {

    
    private final RecentChangeStore store;
    private final JsonTranslator<Iterable<EntityUpdatedEvent>> translator;
    private final ApplicationConfiguration configuration;
    private final SelectionBuilder selectionBuilder;

    public RecentChangesLog(RecentChangeStore store) {
        this.store = store;
        this.translator = new JsonTranslator<Iterable<EntityUpdatedEvent>>();
        this.configuration = ApplicationConfiguration.DEFAULT_CONFIGURATION;
        this.selectionBuilder =  Selection.builder().withDefaultLimit(30)
                .withMaxLimit(100);
    }

    @Override
    public void process(EntityUpdatedEvent command) {
        store.logChange(command);
    }

    @RequestMapping("system/update/changes")
    public void listChanges(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        Iterable<EntityUpdatedEvent> model = store.changes();
        model = selectionBuilder.build(req).apply(model);
        translator.writeTo(req, resp, model, ImmutableSet.<Annotation>of(), configuration);
        
    }
    
}