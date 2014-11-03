package org.atlasapi.query.v2;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atlasapi.application.query.ApplicationConfigurationFetcher;
import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.youview.transactions.Transaction;
import org.atlasapi.feeds.youview.transactions.TransactionQuery;
import org.atlasapi.feeds.youview.transactions.persistence.TransactionStore;
import org.atlasapi.media.entity.Publisher;
import org.atlasapi.output.AtlasErrorSummary;
import org.atlasapi.output.AtlasModelWriter;
import org.atlasapi.persistence.logging.AdapterLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.metabroadcast.common.http.HttpStatusCode;
import com.metabroadcast.common.query.Selection;
import com.metabroadcast.common.query.Selection.SelectionBuilder;
import com.youview.refdata.schemas.youviewstatusreport._2010_12_07.TransactionStateType;

@Controller
public class TransactionController extends BaseController<Iterable<Transaction>> {
    
    private static final SelectionBuilder SELECTION_BUILDER = Selection.builder().withMaxLimit(100).withDefaultLimit(10);
    private static final AtlasErrorSummary NOT_FOUND = new AtlasErrorSummary(new NullPointerException())
            .withMessage("No Transaction exists with the provided ID")
            .withErrorCode("Transaction not found")
            .withStatusCode(HttpStatusCode.NOT_FOUND);
    private static final AtlasErrorSummary FORBIDDEN = new AtlasErrorSummary(new NullPointerException())
            .withMessage("You require an API key to view this data")
            .withErrorCode("Api Key required")
            .withStatusCode(HttpStatusCode.FORBIDDEN);
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final TransactionStore transactionStore;
    
    public TransactionController(ApplicationConfigurationFetcher configFetcher, AdapterLog log,
            AtlasModelWriter<Iterable<Transaction>> outputter, TransactionStore transactionStore) {
        super(configFetcher, log, outputter);
        this.transactionStore = checkNotNull(transactionStore);
    }

    @RequestMapping(value="/3.0/feeds/youview/{publisher}/transactions.json", method = RequestMethod.GET)
    public void transactions(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("publisher") String publisherStr,
            @RequestParam(value = "uri", required = false) String contentUri,
            @RequestParam(value = "transaction_id", required = false) String transactionId,
            @RequestParam(value = "status", required = false) String status) throws IOException {
        
        try {
            Selection selection = SELECTION_BUILDER.build(request);
            ApplicationConfiguration appConfig = appConfig(request);
            Publisher publisher = Publisher.valueOf(publisherStr.trim().toUpperCase());
            
            if (!appConfig.isEnabled(publisher)) {
                errorViewFor(request, response, FORBIDDEN);
            }

            TransactionQuery transactionQuery = queryFrom(publisher, selection, contentUri, transactionId, status);
            Iterable<Transaction> allTransactions = transactionStore.allTransactions(transactionQuery);
            
            modelAndViewFor(request, response, allTransactions, appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
    
    private TransactionQuery queryFrom(Publisher publisher, Selection selection, String contentUri, String transactionId, String status) {
        TransactionQuery.Builder query = TransactionQuery.builder(selection, publisher)
                .withContentUri(contentUri)
                .withTransactionId(transactionId);
        
        if (status != null) {
            TransactionStateType statusType = TransactionStateType.valueOf(status.trim().toUpperCase());
            query.withTransactionStatus(statusType);
        }
        return query.build();
    }

    @RequestMapping(value="/3.0/feeds/youview/{publisher}/transactions/{id}.json", method = RequestMethod.GET)
    public void transaction(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("publisher") String publisherStr,
            @PathVariable("id") String id) throws IOException {
        try {
            
            String rawPublisherStr = publisherStr.trim().toUpperCase();
            log.debug("transactions accessed with publisher {}", rawPublisherStr);
            Publisher publisher = Publisher.valueOf(rawPublisherStr);
            ApplicationConfiguration appConfig = appConfig(request);
            if (!appConfig.isEnabled(publisher)) {
                errorViewFor(request, response, FORBIDDEN);
                return;
            }

            Optional<Transaction> resolved = transactionStore.transactionFor(id, publisher);
            if (!resolved.isPresent()) {
                errorViewFor(request, response, NOT_FOUND);
                return;
            }
            modelAndViewFor(request, response, ImmutableList.of(resolved.get()), appConfig);
        } catch (Exception e) {
            errorViewFor(request, response, AtlasErrorSummary.forException(e));
        }
    }
}
