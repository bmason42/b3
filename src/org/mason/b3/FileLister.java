package org.mason.b3;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Created by bmason42 on 10/4/15.
 */
public class FileLister {
    File basePath;
    String basePathString;
    List<File> skippedFiles=new ArrayList<>();

    public FileLister(File basePath) {
        this.basePath = basePath;
        basePathString=basePath.getAbsolutePath();
    }

    public List<FileMetaData> listFiles(){
        if (!basePath.exists()  || !basePath.isDirectory()){
            throw new B3Exception("Invalid Base Path");
        }

        List<FileMetaData> ret=recurseFiles(basePath);
        return ret;
    }

    private List<FileMetaData> recurseFiles(File path) {

        List<FileMetaData> ret=new ArrayList<FileMetaData>();
        File[] files = path.listFiles();
        List<File> dirs=new ArrayList<File>();
        for (File f:files){
            if (f.isDirectory()){
                dirs.add(f);
            }else{
                try {
                    FileMetaData data=new FileMetaData();
                    data.setName(f.getName());
                    String filePath = f.getAbsolutePath();
                    //strip off base
                    String relativePath=filePath.substring(basePathString.length()+1);
                    //strip off file name
                    relativePath=relativePath.substring(0,relativePath.length() - f.getName().length());
                    //normalize path on windows..  May not be needed, not sure really
                    relativePath=relativePath.replace(File.pathSeparatorChar,'/');
                    data.setRelativePath(relativePath);
                    data.setSize(f.length());
                    data.setTimestamp(f.lastModified());
                    data.setFullLocalPath(f);
                    ret.add(data);
                } catch (Exception e) {
                    skippedFiles.add(f);
                    e.printStackTrace();
                }

            }
        }
        for (File dir:dirs){
            List<FileMetaData> sublist = recurseFiles(dir);
            ret.addAll(sublist);
        }
        return ret;
    }



}
