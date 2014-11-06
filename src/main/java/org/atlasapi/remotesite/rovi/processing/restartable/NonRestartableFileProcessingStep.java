package org.atlasapi.remotesite.rovi.processing.restartable;


import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import org.atlasapi.remotesite.rovi.processing.FileProcessor;
import org.atlasapi.remotesite.rovi.processing.RoviDataProcessingResult;

import com.google.api.client.repackaged.com.google.common.base.Throwables;

public class NonRestartableFileProcessingStep extends AbstractIngestProcessingStep {

    private final FileProcessor processor;
    private final File file;

    private NonRestartableFileProcessingStep(IngestStep step, FileProcessor processor, File file) {
        super(step);
        this.processor = checkNotNull(processor);
        this.file = checkNotNull(file);
    }

    @Override
    public RoviDataProcessingResult executeWithStatus(IngestStatus ingestStatus) {
        try {
            return processor.process(file);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IngestStep step;
        private FileProcessor processor;
        private File file;

        public Builder withStep(IngestStep step) {
            this.step = step;
            return this;
        }

        public Builder withFileProcessor(FileProcessor processor) {
            this.processor = processor;
            return this;
        }

        public Builder withFile(File file) {
            this.file = file;
            return this;
        }

        public NonRestartableFileProcessingStep build() {
            return new NonRestartableFileProcessingStep(step, processor, file);
        }
    }
}
