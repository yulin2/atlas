package org.atlasapi.system;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.output.Annotation;
import org.atlasapi.output.JsonTranslator;
import org.atlasapi.messaging.EntityUpdatedMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.ImmutableSet;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import org.atlasapi.persistence.event.RecentChangeStore;

@Controller
public class RecentChangesController {

    private final RecentChangeStore store;
    private final JsonTranslator<Iterable<EntityUpdatedMessage>> translator;
    private final ApplicationConfiguration configuration;
    private final SelectionBuilder selectionBuilder;

    public RecentChangesController(RecentChangeStore store) {
        this.store = store;
        this.translator = new JsonTranslator<Iterable<EntityUpdatedMessage>>();
        this.configuration = ApplicationConfiguration.DEFAULT_CONFIGURATION;
        this.selectionBuilder = Selection.builder().withDefaultLimit(30).withMaxLimit(100);
    }

    @RequestMapping("system/update/changes")
    public void listChanges(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Iterable<EntityUpdatedMessage> model = store.changes();
        model = selectionBuilder.build(req).apply(model);
        translator.writeTo(req, resp, model, ImmutableSet.<Annotation>of(), configuration);
    }
}