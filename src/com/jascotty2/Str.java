/**
 * Programmer: Jacob Scott
 * Program Name: Str
 * Description:
 * Date: Mar 31, 2011
 */

package com.jascotty2;

/**
 * @author jacob
 */
public class Str {


    public static String argStr(String[] s) {
        return argStr(s, " ");
    }

    public static String argStr(String[] s, String sep) {
        String ret = "";
        if (s != null) {
            for (int i = 0; i < s.length; ++i) {
                ret += s[i];
                if (i + 1 < s.length) {
                    ret += sep;
                }
            }
        }
        return ret;
    }

    public static boolean isIn(String input, String[] check) {
        input = input.trim();
        for (String c : check) {
            if (input.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIn(String input, String check) {
        String comms[] = check.split(",");
        input = input.trim();
        for (String c : comms) {
            if (input.equalsIgnoreCase(c.trim())) {
                return true;
            }
        }
        return false;
    }
} // end class Str
