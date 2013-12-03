package org.atlasapi.remotesite.wikipedia.film;

import java.io.IOException;
import java.util.Iterator;

import org.atlasapi.remotesite.wikipedia.SwebleHelper;
import org.atlasapi.remotesite.wikipedia.SwebleHelper.ListItemResult;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sweble.wikitext.lazy.preprocessor.LazyPreprocessedPage;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;

import xtc.parser.ParseException;

import com.google.common.collect.ImmutableList;

import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;

/**
 * This utility class extracts information from the Film infobox in a Wikipedia article.
 */
public final class FilmInfoboxScraper {
    private final static Logger log = LoggerFactory.getLogger(FilmInfoboxScraper.class);
    
    public static class Result {
        public ImmutableList<ListItemResult> name;
        public ImmutableList<ListItemResult> directors;
        public ImmutableList<ListItemResult> producers;
        public ImmutableList<ListItemResult> writers;
        public ImmutableList<ListItemResult> screenplayWriters;
        public ImmutableList<ListItemResult> storyWriters;
        public ImmutableList<ListItemResult> narrators;
        public ImmutableList<ListItemResult> starring;
        public ImmutableList<ListItemResult> composers;
        public ImmutableList<ListItemResult> cinematographers;
        public ImmutableList<ListItemResult> editors;
        public ImmutableList<ListItemResult> productionStudios;
        public ImmutableList<ListItemResult> distributors;
        public ImmutableList<LocalDate> releaseDates;  // TODO oh dear, this is mildly complicated
        public Integer runtimeInMins;
        public ImmutableList<ListItemResult> countries;
        public ImmutableList<ListItemResult> language;
        public String imdbID;
    }

    /**
     * Returns the information given to the Film infobox template in the given Mediawiki page source.
     */
    public static Result getInfoboxAttrs(String articleText) throws IOException, ParseException {
        LazyPreprocessedPage ast = SwebleHelper.preprocess(articleText, false);

        InfoboxVisitor v = new InfoboxVisitor();
        Iterator<AstNode> topLevelEls = ast.getContent().iterator();
        while(topLevelEls.hasNext()) {
            v.consumeInfobox(topLevelEls.next());
        }

        return v.attrs;
    }

    /**
     * This thing looks at a preprocessor-generated AST of a Mediawiki page, finds the Film infobox, and gathers key-value pairs from it.
     */
    private static final class InfoboxVisitor {
        final Result attrs = new Result();

        void consumeInfobox(AstNode n) throws IOException, ParseException {
            if (!(n instanceof Template)) {
                return;
            }
            Template t = (Template) n;

            String name = SwebleHelper.flattenTextNodeList(t.getName());
            if ("Infobox film".equalsIgnoreCase(name)) {
                Iterator<AstNode> children = t.getArgs().iterator();
                while(children.hasNext()) {
                    consumeAttribute(children.next());
                }
            } else if ("IMDb title".equalsIgnoreCase(name)) {
                NodeList args = t.getArgs();
                try {
                    attrs.imdbID = SwebleHelper.flattenTextNodeList(((TemplateArgument) args.get(0)).getValue());
                } catch (Exception e) {
                    log.warn("Failed to extract IMDB id from \""+ SwebleHelper.unparse(t) +"\"");
                }
            }
        }

        void consumeAttribute(AstNode n) throws IOException, ParseException {
            if (!(n instanceof TemplateArgument)) {
                return;
            }
            TemplateArgument a = (TemplateArgument) n;

            final String key = SwebleHelper.flattenTextNodeList(a.getName());
            String unparsed = SwebleHelper.unparse(a.getValue()).trim();
            AstNode reparsedValue = null;
            try {
                reparsedValue = SwebleHelper.parse(unparsed);
            } catch (Exception e) {
                log.warn("Failed to reparse: " + unparsed, e);
            }
            
            if ("name".equalsIgnoreCase(key)) {
                attrs.name = SwebleHelper.extractList(reparsedValue);
            } else if ("director".equalsIgnoreCase(key)) {
                attrs.directors = SwebleHelper.extractList(reparsedValue);
            } else if ("producer".equalsIgnoreCase(key)) {
                attrs.producers = SwebleHelper.extractList(reparsedValue);
            } else if ("writer".equalsIgnoreCase(key)) {
                attrs.writers = SwebleHelper.extractList(reparsedValue);
            } else if ("screenplay".equalsIgnoreCase(key)) {
                attrs.screenplayWriters = SwebleHelper.extractList(reparsedValue);
            } else if ("story".equalsIgnoreCase(key)) {
                attrs.storyWriters = SwebleHelper.extractList(reparsedValue);
            } else if ("narrator".equalsIgnoreCase(key)) {
                attrs.narrators = SwebleHelper.extractList(reparsedValue);
            } else if ("starring".equalsIgnoreCase(key)) {
                attrs.starring = SwebleHelper.extractList(reparsedValue);
            } else if ("music".equalsIgnoreCase(key)) {
                attrs.composers = SwebleHelper.extractList(reparsedValue);
            } else if ("cinematography".equalsIgnoreCase(key)) {
                attrs.cinematographers = SwebleHelper.extractList(reparsedValue);
            } else if ("editing".equalsIgnoreCase(key)) {
                attrs.editors = SwebleHelper.extractList(reparsedValue);
            } else if ("studio".equalsIgnoreCase(key)) {
                attrs.productionStudios = SwebleHelper.extractList(reparsedValue);
            } else if ("distributor".equalsIgnoreCase(key)) {
                attrs.distributors = SwebleHelper.extractList(reparsedValue);
            } else if ("released".equalsIgnoreCase(key)) {
                // TODO extract release dates
            } else if ("runtime".equalsIgnoreCase(key)) {
                String runtimeText = SwebleHelper.flattenTextNodeList(a.getValue());;
                try {
                    attrs.runtimeInMins = Integer.parseInt(runtimeText.split(" ", 2)[0]);
                    if (attrs.runtimeInMins < 10 || attrs.runtimeInMins > 300) {
                        log.warn("Suspicious running time: " + attrs.runtimeInMins);
                    }
                } catch (NumberFormatException e) {
                    if (! runtimeText.isEmpty()) { log.warn("Failed to extract running time from weird string \"" + runtimeText + "\""); }
                }
            } else if ("country".equalsIgnoreCase(key)) {
                attrs.countries = SwebleHelper.extractList(reparsedValue);
            } else if ("language".equalsIgnoreCase(key)) {
                attrs.language = SwebleHelper.extractList(reparsedValue);
            }
        }
    }

}
