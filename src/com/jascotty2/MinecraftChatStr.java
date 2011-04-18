
/*
 * character widths taken from Help's MinecraftFontWidthCalculator
 * https://github.com/tkelly910/Help
 * 
 */
package com.jascotty2;

import java.util.LinkedList;
import org.bukkit.ChatColor;

public class MinecraftChatStr {

    private static String charWidthIndexIndex = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_'abcdefghijklmnopqrstuvwxyz{|}~⌂ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»";
    private static int[] charWidths = {4, 2, 5, 6, 6, 6, 6, 3, 5, 5, 5, 6, 2, 6, 2, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 2, 2, 5, 6, 5, 6, 7, 6, 6, 6, 6, 6, 6, 6,
        6, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 5, 6, 6, 2, 6, 5, 3, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 6, 6,
        6, 6, 5, 2, 5, 7, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 3, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 6, 3, 6, 6, 6, 6, 6, 6, 6, 7,
        6, 6, 6, 2, 6, 6, 8, 9, 9, 6, 6, 6, 8, 8, 6, 8, 8, 8, 8, 8, 6, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 6, 9,
        9, 9, 5, 9, 9, 8, 7, 7, 8, 7, 8, 8, 8, 7, 8, 8, 7, 9, 9, 6, 7, 7, 7, 7, 7, 9, 6, 7, 8, 7, 6, 6, 9, 7, 6, 7, 1};

    public static int getStringWidth(String s) {
        int i = 0;
        if (s != null) {
            s = s.replaceAll("\\u00A7.", "");
            for (int j = 0; j < s.length(); j++) {
                if (s.charAt(j) >= 0) {
                    i += getCharWidth(s.charAt(j));
                }
            }
        }
        return i;
    }

    public static String uncoloredStr(String s) {
        return s != null ? s.replaceAll("\\u00A7.", "") : s;
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
        return str + Str.repeat(pad, len / getCharWidth(pad, 6));
    }

    public static String strPadRightChat(String str, int abslen, char pad) {
        // int width = 325;
        abslen -= getStringWidth(str);
        return str + Str.repeat(pad, abslen / getCharWidth(pad, 6));
    }

    public static String strPadRightChat(String str, char pad) {
        int width = 325 - getStringWidth(str);
        return str + Str.repeat(pad, width / getCharWidth(pad, 6));
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
        return Str.repeat(pad, len / getCharWidth(pad, 6)) + str;
    }

    public static String strPadLeftChat(String str, int abslen, char pad) {
        // int width = 325;
        abslen -= getStringWidth(str);
        return Str.repeat(pad, abslen / getCharWidth(pad, 6)) + str;
    }

    public static String strPadLeftChat(String str, char pad) {
        int width = 325 - getStringWidth(str);
        return Str.repeat(pad, width / getCharWidth(pad, 6)) + str;
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
        len -= prepad * padwid;
        return Str.repeat(pad, prepad) + str + Str.repeat(pad, len / padwid);
    }

    public static String strPadCenterChat(String str, int abslen, char pad) {
        // int width = 325;
        abslen -= getStringWidth(str);
        int padwid = getCharWidth(pad, 6);
        int prepad = (abslen / padwid) / 2;
        abslen -= prepad * padwid;
        return Str.repeat(pad, prepad) + str + Str.repeat(pad, abslen / padwid);
    }

    public static String strPadCenterChat(String str, char pad) {
        int width = 325 - getStringWidth(str);
        int padwid = getCharWidth(pad, 6);
        int prepad = (width / padwid) / 2;
        width -= prepad * padwid;
        return Str.repeat(pad, prepad) + str + Str.repeat(pad, width / padwid);
    }


    private static boolean containsAlignTag(String str, String tag) {
        int pos = str.indexOf("<" + tag);
        if (pos >= 0) {
            return str.length() > pos + ("<" + tag).length()
                    && (str.charAt(pos + ("<" + tag).length()) == '>'
                    || str.charAt(pos + ("<" + tag).length() + 1) == '>');
        }
        return false;
    }

    public static LinkedList<String> alignTags(LinkedList<String> input, boolean minecraftChatFormat) {
        for (String fm : new String[]{"l", "r", "c"}) {
            while (containsAlignTag(input.get(1), fm)) {
                char repl = ' ';
                if (input.get(1).matches("^.*<" + fm + ".>.*$")) {// || input.get(1).matches("^.*<r.>.*$")) {
                    repl = input.get(1).substring(input.get(1).indexOf("<" + fm) + 2).charAt(0); //, input.get(1).indexOf(">")
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
                                newinput.add(MinecraftChatStr.strPadRight(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            } else {
                                newinput.add(Str.padRight(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        } else if (fm.equals("c")) {
                            if (minecraftChatFormat) {
                                newinput.add(MinecraftChatStr.strPadCenter(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            } else {
                                newinput.add(Str.padCenter(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            }
                        } else {
                            if (minecraftChatFormat) {
                                newinput.add(MinecraftChatStr.strPadLeft(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
                            } else {
                                newinput.add(Str.padLeft(line.substring(0, line.indexOf("<" + fm + ">")), maxPos, repl) + line.substring(line.indexOf("<" + fm + ">") + 3));
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

    public static String getChatColor(String col) {
        String def = ChatColor.WHITE.toString();//"\u00A70";
        if (col == null || col.length() == 0) {
            return def;
        } else if (col.length() >= 2 && col.startsWith("\u00A7")) {
            return col.substring(0, 2);
        }
        col = col.toLowerCase().trim();
        /*
        #       &0 is black
        #       &1 is dark blue
        #       &2 is dark green
        #       &3 is dark sky blue
        #       &4 is red
        #       &5 is magenta
        #       &6 is gold or amber
        #       &7 is light grey
        #       &8 is dark grey
        #       &9 is medium blue
        #       &2 is light green
        #       &b is cyan
        #       &c is orange-red
        #       &d is pink
        #       &e is yellow
        #       &f is white
         */
        if (col.equalsIgnoreCase("black")) {
            return ChatColor.BLACK.toString();//"\u00A70"; //String.format("\u00A7%x", 0x0);//
        } else if (col.equals("blue") || col.equals("dark blue")) {
            return ChatColor.DARK_BLUE.toString();//"\u00A71"; // String.format("\u00A7%x", 0x1);//
        } else if (col.equals("green") || col.equals("dark green")) {
            return ChatColor.DARK_GREEN.toString();//"\u00A72"; // String.format("\u00A7%x", 0x2);//
        } else if (col.equals("sky blue") || col.equals("dark sky blue") || col.equals("aqua")) {
            return ChatColor.DARK_AQUA.toString();//"\u00A73"; // String.format("\u00A7%x", 0x3);//
        } else if (col.equals("red") || col.equals("dark red")) {
            return ChatColor.DARK_RED.toString();//"\u00A74"; // String.format("\u00A7%x", 0x4);//
        } else if (col.equals("magenta") || col.equals("purple")) {
            return ChatColor.DARK_PURPLE.toString();//"\u00A75"; // String.format("\u00A7%x", 0x5);//
        } else if (col.equals("gold") || col.equals("amber") || col.equals("dark yellow")) {
            return ChatColor.GOLD.toString();//"\u00A76"; // String.format("\u00A7%x", 0x6);//
        } else if (col.equals("light gray") || col.equals("light grey")) {
            return ChatColor.GRAY.toString();//"\u00A77"; // String.format("\u00A7%x", 0x7);//
        } else if (col.equals("dark gray") || col.equals("dark grey") || col.equals("gray") || col.equals("grey")) {
            return ChatColor.DARK_GRAY.toString();//"\u00A78"; // String.format("\u00A7%x", 0x8);//
        } else if (col.equals("medium blue")) {
            return ChatColor.BLUE.toString();//"\u00A79"; // String.format("\u00A7%x", 0x9);//
        } else if (col.equals("light green") || col.equals("lime") || col.equals("lime green")) {
            return ChatColor.GREEN.toString();//"\u00A7a"; // String.format("\u00A7%x", 0xA);//
        } else if (col.equals("cyan") || col.equals("light blue")) {
            return ChatColor.AQUA.toString();//"\u00A7b"; // String.format("\u00A7%x", 0xB);//
        } else if (col.equals("orange") || col.equals("orange-red") || col.equals("red-orange")) {
            return ChatColor.RED.toString();//"\u00A7c"; // String.format("\u00A7%x", 0xC);//
        } else if (col.equals("pink") || col.equals("light red")) {
            return ChatColor.LIGHT_PURPLE.toString();//"\u00A7d"; // String.format("\u00A7%x", 0xD);//
        } else if (col.equals("yellow")) {
            return ChatColor.YELLOW.toString();//"\u00A7e"; // String.format("\u00A7%x", 0xE);//
        } else if (col.equals("white")) {
            return ChatColor.WHITE.toString();//"\u00A7f"; //String.format("\u00A7%x", 0xF);//
        } else {
            return def;
        }
    }
}
