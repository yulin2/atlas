package org.atlasapi.remotesite.pa;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
import org.atlasapi.s3.S3Client;
import org.jmock.Mockery;
import org.joda.time.DateTime;

import com.metabroadcast.common.time.DateTimeZones;

public class DefaultPaProgrammeDataStoreTest extends TestCase {

    private File oldFile;
    private File newFile;
    
    private final Mockery context = new Mockery();
    private final S3Client s3client = context.mock(S3Client.class);
    private final DefaultPaProgrammeDataStore fileManager = new DefaultPaProgrammeDataStore("/tmp/atlas", s3client );
    
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
    
    public void testRequiresUpdatingWhenFileIsNew() {
        FTPFile ftpFile = new FTPFile();
        ftpFile.setName("unseen_tvdata.xml");
        assertTrue(fileManager.requiresUpdating(ftpFile));
    }
    
    public void testRequiresUpdatingWhenFileIsMoreRecent() {
        FTPFile ftpFile = new FTPFile();
        ftpFile.setName("oldFile_tvdata.xml");
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(new DateTime(DateTimeZones.UTC).toDate());
        ftpFile.setTimestamp(cal);
        
        assertTrue(fileManager.requiresUpdating(ftpFile));
    }
    
    public void testRequiresUpdatingWhenFileIsDifferentSize() {
        FTPFile ftpFile = new FTPFile();
        ftpFile.setName("oldFile_tvdata.xml");
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(oldFile.lastModified());
        ftpFile.setTimestamp(cal);
        
        ftpFile.setSize(oldFile.length()*2);
        
        assertTrue(fileManager.requiresUpdating(ftpFile));
    }
    
    public void testRequiresUpdatingWhenFileIsSame() {
        FTPFile ftpFile = new FTPFile();
        ftpFile.setName("oldFile_tvdata.xml");
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(oldFile.lastModified());
        ftpFile.setTimestamp(cal);
        
        ftpFile.setSize(oldFile.length());
        
        assertFalse(fileManager.requiresUpdating(ftpFile));
    }
}
