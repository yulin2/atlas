package org.atlasapi.remotesite.rovi.processing.restartable;

import static com.google.api.client.repackaged.com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;

import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class IngestSequentialFileProcessingStep extends AbstractIngestProcessingStep {

    private final LineProcessor<RoviDataProcessingResult> processor;
    private final File file;
    private final Charset charset;
    private final IngestStatusStore persistor;

    private IngestSequentialFileProcessingStep(IngestStep step,
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

    public static Builder builder(Charset charset, IngestStatusStore persistor) {
        return new Builder(charset, persistor);
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

        private Builder(Charset charset, IngestStatusStore persistor) {
            this.charset = charset;
            this.persistor = persistor;
        }

        public Builder withStep(IngestStep step) {
            this.step = step;
            return this;
        }

        public Builder withProcessor(LineProcessor<RoviDataProcessingResult> processor) {
            this.processor = processor;
            return this;
        }

        public Builder withFile(File file) {
            this.file = file;
            return this;
        }

        public IngestSequentialFileProcessingStep build() {
            return new IngestSequentialFileProcessingStep(step, processor, file, charset, persistor);
        }
    }
}
