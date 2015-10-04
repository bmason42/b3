package org.mason.b3;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by bmason42 on 10/4/15.
 */
public class FileListerTest {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testListFiles() throws Exception {
        File base=new File("testdata");
        FileLister lister=new FileLister(base);
        List<FileMetaData> files = lister.listFiles();
        Assert.assertNotNull(files);
        Assert.assertEquals(8,files.size());
        for (FileMetaData d:files){
            System.out.println(d.getRelativePath() + " / " + d.getName());
            File dir=new File(base,d.getRelativePath());
            File f=new File(dir,d.getName());
            System.out.println(f.getAbsolutePath());
            Assert.assertTrue(f.exists());
            Assert.assertTrue(d.getSize() != 0);
            Assert.assertTrue(d.getTimestamp() != 0);
        }
    }
}