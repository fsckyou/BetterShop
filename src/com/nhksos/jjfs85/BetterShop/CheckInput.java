/**
 * Programmer: Jacob Scott
 * Email: jascottytechie at gmail.com
 * Program Name: CheckInput
 * Description: provides checking for parsing numbers from strings
 * Date: Mar 8, 2011
 */
package com.nhksos.jjfs85.BetterShop;

import java.util.regex.Pattern;

/**
 * @author jacob
 */
public class CheckInput {

    // double string checking from Double documentation
    final static String Digits = "(\\p{Digit}+)";
    final static String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final static String Exp = "[eE][+-]?" + Digits;
    final static String fpRegex =
            ("[\\x00-\\x20]*" + // Optional leading "whitespace"
            "[+-]?(" + // Optional sign character
            "NaN|" + // "NaN" string
            "Infinity|"
            + // "Infinity" string
            // A decimal floating-point string representing a finite positive
            // number without a leading sign has at most five basic pieces:
            // Digits . Digits ExponentPart FloatTypeSuffix
            //
            // Since this method allows integer-only strings as input
            // in addition to strings of floating-point literals, the
            // two sub-patterns below are simplifications of the grammar
            // productions from the Java Language Specification, 2nd
            // edition, section 3.10.2.
            // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
            "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|"
            + // . Digits ExponentPart_opt FloatTypeSuffix_opt
            "(\\.(" + Digits + ")(" + Exp + ")?)|"
            + // Hexadecimal strings
            "(("
            + // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "(\\.)?)|"
            + // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
            "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")"
            + ")[pP][+-]?" + Digits + "))"
            + "[fFdD]?))"
            + "[\\x00-\\x20]*");// Optional trailing "whitespace"
    // int
    final static String IntPattern = "[+-]?" + Digits; // or: "^-?\\d+$"

    public static boolean IsInt(String input) {
        //return Pattern.matches(IntPattern, input);
        // pattern faster for small strings, but NumberFormatException is more accurate :(
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean IsByte(String input) {
        try {
            Byte.parseByte(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean IsDouble(String input) {
        return Pattern.matches(fpRegex, input);
    }

    public static int GetInt(String input, int onError) {
        try {
            return Pattern.matches(IntPattern, input) ? Integer.parseInt(input) : onError;
        } catch (NumberFormatException e) {
            // just in case the number is too large... can never be too careful..
            return onError;
        }
    }

    public static double GetDouble(String input, double onError) {
        try {
            return Pattern.matches(fpRegex, input) ? Double.parseDouble(input) : onError;
        } catch (NumberFormatException e) {
            return onError;
        }
    }

    public static byte GetByte(String input, byte onError) {
        // not fully sure how to catch a byte with an expression, so checking int instead
        try {
            return Pattern.matches(IntPattern, input) ? Byte.parseByte(input) : onError;
        } catch (NumberFormatException e) {
            return onError;
        }
    }
} // end class CheckInput

