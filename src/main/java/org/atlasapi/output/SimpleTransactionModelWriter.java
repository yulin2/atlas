package org.atlasapi.output;

import java.util.Set;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.youview.transactions.Transaction;
import org.atlasapi.feeds.youview.transactions.simple.TransactionQueryResult;
import org.atlasapi.output.simple.ModelSimplifier;

public class SimpleTransactionModelWriter extends TransformingModelWriter<Iterable<Transaction>, TransactionQueryResult> {

    private final ModelSimplifier<Transaction, org.atlasapi.feeds.youview.transactions.simple.Transaction> transactionSimplifier;

    public SimpleTransactionModelWriter(AtlasModelWriter<TransactionQueryResult> delegate, ModelSimplifier<Transaction, org.atlasapi.feeds.youview.transactions.simple.Transaction> transactionSimplifier) {
        super(delegate);
        this.transactionSimplifier = transactionSimplifier;
    }
    
    @Override
    protected TransactionQueryResult transform(Iterable<Transaction> fullTransactions, Set<Annotation> annotations, ApplicationConfiguration config) {
        TransactionQueryResult result = new TransactionQueryResult();
        for (Transaction fullTransaction : fullTransactions) {
            result.add(transactionSimplifier.simplify(fullTransaction, annotations, config));
        }
        return result;
    }

}
