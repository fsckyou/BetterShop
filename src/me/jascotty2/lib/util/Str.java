/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods for manipulating Strings
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Collection;

public class Str extends OutputStream {

	protected StringBuilder text = new StringBuilder();

	@Override
	public void write(int b) throws IOException {
		text.append((char) b);
	}

	// Static Methods
	static NumberFormat nf = NumberFormat.getInstance(),
			cnf = NumberFormat.getCurrencyInstance();
	public static String numFormat(long num){
		return nf.format(num);
	}

	public static String numFormat(double num){
		return nf.format(num);
	}

	public static String currencyFormat(long num){
		return cnf.format(num);
	}

	public static String currencyFormat(double num){
		return cnf.format(num);
	}

	public static String concatStr(String... str) {
		return concatStr(str, 0, "");
	}

	public static String concatStr(String[]... str) {
		StringBuilder ret = new StringBuilder();
		for (String[] s : str) {
			ret.append(concatStr(s));
		}
		return ret.toString();
	}

	public static String concatStr(String[] s, String sep) {
		return concatStr(s, 0, sep);
	}

	public static String concatStr(String[] s, int start) {
		return concatStr(s, start, "");
	}

	public static String concatStr(String[] s, int start, String sep) {
		StringBuilder ret = new StringBuilder();
		if (s != null) {
			for (int i = start; i < s.length; ++i) {
				ret.append(s[i]);
				if (i + 1 < s.length) {
					ret.append(sep);
				}
			}
		}
		return ret.toString();
	}

	public static String concatStr(String[] s, int start, String sep, int length) {
		StringBuilder ret = new StringBuilder();
		if (s != null) {
			for (int i = start, j = 0; i < s.length && j < length; ++i, ++j) {
				ret.append(s[i]);
				if (i + 1 < s.length) {
					ret.append(sep);
				}
			}
		}
		return ret.toString();
	}

	public static String concatStr(Collection<?> strSet) {
		return concatStr(strSet, 0, "");
	}

	public static String concatStr(Collection<?> strSet, String sep) {
		return concatStr(strSet, 0, sep);
	}

	public static String concatStr(Collection<?> strSet, int start) {
		return concatStr(strSet, start, "");
	}

	public static String concatStr(Collection<?> strSet, int start, String sep) {
		if (strSet == null || strSet.isEmpty() || start >= strSet.size()) {
			return "";
		}
		StringBuilder ret = new StringBuilder();
		int i = 0;
		//for(int i=0; i<strSet.size(); ++i){
		for (Object o : strSet) {
			if (++i > start) {
				//ret.append(o.toString());
				//ret.append(String.valueOf(o));
				ret.append(o == null ? "null" : o.toString());
				if (i < strSet.size()) {
					ret.append(sep);
				}
			}
		}
		return ret.toString();
	}

	public static String concatStr(Collection<?> strSet, int start, String sep, int length) {
		if (strSet == null || strSet.isEmpty() || start >= strSet.size() || length <= 0) {
			return "";
		}
		StringBuilder ret = new StringBuilder();
		int i = 0, l = 0;
		//for(int i=0; i<strSet.size(); ++i){
		for (Object o : strSet) {
			if (++i > start) {
				ret.append(o == null ? "null" : o.toString());
				if (++l > length) {
					break;
				} else if (i < strSet.size()) {
					ret.append(sep);
				}
			}
		}
		return ret.toString();
	}

	public static String concatStr(Object[] strSet) {
		return concatStr(strSet, 0, "");
	}

	public static String concatStr(Object[] strSet, String sep) {
		return concatStr(strSet, 0, sep);
	}

	public static String concatStr(Object[] strSet, int start) {
		return concatStr(strSet, start, "");
	}

	public static String concatStr(Object[] strSet, int start, String sep) {
		if (strSet == null || start >= strSet.length) {
			return "";
		}
		StringBuilder ret = new StringBuilder();
		int i = 0;
		//for(int i=0; i<strSet.size(); ++i){
		for (Object o : strSet) {
			if (++i > start) {
				//ret.append(o.toString());
				//ret.append(String.valueOf(o));
				ret.append(o == null ? "null" : o.toString());
				if (i < strSet.length) {
					ret.append(sep);
				}
			}
		}
		return ret.toString();
	}

	public static String concatStr(Object[] strSet, int start, String sep, int length) {
		if (strSet == null || start >= strSet.length || length <= 0) {
			return "";
		}
		StringBuilder ret = new StringBuilder();
		int i = 0, l = 0;
		//for(int i=0; i<strSet.size(); ++i){
		for (Object o : strSet) {
			if (++i > start) {
				ret.append(o == null ? "null" : o.toString());
				if (++l > length) {
					break;
				} else if (i < strSet.length) {
					ret.append(sep);
				}
			}
		}
		return ret.toString();
	}

	public static boolean isIn(String input, String... check) {
		input = input.trim();
		for (String c : check) {
			if (input.equalsIgnoreCase(c.trim())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isIn(String input, String[]... check) {
		input = input.trim();
		for (String[] c : check) {
			for (String c2 : c) {
				if (input.equalsIgnoreCase(c2.trim())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isIn(String input, String check) {
		input = input.trim();
		for (String c : check.split(",")) {
			if (input.equalsIgnoreCase(c.trim())) {
				return true;
			}
		}
		return false;
	}

	public static boolean startIsIn(String input, String check) {
		for (String c : check.split(",")) {
			if (input.length() >= c.length()) {
				if (input.substring(0, c.length()).equalsIgnoreCase(c)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean startIsIn(String input, String[] check) {
		for (String c : check) {
			if (input.length() >= c.length()) {
				if (input.substring(0, c.length()).equalsIgnoreCase(c)) {
					return true;
				}
			}
		}
		return false;
	}

	public static int count(String str, String find) {
		int c = 0;
		for (int i = 0; i < str.length() - find.length(); ++i) {
			if (str.substring(i, i + find.length()).equals(find)) {
				++c;
			}
		}
		return c;
	}

	public static int count(String str, char find) {
		int c = 0;
		for (int i = 0; i < str.length(); ++i) {
			if (str.charAt(i) == find) {
				++c;
			}
		}
		return c;
	}

	public static int countIgnoreCase(String str, String find) {
		int c = 0;
		for (int i = 0; i < str.length() - find.length(); ++i) {
			if (str.substring(i, i + find.length()).equalsIgnoreCase(find)) {
				++c;
			}
		}
		return c;
	}

	public static int indexOf(String array[], String search) {
		if (array != null && array.length > 0) {
			for (int i = array.length - 1; i >= 0; --i) {
				if (array[i].equals(search)) {
					return i;
				}
			}
		}
		return -1;
	}

	public static int indexOfIgnoreCase(String array[], String search) {
		for (int i = array.length - 1; i >= 0; --i) {
			if (array[i].equalsIgnoreCase(search)) {
				return i;
			}
		}
		return -1;
	}

	public static String getStackStr(Exception err) {
		if (err == null) {// || err.getCause() == null) {
			return "";
		}
		Str stackoutstream = new Str();
		PrintWriter stackstream = new PrintWriter(stackoutstream);
		err.printStackTrace(stackstream);
		stackstream.flush();
		stackstream.close();
		return stackoutstream.text.toString();
	}

	public static String getStackStr(Throwable err) {
		if (err == null) {
			return "";
		}
		Str stackoutstream = new Str();
		PrintWriter stackstream = new PrintWriter(stackoutstream);
		err.printStackTrace(stackstream);
		stackstream.flush();
		stackstream.close();
		return stackoutstream.text.toString();
	}

	/**
	 * pads str on the right (space-padded) (left-align)
	 * @param str
	 * @param len
	 * @return
	 */
	public static String padRight(String str, int len) {
		return padRight(str, len, ' ');
	}

	/**
	 * pads str on the right with pad (left-align)
	 * @param str
	 * @param len
	 * @param pad
	 * @return
	 */
	public static String padRight(String str, int len, char pad) {
		StringBuilder ret = new StringBuilder(str);
		for (int i = str.length(); i < len; ++i) {
			ret.append(pad);
		}
		return ret.toString();
	}

	/**
	 * pads str on the left (space-padded) (right-align)
	 * @param str
	 * @param len
	 * @return
	 */
	public static String padLeft(String str, int len) {
		return repeat(' ', len - str.length()) + str;
	}

	/**
	 * pads str on the left with pad (right-align)
	 * @param str
	 * @param len
	 * @param pad
	 * @return
	 */
	public static String padLeft(String str, int len, char pad) {
		return repeat(pad, len - str.length()) + str;
	}

	/**
	 * pads str on the left & right (space-padded) (center-align)
	 * @param str
	 * @param len
	 * @return
	 */
	public static String padCenter(String str, int len) {
		len -= str.length();
		int prepad = len / 2;
		return repeat(' ', prepad) + str + repeat(' ', len - prepad);
	}

	/**
	 * pads str on the left & right with pad (center-align)
	 * @param str
	 * @param len
	 * @param pad
	 * @return
	 */
	public static String padCenter(String str, int len, char pad) {
		len -= str.length();
		int prepad = len / 2;
		return repeat(pad, prepad) + str + repeat(pad, len - prepad);
	}

	public static String strWordWrap(String str, int width) {
		return strWordWrap(str, width, 0, ' ');
	}

	public static String strWordWrap(String str, int width, int tab) {
		return strWordWrap(str, width, tab, ' ');
	}

	public static String strWordWrap(String str, int width, int tab, char tabChar) {
		StringBuilder ret = new StringBuilder();
		while (str.length() > 0) {
			// find last char of first line
			if (str.length() <= width) {
				return (ret.length() > 0 ? ret + "\n" + Str.repeat(tabChar, tab) : "").concat(str);
			}
			String line1 = strTrim(str, width);
			int lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
			while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
				--lastPos;
			}
			if (lastPos == 0) {
				lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
			}
			//ret += strPadRightChat((ret.length() > 0 ? unformattedStrRepeat(tabChar, tab) : "") + str.substring(0, lastPos));
			ret.append(ret.length() > 0 ? "\n" + Str.repeat(tabChar, tab) : "").append(str.substring(0, lastPos));
			str = str.substring(lastPos + 1);
		}
		return ret.toString();
	}

	/**
	 * right-aligns paragraphs
	 * @param str
	 * @param width
	 * @param tab
	 * @param tabChar
	 * @return
	 */
	public static String strWordWrapRight(String str, int width, int tab, char tabChar) {
		StringBuilder ret = new StringBuilder();
		while (str.length() > 0) {
			// find last char of first line
			if (str.length() <= width) {
				return (ret.length() > 0 ? ret + "\n" : "").concat(Str.padLeft(str, width, tabChar));
			}
			String line1 = strTrim(str, width);
			int lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
			while (lastPos > 0 && line1.charAt(lastPos) != ' ') {
				--lastPos;
			}
			if (lastPos <= 0) {
				lastPos = line1.length() - (ret.length() > 0 && line1.length() > tab + 1 ? tab + 1 : 1);
			}
			//ret += strPadLeftChat(str.substring(0, lastPos), tabChar);
			ret.append(ret.length() > 0 ? "\n" : "").append(Str.padLeft(str.substring(0, lastPos), width, tabChar));
			str = str.substring(lastPos + 1);
		}
		return ret.toString();
	}

	public static String strWordWrapRight(String str, int width, int tab) {
		return strWordWrapRight(str, width, tab, ' ');
	}

	public static String repeat(char ch, int len) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < len; ++i) {
			ret.append(ch);
		}
		return ret.toString();
	}

	/**
	 * Returns a sequence str of the provided str count # of times
	 * @param str
	 * @param count
	 * @return
	 */
	public static String repeat(String str, int count) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < count; ++i) {
			ret.append(str);
		}
		return ret.toString();
	}

	public static String strTrim(String str, int length) {
		if (str.length() > length) {
			int width = length;
			String ret = "";
			boolean lastCol = false;
			for (char c : str.toCharArray()) {
				if (c == '\u00A7') {
					ret += c;
					lastCol = true;
				} else {
					if (!lastCol) {
						if (width - 1 >= 0) {
							width -= 1;
							ret += c;
						} else {
							return ret;
						}
					} else {
						ret += c;
						lastCol = false;
					}
				}
			}
		}
		return str;
	}

	public static String titleCase(String str) {
		StringBuilder ret = new StringBuilder();
		boolean st = true;
		for (char c : str.toLowerCase().toCharArray()) {
			if (st) {
				ret.append(Character.toTitleCase(c));
			} else {
				ret.append(c);
			}
			st = c == ' ';
		}
		return ret.toString();
	}

	/**
	 * <p>Find the Levenshtein distance between two Strings.</p>
	 *
	 * <p>This is the number of changes needed to change one String into
	 * another, where each change is a single character modification (deletion,
	 * insertion or substitution).</p>
	 *
	 * <p>This Method was written by Chas Emerick, and which avoids an OutOfMemoryError
	 * which can occur when some Java implementations are used with very large strings.<br>
	 * This implementation of the Levenshtein distance algorithm
	 * is from <a href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.com/ldjava.htm</a></p>
	 *
	 * <pre>
	 * StringUtil.getLevenshteinDistance("","")               = 0
	 * StringUtil.getLevenshteinDistance("","a")              = 1
	 * StringUtil.getLevenshteinDistance("aaapppp", "")       = 7
	 * StringUtil.getLevenshteinDistance("frog", "fog")       = 1
	 * StringUtil.getLevenshteinDistance("fly", "ant")        = 3
	 * StringUtil.getLevenshteinDistance("elephant", "hippo") = 7
	 * StringUtil.getLevenshteinDistance("hippo", "elephant") = 7
	 * StringUtil.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
	 * StringUtil.getLevenshteinDistance("hello", "hallo")    = 1
	 * </pre>
	 *
	 * @param s  the first String, must not be null
	 * @param t  the second String, must not be null
	 * @return result distance
	 * @throws IllegalArgumentException if either String input <code>null</code>
	 */
	public static int getLevenshteinDistance(String s, String t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		The difference between this impl. and the previous is that, rather
		than creating and retaining a matrix of size s.length()+1 by t.length()+1,
		we maintain two single-dimensional arrays of length s.length()+1.  The first, d,
		is the 'current working' distance array that maintains the newest distance cost
		counts as we iterate through the characters of String s.  Each time we increment
		the index of String t we are comparing, d is copied to p, the second int[].  Doing so
		allows us to retain the previous cost counts as required by the algorithm (taking
		the minimum of the cost count to the left, up one, and diagonally up and to the left
		of the current cost count being calculated).  (Note that the arrays aren't really
		copied anymore, just switched...this is clearly much better than cloning an array
		or doing a System.arraycopy() each time  through the outer loop.)

		Effectively, the difference between the two implementations is this one does not
		cause an out of memory condition when calculating the LD over two very large strings.
		 */

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		int p[] = new int[n + 1]; //'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; //placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}
} // end class Str

