package org.atlasapi.remotesite.redux;

public class UpdateProgress {

    private final int processed;
    private final int failures;
    
    public static final UpdateProgress START = new UpdateProgress(0,0);

    public UpdateProgress(int processed, int failures) {
        this.processed = processed;
        this.failures = failures;
    }
    
    public UpdateProgress add(UpdateProgress update) {
        if(update == null || START.equals(update)) {
            return this;
        }
        return new UpdateProgress(processed + update.processed, failures + update.failures);
    }

    public int getFailures() {
        return failures;
    }

    public int getProcessed() {
        return processed;
    }
    
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that instanceof UpdateProgress) {
            UpdateProgress other = (UpdateProgress) that;
            return processed == other.processed && failures == other.failures;
        }
        return false;
    }
    
    public int hashCode() {
        //Same as Objects.hashCode(processed, failures)
        return 961 + ((processed << 5) - processed) + failures;
    }
    
    @Override
    public String toString() {
        return toString("Progress: %d processed, %s failures");
    }
    
    public String toString(String pattern) {
        return String.format(pattern, processed, failures);
    }
    
    
}
