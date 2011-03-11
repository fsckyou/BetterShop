
/*
 * Base Class taken from Help
 * https://github.com/tkelly910/Help
 * 
 */
package com.jascotty2;

import java.util.LinkedList;

public class MinecraftFontWidthCalculator {

    private static String charWidthIndexIndex = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";
    private static int[] charWidths = {4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6,
        6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6,
        6, 6, 5, 2, 5, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 6, 6, 7,
        6, 6, 6, 2, 6, 6, 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9,
        9, 9, 5, 9, 9, 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7, 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1};

    public static int getStringWidth(String s) {
        int i = 0;
        if (s != null) {
            for (int j = 0; j < s.length(); j++) {
                i += getCharWidth(s.charAt(j));
            }
        }
        return i;
    }

    public static int getCharWidth(char c) {
        int k = charWidthIndexIndex.indexOf(c);
        if (c != '\247' && k >= 0) {
            return charWidths[k];
        }
        return 0;
    }

    public static int getCharWidth(char c, int defaultReturn) {
        int k = charWidthIndexIndex.indexOf(c);
        if (c != '\247' && k >= 0) {
            return charWidths[k];
        }
        return defaultReturn;
    }

    /**
     * pads str on the right with pad (left-align)
     * @param str string to format
     * @param len spaces to pad
     * @param pad character to use when padding
     * @return str with padding appended
     */
    public static String strPadRight(String str, int len, char pad) {
        // int width = 325;
        // for purposes of this function, assuming a normal char to be 6
        len *= 6;
        len -= getStringWidth(str);
        return str + unformattedStrRepeat(pad, len / getCharWidth(pad, 6));
    }

    /**
     * pads str on the left with pad (right-align)
     * @param str string to format
     * @param len spaces to pad
     * @param pad character to use when padding
     * @return str with padding prepended
     */
    public static String strPadLeft(String str, int len, char pad) {
        // for purposes of this function, assuming a normal char to be 6
        len *= 6;
        len -= getStringWidth(str);
        return unformattedStrRepeat(pad, len / getCharWidth(pad, 6)) + str;
    }
    /**
     * pads str on the left & right with pad (center-align)
     * @param str string to format
     * @param len spaces to pad
     * @param pad character to use when padding
     * @return str centered with pad
     */
    public static String strPadCenter(String str, int len, char pad) {
        // for purposes of this function, assuming a normal char to be 6
        len *= 6;
        len -= getStringWidth(str);
        int padwid = getCharWidth(pad, 6);
        int prepad = (len / padwid) / 2;
        len-=prepad *padwid;
        return unformattedStrRepeat(pad, prepad) + str + unformattedStrRepeat(pad, len / padwid);
    }

    public static String unformattedPadRight(String str, int len, char pad) {
        for (int i = str.length(); i < len; ++i) {
            str += pad;
        }
        return str;
    }

    public static String unformattedPadLeft(String str, int len, char pad) {
        return unformattedStrRepeat(pad, len - str.length()) + str;
    }
    public static String unformattedPadCenter(String str, int len, char pad) {
        len -=str.length();
        int prepad = len / 2;
        return unformattedStrRepeat(pad, prepad) + str + unformattedStrRepeat(pad, len-prepad);
    }

    public static String unformattedStrRepeat(char ch, int len) {
        String str = "";
        for (int i = 0; i < len; ++i) {
            str += ch;
        }
        return str;
    }

    public static LinkedList<String> alignTags(LinkedList<String> input, boolean minecraftChatFormat) {
        for (String fm : new String[]{"l", "r", "c"}) {
            while (input.get(1).contains("<" + fm)) {
                char repl = ' ';
                if (input.get(1).matches("^.*<" + fm + ".>.*$")) {// || input.get(1).matches("^.*<r.>.*$")) {
                    repl = input.get(1).substring(input.get(1).indexOf("<" + fm)+2, input.get(1).indexOf(">")).charAt(0);
                    for (int i = 0; i < input.size(); ++i) {
                        input.set(i, input.get(i).replaceFirst("<" + fm + ".>", "<" + fm + ">"));
                    }
                }

                int maxPos = 0;
                for (int i = 1; i < input.size(); ++i) {
                    if (input.get(i).indexOf("<" + fm + ">") > maxPos) {
                        maxPos = input.get(i).indexOf("<" + fm + ">");
                    }
                }

                LinkedList<String> newinput = new LinkedList<String>();
                for (int i = 0; i < input.size(); ++i) {
                    String line = input.get(i);
                    if (line.indexOf("<" + fm + ">") != -1) {
                        if (fm.equals("l")) {
                            if (minecraftChatFormat) {
                                newinput.add(MinecraftFontWidthCalculator.strPadRight(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            } else {
                                newinput.add(MinecraftFontWidthCalculator.unformattedPadRight(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        } else if(fm.equals("c")) {
                            if (minecraftChatFormat) {
                                newinput.add(MinecraftFontWidthCalculator.strPadCenter(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            } else {
                                newinput.add(MinecraftFontWidthCalculator.unformattedPadCenter(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        } else {
                            if (minecraftChatFormat) {
                                newinput.add(MinecraftFontWidthCalculator.strPadLeft(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            } else {
                                newinput.add(MinecraftFontWidthCalculator.unformattedPadLeft(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        }
                    } else {
                        newinput.add(line);
                    }
                }
                input = newinput;
            }
        }
        return input;
    }
}
