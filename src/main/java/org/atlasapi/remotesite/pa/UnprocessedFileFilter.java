package org.atlasapi.remotesite.pa;

import java.io.File;

import org.atlasapi.feeds.upload.FileUploadResult;
import org.atlasapi.feeds.upload.FileUploadResult.FileUploadResultType;
import org.atlasapi.feeds.upload.persistence.FileUploadResultStore;

import com.google.common.base.Predicate;
import com.metabroadcast.common.base.Maybe;

public final class UnprocessedFileFilter implements Predicate<File> {

    private final Long since;
    private final FileUploadResultStore resultStore;
    private final String service;

    public UnprocessedFileFilter(FileUploadResultStore resultStore, String service, Long since) {
        this.service = service;
        this.since = since;
        this.resultStore = resultStore;
    }

    @Override
    public boolean apply(File input) {
        Maybe<FileUploadResult> result = resultStore.latestResultFor(service, input.getName());
        return input.lastModified() > since &&
                (result.isNothing() || !FileUploadResultType.SUCCESS.equals(result.requireValue().type()));
    }
}