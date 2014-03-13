package org.atlasapi.remotesite.rovi;

import java.io.File;


/**
 * This class is a supplier for full and delta files to ingest
 */
public class FileSupplier {
    
    // Using constants for now, there will then be a logic based on the current time
    private static final String PROGRAMS_FILE = "/data/rovi/Program.txt";
    private static final String PROGRAM_DESCRIPTION = "/data/rovi/Program_Description.txt";
    private static final String EPISODE_SEQUENCE = "/data/rovi/Episode_Sequence.txt";
    private static final String SEASON_HISTORY = "/data/rovi/Season_History.txt";
    private static final String SCHEDULE_FILE = "/data/rovi/Schedule.txt";
    
    private static final String DELTA_PROGRAMS_FILE = "/data/rovi/delta/Program.txt";
    private static final String DELTA_PROGRAM_DESCRIPTION = "/data/rovi/delta/Program_Description.txt";
    private static final String DELTA_EPISODE_SEQUENCE = "/data/rovi/delta/Episode_Sequence.txt";
    private static final String DELTA_SEASON_HISTORY = "/data/rovi/delta/Season_History.txt";
    private static final String DELTA_SCHEDULE_FILE = "/data/rovi/delta/Schedule.txt";  
    
    private static final String SCHEDULE_FOLDER = "/data/rovi/schedule";
    
    // Return files for full ingest
    public static File fullProgramFile() {
        return new File(PROGRAMS_FILE);
    }
    public static File fullProgramDescriptionsFile() {
        return new File(PROGRAM_DESCRIPTION);
    }
    public static File fullEpisodeSequenceFile() {
        return new File(EPISODE_SEQUENCE);
    }
    public static File fullSeasonHistoryFile() {
        return new File(SEASON_HISTORY);
    }
    public static File fullScheduleFile() {
        return new File(SCHEDULE_FILE);
    }
    
    // Return files for delta ingest
    public static File deltaProgramFile() {
        return new File(DELTA_PROGRAMS_FILE);
    }
    
    public static File deltaProgramDescriptionsFile() {
        return new File(DELTA_PROGRAM_DESCRIPTION);
    }
    
    public static File deltaEpisodeSequenceFile() {
        return new File(DELTA_EPISODE_SEQUENCE);
    }
    
    public static File deltaSeasonHistoryFile() {
        return new File(DELTA_SEASON_HISTORY);
    }
    
    public static File deltaScheduleFile() {
        return new File(DELTA_SCHEDULE_FILE);
    }
    
    public static File scheduleFolder() {
        return new File(SCHEDULE_FOLDER);
    }

}
