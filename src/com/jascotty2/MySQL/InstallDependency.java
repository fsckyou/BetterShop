// downloads & installs MySQL dependency (base class from iConomy)
package com.jascotty2.MySQL;

import com.jascotty2.UnZip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URLConnection;
import java.net.URL;
/**
 * @author jacob
 */
public class InstallDependency {

    protected static int count, total, itemCount, itemTotal;
    protected static long lastModified;
    protected static String error;
    protected static boolean cancelled;

    public InstallDependency() {
    }

    public synchronized void cancel() {
        cancelled = true;
    }

    public static boolean install() {
        if (canUse("http://mirror.services.wisc.edu/mysql/Downloads/Connector-J/mysql-connector-java-5.1.15.zip")) {
            if (installzip("http://mirror.services.wisc.edu/mysql/Downloads/Connector-J/mysql-connector-java-5.1.15.zip",
                    "mysql-connector-java-5.1.15/mysql-connector-java-5.1.15-bin.jar",
                    "mysql-connector-java-bin.jar")) {
                return true;
            }
        }
        if (canUse("http://mirror.anigaiku.com/Dependencies/mysql-connector-java-bin.jar")) {
            if (install("http://mirror.anigaiku.com/Dependencies/mysql-connector-java-bin.jar",
                    "mysql-connector-java-bin.jar")) {
                return true;
            }
        }
        System.out.println("download inaccessable");
        return false;
    }

    static boolean canUse(String location) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            // note : you may also need HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) new URL(location).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
    }

    public static boolean install(String location, String filename) {
        try {
            cancelled = false;
            count = total = itemCount = itemTotal = 0;
            if (cancelled) {
                return false;
            }
            System.out.println("   + " + filename + " downloading...");
            download(location, filename);
            System.out.println("   - " + filename + " finished.");
            return true;
        } catch (IOException ex) {
            System.out.println("Error Downloading File: " + ex);
            return false;
        }
    }

    public static boolean installzip(String location, String filename) {
        install(location, filename + ".zip");

        boolean success = UnZip.unzip("lib" + File.separator + filename + ".zip", "lib" + File.separator);
        (new File("lib" + File.separator + filename + ".zip")).delete();
        return success;
    }

    public static boolean installzip(String location, String fileToExtract, String filename) {
        install(location, filename + ".zip");

        boolean success = UnZip.unzip("lib" + File.separator + filename + ".zip", fileToExtract, "lib" + File.separator);
        (new File("lib" + File.separator + filename + ".zip")).delete();
        return success;
    }

    protected static synchronized void download(String location, String filename) throws IOException {
        URLConnection connection = new URL(location).openConnection();
        connection.setUseCaches(false);
        lastModified = connection.getLastModified();
        //int filesize = connection.getContentLength();
        String destination = "lib" + File.separator + filename;
        File parentDirectory = new File(destination).getParentFile();

        if (parentDirectory != null) {
            parentDirectory.mkdirs();
        }

        InputStream in = connection.getInputStream();
        OutputStream out = new FileOutputStream(destination);

        byte[] buffer = new byte[65536];
        //int currentCount = 0;
        for (;;) {
            if (cancelled) {
                break;
            }

            int count = in.read(buffer);

            if (count < 0) {
                break;
            }

            out.write(buffer, 0, count);
            //currentCount += count;
        }

        in.close();
        out.close();
    }

    public long getLastModified() {
        return lastModified;
    }
} // end class InstallDependency

