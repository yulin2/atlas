package org.atlasapi.equiv.results.description;

public interface ResultDescription {

    ResultDescription appendText(String format, Object... args);
    
    ResultDescription startStage(String stageName);
    
    ResultDescription finishStage();
        
}
