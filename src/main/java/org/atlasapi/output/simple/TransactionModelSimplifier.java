package org.atlasapi.output.simple;

import java.util.Set;

import org.atlasapi.application.v3.ApplicationConfiguration;
import org.atlasapi.feeds.youview.transactions.Transaction;
import org.atlasapi.feeds.youview.transactions.TransactionStatus;
import org.atlasapi.feeds.youview.transactions.simple.StatusDetail;
import org.atlasapi.output.Annotation;


public class TransactionModelSimplifier implements ModelSimplifier<Transaction, org.atlasapi.feeds.youview.transactions.simple.Transaction> {
    

    public TransactionModelSimplifier() {
    }

    @Override
    public org.atlasapi.feeds.youview.transactions.simple.Transaction simplify(Transaction model,
            Set<Annotation> annotations, ApplicationConfiguration config) {
        org.atlasapi.feeds.youview.transactions.simple.Transaction transaction = new org.atlasapi.feeds.youview.transactions.simple.Transaction();
        
        transaction.setId(model.id());
        transaction.setPublisher(model.publisher());
        transaction.setUploadTime(model.uploadTime().toDate());
        transaction.setContent(model.content());
        transaction.setStatus(model.status().status());
        
        if (annotations.contains(Annotation.STATUS_DETAIL)) {
            transaction.setStatusDetail(simplifyStatus(model.status()));
        }
        
        return transaction;
    }

    private StatusDetail simplifyStatus(TransactionStatus status) {
        StatusDetail statusDetail = new StatusDetail();
        
        statusDetail.setStatus(status.status());
        statusDetail.setMessage(status.message());
        if (status.fragmentReports().isPresent()) {
            statusDetail.setFragmentReports(status.fragmentReports().get());
        }
        
        return statusDetail;
    }
}
