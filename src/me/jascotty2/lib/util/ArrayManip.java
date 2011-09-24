/**
 * Copyright (C) 2011 Jacob Scott <jascottytechie@gmail.com>
 * Description: methods fro manipulating & outputting arrays
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
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author jacob
 */
public class ArrayManip {

	public static void printArray(OutputStream out, int array[], int cols) throws IOException {
		//try {
		int maxLen = 1;
		for (int i = 0; i < array.length; ++i) {
			if (String.valueOf(array[i]).length() > maxLen) {
				maxLen = String.valueOf(array[i]).length();
			}
		}

		for (int i = 0; i < array.length; ++i) {
			out.write(Str.padLeft(String.valueOf(array[i]), maxLen + 1).getBytes());
			if (i % cols == cols - 1) {
				out.write("\n".getBytes());
			}
		}
		if ((array.length - 1) % cols != cols - 1) {
			out.write("\n".getBytes());
		}
		//} catch (IOException ex) {
		//    Logger.getAnonymousLogger().log(Level.SEVERE, ex.getMessage(), ex);
		//}
	}

	public static void printArray(OutputStream out, int array[]) throws IOException {
		for (int i = 0; i < array.length; ++i) {
			out.write(String.valueOf(array[i]).getBytes());
		}
	}

	public static void selectionSort(int array[], boolean ascending) {
		for (int i = 0; i < array.length - 1; ++i) {
			int swapI = i;
			for (int j = i + 1; j < array.length; ++j) {
				if ((ascending && array[j] < array[swapI])
						|| (!ascending && array[j] > array[swapI])) {
					swapI = j;
				}
			}
			if (swapI != i) {
				int t = array[i];
				array[i] = array[swapI];
				array[swapI] = t;
			}
		}
	}

	public static void quickSort(int array[], boolean ascending) {
		Arrays.sort(array, 0, array.length);
		if (!ascending) {
			reverse(array);
		}
	}

	public static void reverse(int array[]) {
		for (int i = array.length / 2; i >= 0; --i) {
			int t = array[i];
			array[i] = array[array.length - i - 1];
			array[array.length - i - 1] = t;
		}
	}

	public static void swapElem(int array[], int a, int b) {
		if (a > 0 && b > 0 && a < array.length && b < array.length) {
			int t = array[a];
			array[a] = array[b];
			array[b] = t;
		}
	}

	public static int[] arrayConcat(int arr1[], int arr2[]) {
		if (arr1 == null || arr1.length == 0) {
			return arr2 == null ? new int[0] : arr2;
		} else if (arr2 == null || arr2.length == 0) {
			return arr1 == null ? new int[0] : arr1;
		}
		int i = 0;
		int ret[] = new int[arr1.length + arr2.length];
		for (; i < arr1.length; ++i) {
			ret[i] = arr1[i];
		}
		for (int n = 0; n < arr2.length; ++i, ++n) {
			ret[i] = arr2[n];
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] arrayConcat(T arr1[], T arr2[]) {
		if (arr1 == null && arr2 == null){
			return null;
		} else if(arr1 == null || arr1.length == 0){
			return arr2;
		} else if(arr2 == null || arr2.length == 0){
			return arr1;
		}
//		Object ret[] = new Object[arr1.length + arr2.length];
//		System.arraycopy(arr1, 0, ret, 0, arr1.length);
//		System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
//		return (T[]) ret;
		T[] ret = (T[]) Array.newInstance(arr1.getClass().getComponentType(), arr1.length + arr2.length);
		System.arraycopy(arr1, 0, ret, 0, arr1.length);
		System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
		return ret;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] arraySub(T arr[], int startIndex, int endIndex) {
		if(arr == null){
			return null;
		} else if(startIndex >= arr.length){
			throw new IllegalArgumentException("startIndex is greater than the array");
		} else if(endIndex > arr.length){
			throw new IllegalArgumentException("endIndex is greater than the array");
		} else if(startIndex < 0){
			throw new IllegalArgumentException("startIndex cannot be negative");
		} else if(endIndex < startIndex){
			throw new IllegalArgumentException("startIndex is greater than the endIndex");
		}
		T[] ret = (T[]) Array.newInstance(arr.getClass().getComponentType(), endIndex - startIndex);
		System.arraycopy(arr, startIndex, ret, 0, ret.length);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public synchronized static <T> int indexOf(T array[], T search) {
		if (array == null || array.length == 0) {
			return -1;
		}

		if (search == null) {
			for (int i = 0; i < array.length; ++i) {
				if (array[i] == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < array.length; ++i) {
				if (array[i] != null && search.equals(array[i])) {
					return i;
				}
			}
		}
		return -1;
	}
} // end class ArrayManip

