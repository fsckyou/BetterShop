/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for reading/writing files
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
package me.jascotty2.lib.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author jacob
 */
public class FileIO {

    public static ArrayList<String[]> loadCSVFile(File toLoad) throws FileNotFoundException, IOException {
        ArrayList<String[]> ret = new ArrayList<String[]>();
        if (toLoad.exists() && toLoad.isFile() && toLoad.canRead()) {
            FileReader fstream = null;
            fstream = new FileReader(toLoad.getAbsolutePath());
            BufferedReader in = new BufferedReader(fstream);
            try {
                int n = 0;
                for (String line = null; (line = in.readLine()) != null && line.length() > 0; ++n) {
                    // if was edited in openoffice, will instead have semicolins..
                    ret.add(line.replace(";", ",").replace(",,", ", ,").split(","));
                }
            } finally {
                in.close();
            }
        }
        return ret;
    }

    public static boolean saveFile(File toSave, String[] lines) throws IOException {
        if (!toSave.exists() && !toSave.createNewFile()) {
            return false;
        }
        if (toSave.canWrite()) {
            FileWriter fstream = null;
            fstream = new FileWriter(toSave.getAbsolutePath());
            //System.out.println("writing to " + tosave.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);
            for (String line : lines) {
                out.write(line);
                out.newLine();
            }
            out.flush();
            out.close();
            return true;
        }
        return false;
    }

    public static boolean saveFile(File toSave, ArrayList<String> lines) throws IOException {
        if (!toSave.exists()) {
            // TODO: first check if directory exists, then create the file
            File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
            dir.mkdirs();
            try {
                if (!toSave.createNewFile()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        if (toSave.canWrite()) {
            FileWriter fstream = null;
            fstream = new FileWriter(toSave.getAbsolutePath());
            //System.out.println("writing to " + tosave.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);
            for (String line : lines) {
                out.write(line);
                out.newLine();
            }
            out.flush();
            out.close();
            return true;
        }
        return false;
    }

    public static boolean saveCSVFile(File toSave, ArrayList<String[]> lines) throws IOException {
        if (!toSave.exists()) {
            // TODO: first check if directory exists, then create the file
            File dir = new File(toSave.getAbsolutePath().substring(0, toSave.getAbsolutePath().lastIndexOf(File.separatorChar)));
            dir.mkdirs();
            try {
                if (!toSave.createNewFile()) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        if (toSave.canWrite()) {
            FileWriter fstream = null;
            fstream = new FileWriter(toSave.getAbsolutePath());
            //System.out.println("writing to " + tosave.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);
            for (String line[] : lines) {
                for (int i = 0; i < line.length; ++i) {
                    out.write(line[i]);
                    if (i + 1 < line.length) {
                        out.write(",");
                    }
                }
                out.newLine();
            }
            out.flush();
            out.close();
            return true;
        }
        return false;
    }
} // end class CSV

