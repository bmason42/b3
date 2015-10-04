package org.mason.b3;

import java.io.File;

/**
 * Created by bmason42 on 10/4/15.
 */
public class FileMetaData {
    private File fullLocalPath;
    private String name;
    private String relativePath;
    private long size;
    private long timestamp;

    public FileMetaData() {
    }

    public File getFullLocalPath() {
        return fullLocalPath;
    }

    public void setFullLocalPath(File fullLocalPath) {
        this.fullLocalPath = fullLocalPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }



    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getS3Key(){
        return relativePath + name;
    }
}
