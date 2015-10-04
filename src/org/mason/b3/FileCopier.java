package org.mason.b3;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created by bmason42 on 10/4/15.
 */
public class FileCopier {
    private String calcHash(File f)throws Exception{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] buf=new byte[4096];
        String ret;
        try(FileInputStream in=new FileInputStream(f)) {
            int n=in.read(buf);
            while(n>0){
                md.update(buf,0,n);
                n=in.read(buf);
            }
            byte[] digest=md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : buf) {
                sb.append(String.format("%02X ", b));
            }
            ret=sb.toString().toUpperCase();
        }
        return ret;
    }
}
