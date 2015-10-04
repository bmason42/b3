package org.mason.b3;

import java.io.File;
import java.util.List;

/**
 * Created by bmason42 on 10/4/15.
 */
public class MainApp {
    public static void main(String[] args){
        if (args.length <1){
            System.err.println("Need  a path");
            System.exit(1);
        }

        File path=new File(args[0]);
        FileLister fl=new FileLister(path);
        List<FileMetaData> fileMetaDatas = fl.listFiles();
        final FileCopier copier=new FileCopier(fileMetaDatas);
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
