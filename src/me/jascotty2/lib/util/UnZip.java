/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for unzipping a zip archive, or files from one
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.jascotty2.lib.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class UnZip {

    public static boolean isZip(String filename){
        try {
            ZipFile zipFile = new ZipFile(filename);
            zipFile.close();
            return true;
        } catch (IOException ex) {
        }
        return false;
    }

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
        } else if (dest.length() > 0 && dest.charAt(dest.length() - 1) != File.separatorChar) {
            dest += File.separatorChar;
        }
        if (dest.length() > 0 && !(new File(dest)).exists()) {
            (new File(dest)).mkdir();
        }
        try {
			// (based on) method described at http://www.devx.com/getHelpOn/10MinuteSolution/20447
			// by Daniel F. Savarese
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

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
        return unzip(file, new String[]{subPathToExtract}, dest);
    }

    public static boolean unzip(String file, String subPathsToExtract[], String dest) {
        if (file == null) {
            return false;
        }
        if (dest == null) {
            dest = "";
        } else if (dest.length() > 0 && dest.charAt(dest.length() - 1) != File.separatorChar) {
            dest += File.separatorChar;
        }
        if (dest.length() > 0 && !(new File(dest)).exists()) {
            (new File(dest)).mkdir();
        }
        try {
            boolean good = false; // TODO: parallel array & stop if array fills
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                for (String s : subPathsToExtract) {
                    if (entry.getName().equals(s)) {
                        String extractfilename = (new File(s)).getName();
                        //System.out.println("saving " + extractfilename + " at " + dest + extractfilename);
                        copyInputStream(zipFile.getInputStream(entry),
                                new BufferedOutputStream(new FileOutputStream(dest + extractfilename)));
                        good = true;
                    }
                }
            }
            zipFile.close();
            return good;
        } catch (IOException ioe) {
            System.err.println("UnZip Error:");
            ioe.printStackTrace();
            return false;
        }
    }
}
