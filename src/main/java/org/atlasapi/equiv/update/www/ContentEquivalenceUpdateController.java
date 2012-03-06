package org.atlasapi.equiv.update.www;

import static com.metabroadcast.common.http.HttpStatusCode.NOT_FOUND;
import static com.metabroadcast.common.http.HttpStatusCode.OK;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.equiv.update.ContentEquivalenceUpdater;
import org.atlasapi.media.entity.Content;
import org.atlasapi.media.entity.Film;
import org.atlasapi.persistence.content.ContentResolver;
import org.atlasapi.persistence.content.ResolvedContent;
import org.atlasapi.persistence.logging.AdapterLog;
import org.atlasapi.persistence.logging.AdapterLogEntry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

@Controller
public class ContentEquivalenceUpdateController {
    
    private final Splitter commaSplitter = Splitter.on(',').trimResults().omitEmptyStrings();

    private final ContentEquivalenceUpdater<Content> contentUpdater;
    private final ContentEquivalenceUpdater<Film> filmUpdater;
    private final ContentResolver contentResolver;
    private final ExecutorService executor;
    private final AdapterLog log;


    public ContentEquivalenceUpdateController(ContentEquivalenceUpdater<Content> contentUpdater, ContentEquivalenceUpdater<Film> filmUpdater, ContentResolver contentResolver, AdapterLog log) {
        this.contentUpdater = contentUpdater;
        this.filmUpdater = filmUpdater;
        this.contentResolver = contentResolver;
        this.log = log;
        this.executor = Executors.newFixedThreadPool(5);
    }

    @RequestMapping(value = "/system/equivalence/update", method = RequestMethod.POST)
    public void runUpdate(HttpServletResponse response, @RequestParam(value = "uris", required = true) String uris) throws IOException {

        ResolvedContent resolved = contentResolver.findByCanonicalUris(commaSplitter.split(uris));

        if (resolved.isEmpty()) {
            response.setStatus(NOT_FOUND.code());
            response.setContentLength(0);
            return;
        }

        for (Content content : Iterables.filter(resolved.getAllResolvedResults(), Content.class)) {
            executor.submit(updateFor(content));
        }
        response.setStatus(OK.code());

    }

    private Runnable updateFor(final Content content) {
        return new Runnable() {
            @Override
            public void run() {
                try{
                    if (content instanceof Film) {
                        filmUpdater.updateEquivalences((Film) content, Optional.<List<Film>>absent());
                    } else {
                        contentUpdater.updateEquivalences(content, Optional.<List<Content>>absent());
                    }
                }catch (Exception e) {
                    log.record(AdapterLogEntry.errorEntry().withSource(getClass()).withCause(e).withDescription("Equivalence Update failed for "+content.getCanonicalUri()));
                }
            }
        };
    }

}
