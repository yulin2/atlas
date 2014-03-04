package org.atlasapi.remotesite.rovi.processing;


import static org.atlasapi.remotesite.rovi.RoviConstants.FILE_CHARSET;
import static org.atlasapi.remotesite.rovi.RoviConstants.UTF_16LE_BOM;
import static org.atlasapi.remotesite.rovi.RoviTestUtils.fileFromResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.atlasapi.remotesite.rovi.processing.RoviLineProcessor;
import org.junit.Test;

import com.google.common.io.Files;


public class RoviLineProcessorTest {

    private static final String PATH_FILE_WITHOUT_BOM = "org/atlasapi/remotesite/rovi/without_bom.txt";
    private static final String PATH_FILE_WITH_BOM = "org/atlasapi/remotesite/rovi/program.txt";

    @Test
    public void testDetectBomInLine() throws IOException {
        String path = PATH_FILE_WITH_BOM;
        String line = Files.readLines(fileFromResource(path), FILE_CHARSET).get(0);
        boolean startsWithBom = RoviLineProcessor.startsWithUTF16LEBom(line);
        assertTrue(startsWithBom);
        
        path = PATH_FILE_WITHOUT_BOM;
        line = Files.readLines(fileFromResource(path), FILE_CHARSET).get(0);
        startsWithBom = RoviLineProcessor.startsWithUTF16LEBom(line);
        assertFalse(startsWithBom);  
    }
    
    @Test
    public void testStripBomFromLine() throws IOException {
        String path = PATH_FILE_WITH_BOM;
        
        List<String> lines = Files.readLines(fileFromResource(path), FILE_CHARSET);
        String firstLine = lines.get(0);
        
        int sizeWithBom = firstLine.getBytes(FILE_CHARSET).length;
        int sizeWithoutBom = RoviLineProcessor.stripBom(firstLine).getBytes(FILE_CHARSET).length;
        
        assertEquals(UTF_16LE_BOM.length, sizeWithBom - sizeWithoutBom);
    }
}
