package org.atlasapi.remotesite.pa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.atlasapi.remotesite.pa.data.DefaultPaProgrammeDataStore;
import org.atlasapi.s3.S3Client;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.joda.time.DateTime;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.metabroadcast.common.time.DateTimeZones;

public class DefaultPaProgrammeDataStoreTest extends TestCase {

    private File dir = Files.createTempDir();
    private File oldFile;
    private File newFile;

    private final Mockery context = new Mockery();
    private final S3Client s3client = context.mock(S3Client.class);

    private final DefaultPaProgrammeDataStore fileManager = new DefaultPaProgrammeDataStore(dir.getAbsolutePath(), s3client);

    @Override
    protected void setUp() throws Exception {
        dir.mkdirs();
        dir.deleteOnExit();

        oldFile = new File(dir, "oldFile_tvdata.xml");
        FileUtils.writeStringToFile(oldFile, "oldFile");
        assertTrue(oldFile.setLastModified(new DateTime(DateTimeZones.UTC).minusDays(7).getMillis()));

        newFile = new File(dir, "newFile_tvdata.xml");
        FileUtils.writeStringToFile(newFile, "newFile");
        assertTrue(newFile.setLastModified(new DateTime(DateTimeZones.UTC).getMillis()));
    }

    public void testSave() throws Exception {
        context.checking(new Expectations() {
            {
                one(s3client).put(with("saveFile_tvData.xml"), with(any(File.class)));
            }
        });

        String content = "This is some data!";
        fileManager.save("saveFile_tvData.xml", new ByteArrayInputStream(content.getBytes()));

        assertThat(dir.list(), hasItemInArray("saveFile_tvData.xml"));
        for (File file : dir.listFiles()) {
            if (file.getName().equals("saveFile_tvData.xml")) {
                assertThat(Files.readFirstLine(file, Charsets.UTF_8), is(equalTo(content)));
            }
        }
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

        ftpFile.setSize(oldFile.length() * 2);

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
