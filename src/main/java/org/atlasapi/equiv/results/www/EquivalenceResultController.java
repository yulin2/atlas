package org.atlasapi.equiv.results.www;

import static com.google.common.base.Charsets.UTF_8;
import static com.metabroadcast.common.http.HttpStatusCode.NOT_FOUND;
import static com.metabroadcast.common.http.HttpStatusCode.OK;
import static com.metabroadcast.common.media.MimeType.TEXT_HTML;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.atlasapi.equiv.results.persistence.EquivalenceIdentifier;
import org.atlasapi.equiv.results.persistence.EquivalenceResultStore;
import org.atlasapi.equiv.results.persistence.RestoredEquivalenceResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.time.DateTimeZones;

@Controller
public class EquivalenceResultController {

    private final EquivalenceResultStore store;

    public EquivalenceResultController(EquivalenceResultStore store) {
        this.store = store;
    }
    
    @RequestMapping(value = "/system/equivalence/results", method = RequestMethod.GET)
    public void runUpdate(HttpServletResponse response, @RequestParam(value = "uri", required = true) String uri) throws IOException {
        
        RestoredEquivalenceResult equivalenceResult = store.forId(uri);
        
        if(equivalenceResult == null) {
            response.sendError(NOT_FOUND.code(), "No result for URI");
            return;
        }
        
        response.setStatus(OK.code());
        response.setContentType(TEXT_HTML.toString());
        response.setCharacterEncoding(UTF_8.toString());
        
        PrintWriter out = response.getWriter();
        out.write("<html>");
        out.write(headerFor(equivalenceResult));
        
        out.write("<h1>");
        out.write(equivalenceResult.title());
        out.write("</h1><h2>");
        out.write(equivalenceResult.id());
        out.write("</h2><h3>");
        out.write(equivalenceResult.resultTime().toDateTime(DateTimeZones.LONDON).toString("HH:mm:ss dd/MM/YY"));
        out.write("</h3>");
        
        out.write(resultTable(equivalenceResult));
        
    }

    private String resultTable(RestoredEquivalenceResult result) {
        StringBuilder table = new StringBuilder();
        table.append("<table><thead><tr><th>Suggested</th><th>Combined</th>");
        List<String> sourceKeys = ImmutableList.copyOf(result.sourceResults().columnKeySet());
        for (String sourceName : sourceKeys) {
            table.append("<th>").append(sourceName).append("</th>");
        }
        table.append("</tr></thead><tbody>");
        for (Entry<EquivalenceIdentifier, Double> combinedEntry : result.combinedResults().entrySet()) {
            EquivalenceIdentifier id = combinedEntry.getKey();
            table.append("<tr>").append(title(id)).append(id.publisher()).append("/").append(id.title()).append(id.strong() ? "</th>" : "</td>");
            table.append("<td>").append(combinedEntry.getValue()).append("</td>");

            Map<String, Double> sourceResultRow = result.sourceResults().row(id.id());
            for (String source : sourceKeys) {
                Double score = sourceResultRow.get(source);
                table.append("<td>").append(score != null ? score : "N/A").append("</td>");
            }
            table.append("</tr>");
        }
        return null;
    }

    private String title(EquivalenceIdentifier id) {
        return String.format("<t%s title=\"%s\">", id.strong() ? "h" : "d", id.id());
    }

    private String headerFor(RestoredEquivalenceResult result) {
        StringBuilder headerBuilder = new StringBuilder();
        headerBuilder.append("<head><title>").append(result.title()).append(" - ").append(result.id()).append("</title>");
        headerBuilder.append("<style></style>");
        headerBuilder.append("</head>");
        return null;
    }
    
}
