package org.atlasapi.remotesite.wikipedia.film;

import com.google.common.base.Function;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import java.io.IOException;
import java.util.Iterator;
import org.atlasapi.remotesite.wikipedia.SwebleHelper;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.sweble.wikitext.lazy.parser.LazyParsedPage;
import org.sweble.wikitext.lazy.preprocessor.LazyPreprocessedPage;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;
import xtc.parser.ParseException;

/**
 * This utility class extracts information from the Film infobox in a Wikipedia article.
 */
public final class FilmInfoboxScraper {

    /**
     * Returns the key/value arguments given to the Film infobox template in the given Mediawiki page source. 
     */
    public static ListMultimap<String, String> getInfoboxAttrs(String articleText) throws IOException, ParseException {
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
        ListMultimap<String,String> attrs = LinkedListMultimap.<String, String>create();

        void consumeInfobox(AstNode n) throws IOException, ParseException {
            if(!(n instanceof Template)) {
                return;
            }
            Template t = (Template) n;

            String name = SwebleHelper.flattenTextNodeList(t.getName());
            if(! "Infobox film".equalsIgnoreCase(name)) {
                return;
            }

            Iterator<AstNode> children = t.getArgs().iterator();
            while(children.hasNext()) {
                consumeAttribute(children.next());
            }
        }

        void consumeAttribute(AstNode n) throws IOException, ParseException {
            if(!(n instanceof TemplateArgument)) {
                return;
            }
            TemplateArgument a = (TemplateArgument) n;

            final String key = SwebleHelper.flattenTextNodeList(a.getName());
            AstNode value = SwebleHelper.parse(SwebleHelper.flattenTextNodeList(a.getValue()));
            new InfoboxIndividualValueExtractor(new Function<String, Void>() {
                @Override
                public Void apply(String f) {
                    if(f.startsWith("(")) {  // This is probably an extraneous annotation, we skip it!
                        return null;
                    }
                    attrs.put(key, f);
                    return null;
                }
            }).go(value);
        }
    }

    /**
     * This looks at a parser-generated AST of the content given to an infobox argument (such as a list of names/links), disregarding formatting and calling back with the individual text chunks that should be taken as values.
     */
    public static final class InfoboxIndividualValueExtractor extends AstVisitor {

        Function<String, Void> cb;

        public InfoboxIndividualValueExtractor(Function<String, Void> cb) {
            this.cb = cb;
        }

        public void visit(LazyParsedPage value) {
            iterate(value.getContent());
        }
        public void visit(InternalLink link) {
            NodeList titleContent = link.getTitle().getContent();
            if(titleContent.isEmpty()) {  // TODO find a more appropriate distinction (this is for the case where two Coen Brothers share an article...)
                cb.apply(link.getTarget());
            } else {
                iterate(titleContent);
            }
        }
        public void visit(Text t) {
            cb.apply(t.getContent());
        }

        @Override
        protected Object visitNotFound(AstNode node) { return null; }

    }
}
