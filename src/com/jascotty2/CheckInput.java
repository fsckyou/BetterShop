/**
 * Programmer: Jacob Scott
 * Email: jascottytechie at gmail.com
 * Program Name: CheckInput
 * Description: provides checking for parsing numbers from strings
 * Date: Mar 8, 2011
 */
package com.jascotty2;

import java.math.BigInteger;
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
        // pattern may be faster for small strings, but NumberFormatException is more accurate :(
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

    public static long GetLong(String input, long onError) {
        if(input==null)return onError;
        try {
            return Pattern.matches(IntPattern, input) ? Long.parseLong(input) : onError;
        } catch (NumberFormatException e) {
            // just in case the number is too large... can never be too careful..
            return onError;
        }
    }

    public static int GetInt(String input, int onError) {
        if(input==null)return onError;
        try {
            return Pattern.matches(IntPattern, input) ? Integer.parseInt(input) : onError;
        } catch (NumberFormatException e) {
            // just in case the number is too large... can never be too careful..
            return onError;
        }
    }

    public static double GetDouble(String input, double onError) {
        if(input==null)return onError;
        try {
            return Pattern.matches(fpRegex, input) ? Double.parseDouble(input) : onError;
        } catch (NumberFormatException e) {
            return onError;
        }
    }

    public static byte GetByte(String input, byte onError) {
        if(input==null)return onError;
        // not fully sure how to catch a byte with an expression, so checking int instead
        try {
            return Pattern.matches(IntPattern, input) ? Byte.parseByte(input) : onError;
        } catch (NumberFormatException e) {
            return onError;
        }
    }

    
    public static BigInteger GetBigInt(String str, long defaultNum) {
        if(str==null){
            return new BigInteger(String.valueOf(defaultNum));
        }
        try{
            return new BigInteger(str);
        }catch(Exception e){
            return new BigInteger(String.valueOf(defaultNum));
        }
    }
    
    public static BigInteger GetBigInt_TimeSpanInSec(String str) throws Exception {
        return GetBigInt_TimeSpanInSec(str, 's');
    }
    
    public static BigInteger GetBigInt_TimeSpanInSec(String str, char defaultUnit) throws Exception {
        BigInteger ret = new BigInteger("0");
        int charPos = 0;
        for (; charPos < str.length() ; ++charPos) {
            if (!Character.isDigit(str.charAt(charPos))) {
                break;
            }
        }
        //boolean good = false;
        if (charPos > 0) {
            // double-check value
            if (CheckInput.IsInt(str.substring(0, charPos))) {
                ret = new BigInteger(str.substring(0, charPos));
                char unit = str.length()==charPos ? defaultUnit : str.charAt(charPos);
                if(unit == 's'){
                    // do nothing: is already seconds
                    return ret;
                } else if (unit == 'm') {
                    // it's annoying that this class doesn't accept a long..
                    ret=ret.multiply(new BigInteger("60"));
                } else if (unit  == 'h') {
                    ret=ret.multiply(new BigInteger("3600"));
                } else if (unit== 'd') {
                    ret=ret.multiply(new BigInteger("86400"));
                } else if (unit == 'w') {
                    ret=ret.multiply(new BigInteger("604800"));
                } else if (unit == 'M') {
                    // using 1m = 30 days
                    ret=ret.multiply(new BigInteger("18144000"));
                } else {
                    throw new Exception("Unknown TimeSpan unit: "  + str.charAt(charPos));
                }
                return ret;
            }else{
                throw new Exception("Invalid Numerical Value: "  + str);
            }
        }
        // will throw it's own exception
        return new BigInteger(str);
    }
} // end class CheckInput

