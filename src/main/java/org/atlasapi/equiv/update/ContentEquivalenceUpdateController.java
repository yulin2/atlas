package org.atlasapi.equiv.update;

import static com.metabroadcast.common.http.HttpStatusCode.BAD_REQUEST;
import static com.metabroadcast.common.http.HttpStatusCode.NOT_FOUND;
import static com.metabroadcast.common.http.HttpStatusCode.OK;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Identified;
import org.atlasapi.persistence.content.ContentResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContentEquivalenceUpdateController {

    private final RootEquivalenceUpdater contentUpdater;
    private final ContentResolver contentResolver;
    private final ExecutorService executor;

    public ContentEquivalenceUpdateController(RootEquivalenceUpdater contentUpdater, ContentResolver contentResolver) {
        this.contentUpdater = contentUpdater;
        this.contentResolver = contentResolver;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @RequestMapping(value = "/system/equivalence/update", method = RequestMethod.POST)
    public void runUpdate(HttpServletResponse response, @RequestParam(value = "uri", required = true) String uri) throws IOException {

        Identified resolved = contentResolver.findByCanonicalUri(uri);

        if (resolved == null) {
            response.setStatus(NOT_FOUND.code());
            response.setContentLength(0);
            return;
        }

        Content content;
        try {
            content = (Content) resolved;
        } catch (ClassCastException cce) {
            response.sendError(BAD_REQUEST.code(), "Not Content");
            return;
        }

        executor.submit(updateFor(content));
        response.setStatus(OK.code());

    }

    private Runnable updateFor(final Content content) {
        return new Runnable() {
            @Override
            public void run() {
                contentUpdater.updateEquivalences(content);
            }
        };
    }

}
