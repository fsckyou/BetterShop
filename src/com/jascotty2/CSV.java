/**
 * Programmer: Jacob Scott
 * Program Name: CSV
 * Description:
 * Date: Mar 19, 2011
 */
package com.jascotty2;

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
public class CSV {

    public static ArrayList<String[]> loadFile(File toLoad) throws FileNotFoundException, IOException {
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

    public static boolean saveCSVFile(File toSave, ArrayList<String[]> lines) throws IOException {
        if (!toSave.exists() && !toSave.createNewFile()) {
            return false;
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

