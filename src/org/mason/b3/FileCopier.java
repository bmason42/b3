package org.mason.b3;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by bmason42 on 10/4/15.
 */
public class FileCopier {

    List<FileMetaData> files=new ArrayList<>();
    AtomicInteger nextFile=new AtomicInteger(0);
    int numberOfThreads=1;

    private String credtialProfile="default";
    private String bucket="b3-backup-bmason42";
    public static final String FILE_TIME_STAMP_KEY = "filetimestamp";
    public static final String HASH_KEY= "filehash";
    private AtomicBoolean done=new AtomicBoolean(false);

    public FileCopier(List<FileMetaData> files) {
        this.files = files;
    }


    public FileCopier(int numberOfThreads, List<FileMetaData> files) {
        this.numberOfThreads = numberOfThreads;
        this.files = files;
    }
    public boolean isDone(){
        return done.get();
    }
    public int getCurrentCount(){
        return nextFile.get();
    }
    public String getCredtialProfile() {
        return credtialProfile;
    }

    public void setCredtialProfile(String credtialProfile) {
        this.credtialProfile = credtialProfile;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public void start(){
        checkBucketAndMakeIfNeeded();
        final CountDownLatch latch=new CountDownLatch(numberOfThreads);
        for (int i=0;i<numberOfThreads;i++){
            Runnable run=new Runnable() {
                @Override
                public void run() {
                    try {
                        startCopyThread();
                    }catch (Throwable t){
                        t.printStackTrace();
                    }
                    latch.countDown();
                }
            };
            Thread t=new Thread(run,"S3 Backup-" +i);
            t.setDaemon(true);
            t.start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            //ignore
        }
        done.set(true);
        synchronized (this) {
            this.notifyAll();
        }

    }
    private void checkBucketAndMakeIfNeeded(){
        ProfileCredentialsProvider provider=new ProfileCredentialsProvider(credtialProfile);
        AmazonS3Client s3=new AmazonS3Client(provider);
        Regions usWest21 = Regions.US_WEST_2;
        com.amazonaws.regions.Region usWest2 = com.amazonaws.regions.Region.getRegion(usWest21);
        s3.setRegion(usWest2);
        List<Bucket> buckets = s3.listBuckets();
        Bucket bucketToUse=null;
        for (Bucket b:buckets){
            if (b.getName().equals(bucket)){
                bucketToUse=b;
                break;
            }
        }
        if (bucketToUse == null){
            s3.createBucket(bucket);
        }
    }
    private void startCopyThread() {
        ProfileCredentialsProvider provider=new ProfileCredentialsProvider(credtialProfile);
        AmazonS3Client s3=new AmazonS3Client(provider);
        Regions usWest21 = Regions.US_WEST_2;
        com.amazonaws.regions.Region usWest2 = com.amazonaws.regions.Region.getRegion(usWest21);
        s3.setRegion(usWest2);
        int n=nextFile.getAndIncrement();
        try {
            while (n < files.size()) {
                FileMetaData data=files.get(n);
                backupFileIfNeeded(s3,data);
                n = nextFile.getAndIncrement();
            }
        }catch (Throwable t){
            t.printStackTrace();

        }

    }

    private void backupFileIfNeeded(AmazonS3Client s3,FileMetaData data) throws Exception{
        String key=data.getS3Key();
        ObjectMetadata metadata =null;
        try {
            metadata = s3.getObjectMetadata(bucket, key);
        }catch (Exception e){
            metadata=null;
        }
        boolean saveFile=true;
        String localHash;
        try {
            localHash=calcHash(data.getFullLocalPath());
        } catch (Exception e) {
            localHash="";
            e.printStackTrace();
        }
        if (metadata != null){

            Map<String, String> userMetadata = metadata.getUserMetadata();
            String tsString = userMetadata.get(FILE_TIME_STAMP_KEY);
            long ts=tsString == null ? 0 :Long.parseLong(tsString);
            String s3Hash=metadata.getContentMD5();
            String userHash=userMetadata.get(HASH_KEY);
            s3Hash=s3Hash==null ? userHash :  s3Hash;

            saveFile=!localHash.equals(s3Hash);
        }else{
            metadata=new ObjectMetadata();

        }

        //metadata.setContentMD5(localHash);
        if (saveFile) {
            metadata.setContentMD5(localHash);
            metadata.setContentLength(data.getFullLocalPath().length());
            metadata.getUserMetadata().put(FILE_TIME_STAMP_KEY, String.valueOf(data.getTimestamp()));
            metadata.getUserMetadata().put(HASH_KEY,localHash);
            try (FileInputStream in = new FileInputStream(data.getFullLocalPath())) {
                PutObjectResult result = s3.putObject(bucket,key,in,metadata);
                System.out.println(result);
            }
        }


    }

    private String calcHash(File f)throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buf=new byte[4096];
        String ret;
        try(FileInputStream in=new FileInputStream(f)) {
            int n=in.read(buf);
            while(n>0){
                md.update(buf,0,n);
                n=in.read(buf);
            }
            byte[] digest=md.digest();
            /*
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            ret=sb.toString().toUpperCase();
            */
            ret=Base64.getEncoder().encodeToString(digest);
        }
        return ret;
    }
}
