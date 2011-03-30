/**
 * Programmer: Jacob Scott
 * Program Name: Rand
 * Description:
 * Date: Mar 29, 2011
 */
package com.jascotty2;

import java.util.Date;
import java.util.Random;

/**
 * @author jacob
 */
public class Rand {

    static Random rand = new Random();
    public static char filenameChars[] = {};
    protected static boolean isRand = false;

    static String randFname() {
        return randFname(10, 25);
    }

    static String randFname(int length) {
        return randFname(length, length);
    }

    static String randFname(int minlength, int maxlength) {
        if (filenameChars.length == 0) {
            // populate with alphanumerical chars
            filenameChars = new char[62];
            int n = 48;
            for (int i = 0; i < 62; ++i) {
                filenameChars[i] = (char) n++;
                if (n == 58) {
                    n = 65;
                } else if (n == 91) {
                    n = 97;
                }
            }
        }
        String ret = "";
        for(int i = RandomInt(minlength, maxlength); i>0; --i){
            ret+=filenameChars[RandomInt(0, filenameChars.length-1)];
        }
        return ret;
    }
    public static int RandomInt(int min, int max) {
        if (!isRand) {
            rand.setSeed((new Date()).getTime());
            isRand = true;
        }
        return min + rand.nextInt(max - min);
    }
} // end class Rand

