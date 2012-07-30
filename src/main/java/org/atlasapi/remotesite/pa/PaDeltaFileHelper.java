package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaDeltaFileHelper {

    private static final Logger log = LoggerFactory.getLogger(PaDeltaFileHelper.class);
    
    private static final Pattern FILEVERSION_PATTERN = Pattern.compile("^.*/(\\d{12})_\\d{8}_tvdata.xml$");
    private static final Pattern FILEDATE_PATTERN = Pattern.compile("^.*(\\d{8})_tvdata.xml$");
    
    /**
     * Get the version number for a PA delta file. Version numbers
     * are strictly increasing over time, but are not continuous
     * 
     * @param filename
     * @return version number
     */
    public long versionNumber(String filename) {
        Matcher versionMatcher = FILEVERSION_PATTERN.matcher(filename);
        if(versionMatcher.matches()) {
            return Long.valueOf(versionMatcher.group(1));
        }
        return 1;
    }
    
    /**
     * Order files to be processed 
     * 
     * Files for different days can be processed in parallel. However, files for a given
     * day must be processed sequentially, the full file followed by its deltas, in order. 
     * Therefore we produce a set of lists of files, one list per schedule day, and we can 
     * process the head of each list in parallel.
     * 
     * @param files
     * @return
     */
    public Set<Queue<File>> groupAndOrderFilesByDay(Iterable<File> files) {
        
        java.util.Map<String, LinkedList<File>> filesByDay = new HashMap<String, LinkedList<File> >();
        
        for(File file : files) {
             Matcher matcher = FILEDATE_PATTERN.matcher(file.toURI().toString());
                
            if (matcher.matches()) {
                final String fileDate = matcher.group(1);
                
                if(!filesByDay.containsKey(fileDate)) {
                    filesByDay.put(fileDate, new LinkedList<File>());
                }
                filesByDay.get(fileDate).add(file);
            }
            else {
                log.warn("Ignoring file " + file.toURI().toString() + " as we were unable to parse for date");
            }
        }
        
        // Files are to be processed by full file first, followed by deltas in time
        // sequence. Full files are named: YYYYMMDD_tvdata.xml, deltas 
        // yyyymmddhhmm_YYYYMMDD_tvdata.xml, where yyyymmddhhmm is the timestamp of
        // the delta, and YYYYMMDD is the schedule date.
        
        for(List<File> filesForDay : filesByDay.values()) {
            Collections.sort(filesForDay, new Comparator<File>() {

                public int compare(File o1, File o2) {
                    if(o1.getName().length() != o2.getName().length()) {
                        return o1.getName().length() < o2.getName().length() ? -1 : 1;
                    }
                    else {
                        return o1.getName().compareTo(o2.getName());
                    }
                }
                
            });
        }
        return new HashSet<Queue<File>>(filesByDay.values());
    }
}
