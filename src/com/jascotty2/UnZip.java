//http://www.devx.com/getHelpOn/10MinuteSolution/20447
package com.jascotty2;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class UnZip {

    public static void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    public static boolean unzip(String file, String dest) {
        if (file == null) {
            return false;
        }
        if (dest == null) {
            dest = "";
        } else if (dest.charAt(dest.length() - 1) != File.separatorChar) {
            dest += File.separatorChar;
        }
        if (dest.length() > 0 && !(new File(dest)).exists()) {
            (new File(dest)).mkdir();
        }
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then children.
                    //System.err.println("Extracting directory: " + entry.getName());
                    (new File(dest + entry.getName())).mkdir();
                    continue;
                }
                
                //System.err.println("Extracting file: " + entry.getName());
                copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(dest + entry.getName())));
            }

            zipFile.close();
            return true;
        } catch (IOException ioe) {
            System.err.println("UnZip Error:");
            ioe.printStackTrace();
            return false;
        }
    }
    
    public static boolean unzip(String file, String subPathToExtract, String dest) {
        if (file == null) {
            return false;
        }
        if (dest == null) {
            dest = "";
        } else if (dest.charAt(dest.length() - 1) != File.separatorChar) {
            dest += File.separatorChar;
        }
        if (dest.length() > 0 && !(new File(dest)).exists()) {
            (new File(dest)).mkdir();
        }
        String extractfilename = (new File(subPathToExtract)).getName();
        try {
            ZipFile zipFile = new ZipFile(file);
            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if(entry.getName().equals(subPathToExtract)){
                    copyInputStream(zipFile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(dest + extractfilename)));
                    return true;
                }
            }
            zipFile.close();
            return false;
        } catch (IOException ioe) {
            System.err.println("UnZip Error:");
            ioe.printStackTrace();
            return false;
        }
    }
}
