package org.atlasapi.remotesite.wikipedia.television;

import org.atlasapi.remotesite.wikipedia.Callback;
import org.atlasapi.remotesite.wikipedia.SwebleHelper;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.lazy.parser.LazyParsedPage;
import org.sweble.wikitext.lazy.preprocessor.LazyPreprocessedPage;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;

import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;

public final class BrandInfoboxScraper extends AstVisitor {
    private static final Logger log = LoggerFactory.getLogger(BrandInfoboxScraper.class);
    public static class Result {
        public String title;
        public String creator;
        public String episodeListLinkTarget;
        public LocalDate firstAired;
        public LocalDate lastAired;
        public String imdbID;

        @Override
        public String toString() {
            return "BrandInfoboxScraper.Result{" + "title=" + title + ", creator=" + creator + ", episodeListLinkTarget=" + episodeListLinkTarget + ", firstAired=" + firstAired + ", lastAired=" + lastAired + '}';
        }
    }
    
    private final Callback<Result> callback;
    private final Result result;
    public BrandInfoboxScraper(Callback<Result> callback) {
        this.callback = callback;
        this.result = new Result();
    }
    
    @Override
    protected Object after(AstNode node, Object irrelevantResult) {
        if (this.callback != null) { callback.have(this.result); }
        return this.result;
    }

    /** Courtesy exception in case of the wrong AST */
    public void visit(LazyParsedPage p) {
        throw new IllegalArgumentException("The TvBrandInfoboxScraper only knows how to deal with templates, so it wants a preprocessed page, not a parsed one.");
    }
    
    public void visit(LazyPreprocessedPage p) {
        iterate(p.getContent());
    }

    public void visit(Template t) {
        String name = SwebleHelper.flattenTextNodeList(t.getName());
        if ("Infobox television".equalsIgnoreCase(name)) {
            iterate(t.getArgs());
        } else if ("IMDb title".equalsIgnoreCase(name)) {
            NodeList args = t.getArgs();
            try {
                result.imdbID = SwebleHelper.flattenTextNodeList(((TemplateArgument) args.get(0)).getValue());
            } catch (Exception e) {
                log.warn("Failed to extract IMDB id from \""+ SwebleHelper.unparse(t) +"\"");
            }
        } else {
            log.debug("Ignoring template " + name);
        }
    }

    public void visit(TemplateArgument a) {
        String name = SwebleHelper.flattenTextNodeList(a.getName());
        // TODO: alt titles? and other stuff http://en.wikipedia.org/wiki/Template:Infobox_television
        if ("show_name".equalsIgnoreCase(name)) {
            result.title = SwebleHelper.flattenTextNodeList(a.getValue());
        } else if ("creator".equalsIgnoreCase(name)) {
            result.creator = SwebleHelper.flattenTextNodeList(a.getValue());
        } else if ("list_episodes".equalsIgnoreCase(name)) {
            result.episodeListLinkTarget = SwebleHelper.flattenTextNodeList(a.getValue());
        } else if ("first_aired".equalsIgnoreCase(name) && result.firstAired == null) {
            result.firstAired = SwebleHelper.extractDate(a.getValue());
        } else if ("last_aired".equalsIgnoreCase(name) && result.lastAired == null) {
            result.lastAired = SwebleHelper.extractDate(a.getValue());
        }
    }

    @Override
    protected Object visitNotFound(AstNode node) {
        log.debug("Skipping node " + node.getNodeName());
        return null;
    }
    
}
