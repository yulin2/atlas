package org.atlasapi.remotesite.wikipedia;

import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.sweble.wikitext.lazy.LazyParser;
import org.sweble.wikitext.lazy.LazyPreprocessor;
import org.sweble.wikitext.lazy.ParserConfigInterface;
import org.sweble.wikitext.lazy.parser.LazyParsedPage;
import org.sweble.wikitext.lazy.preprocessor.LazyPreprocessedPage;
import org.sweble.wikitext.lazy.utils.SimpleParserConfig;
import xtc.parser.ParseException;

/**
 * Contains helper methods for dealing with the SWEBLE wikitext parser and its AST output.
 */
public class SwebleHelper {
    private static final ParserConfigInterface cfg = new SimpleParserConfig();
    private static final LazyParser parser = new LazyParser(cfg);
    private static final LazyPreprocessor preprocessor = new LazyPreprocessor(cfg);

    /**
     * Performs the first half of the Mediawiki parsing process -- the resulting AST includes templates and their arguments (the usual intention being to expand and include these before the remaining parse) but no awareness of formatting or any other textual abnormalities.
     * @param includeOnly If true, the page is processed as if it's being included – everything outside of the 'includeonly' section is ignored. This is handy for instance in the case of TV season pages, which when treated as a template in this way, include only the episodes table.
     * @see #parse(java.lang.String)
     * @see <a href="http://sweble.org/downloads/diwp-preprint.pdf">SWEBLE spec pdf</a>
     */
    public static LazyPreprocessedPage preprocess(String mediaWikiSource, boolean includeOnly) throws IOException, ParseException {
        return (LazyPreprocessedPage) preprocessor.parseArticle(mediaWikiSource, "", includeOnly);
    }
    
    /**
     * Performs the second half of the Mediawiki parsing process -- the resulting AST includes links, formatting, sections etc., but any template inclusions remain in their useless plain text form.
     * @see #preprocess(java.lang.String, boolean)
     * @see <a href="http://sweble.org/downloads/diwp-preprint.pdf">SWEBLE spec pdf</a>
     */
    public static LazyParsedPage parse(String mediaWikiSource) throws IOException, ParseException {
        return (LazyParsedPage) parser.parseArticle(mediaWikiSource, "");
    }
    
    /**
     * Prints a representation of the given AST to System.out
     * @param indent How many spaces to indent by at the start of each line.
     */
    public static void showAST(AstNode ast, int indent) {
        for (int i=0; i<indent; i++) {
            System.out.print(ast.getNodeName().equals("Template") ? "* " : "  ");
        }
        System.out.println("* " + ast.getNodeName() + " [" + ast.getNodeTypeName() + "]");
        for (Map.Entry attr : ast.getAttributes().entrySet()) {
            for (int i=0; i<indent; i++) {
                System.out.print("  ");
            }
            System.out.println("   " + attr.getKey() + " : " + attr.getValue());
        }
        if (ast instanceof Text) {
            for (int i=0; i<indent; i++) {
                System.out.print("  ");
            }
            System.out.println("  => " + ((Text) ast).getContent());
        }
        Iterator<AstNode> children = ast.iterator();
        while (children.hasNext()) {
            showAST(children.next(), indent + 1);
        }
    }

    /**
     * Quick and dirty way to flatten a NodeList containing a bunch of Text nodes into a single string -- for instance, to process them with the other half of the parsing process (or some other form of interpretation) and get information out.
     * 
     * Watch out though, because any other, non-Text nodes will be ignored...
     */
    public static String flattenTextNodeList(NodeList l) {
        StringBuilder b = new StringBuilder(600);
        Iterator<AstNode> children = l.iterator();
        AstNode n;
        while (children.hasNext()) {
            n = children.next();
            if (n instanceof Text) {
                b.append(((Text) n).getContent());
            }
        }
        return b.toString().trim();
    }
    
}
