package org.atlasapi.remotesite.rovi.processing.restartable;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.atlasapi.remotesite.rovi.processing.IngestStepFailedException;
import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class IngestFileProcessingStep extends AbstractIngestProcessingStep {

    private final LineProcessor<RoviDataProcessingResult> processor;
    private final File file;
    private final Charset charset;
    private final IngestStatusStore persistor;

    private IngestFileProcessingStep(IngestStep step,
            LineProcessor<RoviDataProcessingResult> delegate, File file, Charset charset,
            IngestStatusStore persistor) {
        super(step);
        this.persistor = checkNotNull(persistor);
        this.processor = checkNotNull(delegate);
        this.file = checkNotNull(file);
        this.charset = checkNotNull(charset);
    }

    @Override
    public RoviDataProcessingResult executeWithStatus(IngestStatus ingestStatus) {
        return execute(new RestartableLineProcessor(processor, ingestStatus, persistor));
    }

    public static Builder forStep(IngestStep step) {
        return new Builder(step);
    }

    private RoviDataProcessingResult execute(LineProcessor<RoviDataProcessingResult> processor) {
        try {
            return Files.readLines(file, charset, processor);
        } catch (IOException e) {
            throw new IngestStepFailedException("Failed to complete ingest step: " + getStep(), e);
        }
    }

    public static class Builder {
        private IngestStep step;
        private LineProcessor<RoviDataProcessingResult> processor;
        private File file;
        private Charset charset;
        private IngestStatusStore persistor;

        private Builder(IngestStep step) {
            this.step = step;
        }

        public Builder withProcessor(LineProcessor<RoviDataProcessingResult> processor) {
            this.processor = processor;
            return this;
        }

        public Builder withFile(File file) {
            this.file = file;
            return this;
        }

        public Builder withCharset(Charset charset) {
            this.charset = charset;
            return this;
        }

        public Builder withStatusPersistor(IngestStatusStore persistor) {
            this.persistor = persistor;
            return this;
        }

        public IngestFileProcessingStep build() {
            return new IngestFileProcessingStep(step, processor, file, charset, persistor);
        }
    }
}
