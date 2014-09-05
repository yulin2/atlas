package org.atlasapi.remotesite.util;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.ProviderCredentials;

import com.google.common.base.Supplier;

public class RestS3ServiceSupplier implements Supplier<S3Service> {

    private final ProviderCredentials credentials;

    public RestS3ServiceSupplier(ProviderCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public S3Service get() {
        try {
            return new RestS3Service(credentials);
        } catch (S3ServiceException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
