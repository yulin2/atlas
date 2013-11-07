package org.atlasapi.remotesite.wikipedia;

import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains helper methods for dealing with the SWEBLE parser's AST output.
 */
public class SwebleHelper {

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
     * Quick and dirty way to flatten a NodeList containing a bunch of Text nodes into a single string.
     * 
     * Watch out because any other nodes will be ignored...
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
