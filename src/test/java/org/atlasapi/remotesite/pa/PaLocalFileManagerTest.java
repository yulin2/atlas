package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.DateTimeZones;

public class PaLocalFileManagerTest extends TestCase {

    private File oldFile;
    private File newFile;
    private final PaLocalFileManager fileManager = new PaLocalFileManager(null, null, null, null, "/tmp/atlas", null, null);
    
    @Override
    protected void setUp() throws Exception {
        File dir = new File("/tmp/atlas");
        dir.mkdirs();
        
        oldFile = new File("/tmp/atlas/oldFile_tvdata.xml");
        FileUtils.writeStringToFile(oldFile, "oldFile");
        assertTrue(oldFile.setLastModified(new DateTime(DateTimeZones.UTC).minusDays(7).getMillis()));
        
        newFile = new File("/tmp/atlas/newFile_tvdata.xml");
        FileUtils.writeStringToFile(newFile, "newFile");
        assertTrue(newFile.setLastModified(new DateTime(DateTimeZones.UTC).getMillis()));
    }
    
    public void testShouldRetrieveRecentlyUpdatedFiles() {
        List<File> files = fileManager.recentlyUpdatedFiles();
        assertTrue(files.contains(newFile));
        assertFalse(files.contains(oldFile));
    }
}
