/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: downloads & installs a dependency jar
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
package me.jascotty2.lib.net;

import me.jascotty2.lib.util.Rand;
import me.jascotty2.lib.util.UnZip;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class InstallDependency {

    /**
     * Download from one of the provided mirrors & save to a file
     * @param fileName result filename
     * @param subpath if there is a zip to download, what jar to extract from it
     * @param mirrors list of urls that can be used
     * @return true if found a working url & the download succeeded
     */
    public static boolean install(String fileName, String subpath, String... mirrors) {
        return install(fileName, new String[]{subpath}, mirrors);
    }

    /**
     * Download from one of the provided mirrors & save to a file
     * @param fileName result filename
     * @param subpath if there is a zip to download, what jar file[s] to extract from it
     * @param mirrors list of urls that can be used
     * @return true if found a working url & the download succeeded
     */
    public static boolean install(String fileName, String subpath[], String... mirrors) {
        boolean used[] = new boolean[mirrors.length];
        Arrays.fill(used, false);
        while (!allTrue(used)) {
            int i = Rand.RandomInt(0, used.length - 1);
            while (used[i]) {
                i = Rand.RandomInt(0, used.length - 1);
            }
            used[i] = true;
            if (FileDownloader.goodLink(mirrors[i])) {
                if (mirrors[i].toLowerCase().endsWith(".zip")
                        // sourceforde download autoselect mirrors have /download after filename
                        // eg http://sourceforge.net/projects/chesspresso/files/chesspresso-lib/0.9.2/Chesspresso-lib-0.9.2.zip/download
                        // or http://downloads.sourceforge.net/project/chesspresso/chesspresso-lib/0.9.2/Chesspresso-lib-0.9.2.zip?r=&ts=1311394055&use_mirror=heanet
                        || (mirrors[i].replace("/", "").endsWith("download") &&
                        mirrors[i].replace("/", "").replace("download", "").endsWith(".zip"))
                        || (mirrors[i].contains("?")
                        && mirrors[i].substring(0, mirrors[i].indexOf("?")).endsWith(".zip"))) {
                    return installzip(mirrors[i], subpath, fileName);
                } else {
                    return install(mirrors[i], fileName);
                }
            }
        }
        System.out.println("download inaccessable");
        return false;
    }

    static boolean allTrue(boolean array[]) {
        for (boolean b : array) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public static boolean install(String location, String filename) {
        try {
            System.out.println("   + " + filename + " downloading...");
            FileDownloader.download(location, filename);
            System.out.println("   - " + filename + " finished.");
            return true;
        } catch (IOException ex) {
            System.out.println("Error Downloading File: " + ex);
            return false;
        }
    }

    public static boolean installzip(String location, String filename) {
        install(location, filename + ".zip");

        boolean success = UnZip.unzip(filename + ".zip", getDirectory(filename));
        (new File(filename + ".zip")).delete();
        return success;
    }

    public static boolean installzip(String location, String fileToExtract, String filename) {
        if (fileToExtract == null) {
            return installzip(location, filename);
        } else {
            return installzip(location, new String[]{fileToExtract}, filename);
        }
    }

    public static boolean installzip(String location, String filesToExtract[], String filename) {
        if (filesToExtract == null || filesToExtract.length == 0) {
            return installzip(location, filename);
        } else {
            install(location, filename + ".zip");

            boolean success = UnZip.unzip(filename + ".zip", filesToExtract, getDirectory(filename));
            (new File(filename + ".zip")).delete();
            return success;
        }
    }

    /**
     * extract the directory portion from a filename
     * @param file
     * @return
     */
    static String getDirectory(String file) {
        return file != null && file.contains(File.separator) ?
            file.substring(0, file.lastIndexOf(File.separatorChar))
            : "";
    }
} // end class InstallDependency

