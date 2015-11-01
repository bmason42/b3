package org.mason.b3;

import com.amazonaws.regions.Regions;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

/**
 * Created by bmason42 on 10/4/15.
 */
public class MainApp {
    public static void main(String[] args){
        if (args.length <1){
            System.err.println("usage is <path to file> [optionally a b3.properties file]");
            System.exit(1);
        }
        File propsFile;
        if (args.length>1){
            propsFile=new File(args[1]);
        }else{
            propsFile=new File(System.getProperty("user.home"),"b3.properties");
        }
        if (!propsFile.exists()){
            System.out.println("Could not find the properties file:" + propsFile.getAbsolutePath());
            System.exit(2);
        }
        Properties props=new Properties();
        try(FileInputStream in=new FileInputStream(propsFile)){
            props.load(in);
        }catch (Exception e){
            System.err.println("Unable to load propertirs file " + e.getLocalizedMessage());
            System.exit(3);
        }
        String s3Bucket=props.getProperty("s3Bucket");
        String accessId=props.getProperty("aws_access_key_id");
        String accessSecret=props.getProperty("aws_secret_access_key");
        String maxRetryString=props.getProperty("retryCount","5");
        String threadString=props.getProperty("threadCount","3");
        int threadCount=Integer.parseInt(threadString);
        int maxRetry=Integer.parseInt(maxRetryString);
        String region=props.getProperty("region", Regions.US_WEST_2.getName());


        File path=new File(args[0]);
        FileLister fl=new FileLister(path);
        List<FileMetaData> fileMetaDatas = fl.listFiles();
        final FileCopier copier=new FileCopier(fileMetaDatas);
        copier.setBucket(s3Bucket);
        copier.setMaxRetries(maxRetry);
        copier.setNumberOfThreads(threadCount);
        copier.setRegion(region);
        copier.setAccessId(accessId);
        copier.setAccessSecret(accessSecret);
        try {
            copier.checkBucketAndMakeIfNeeded();
        }catch (Exception e){
            System.err.println("Unable to login to S3 and check bucket " + e.getLocalizedMessage());
            System.exit(3);
        }
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                copier.start();
            }
        });
        t.setDaemon(true);
        t.start();

        while (!copier.isDone()){
            System.out.println("Doing " + copier.getCurrentCount() + " of " + fileMetaDatas.size() );
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Done");
    }
}
