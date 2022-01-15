package org.texastorque.util;

import java.util.ArrayList;

public final class ArrayUtils {

	private ArrayUtils() { } // Only allow static methods.
	
	
	/** Buffers the contents of source into the destination array.
	 * 
	 * This method automatically pads the 
	 * 
	 * @param source The array from which values are copied.
	 * @param destination The array that is modified in-place to contain the source values.
	 * @return True if the source and destination match in size, otherwise false. Useful for debugging.
	 */
	public static boolean bufferAndFill(double[] source, double[] destination) {
		int copyLength = Math.min(source.length, destination.length);
		
		// Put the contents of source into the destination array.
		System.arraycopy(source, 0, destination, 0, copyLength);
		
		// Handle the case when the source is shorter than the destination.
		if (copyLength < destination.length) {  
			double fill = source[copyLength - 1];  // The last value in the source array.
			
			// Replace the zeros at the end of `destination` with the fill value.
			for (int i = source.length; i < destination.length; i++) {
				destination[i] = fill;
			}
		}
		
		return (source.length == destination.length);
	}
	
	public static boolean isSorted(double[] input, boolean ascending) {
		if (input.length == 0) return true;
		
		double last = input[0];
		for (double value : input) {
			// Check if desired sort order matches actual order of current and previous elements.
			boolean isAscending = value >= last;
			if ((!ascending && isAscending) || (ascending && !isAscending)) {
				return false;
			}
			
			last = value;  // Make sure new values are compared.
		}
		
		return true;
	}

	/**
	 * ##########################
	 * # THIS DOES NOT WORK YET #
	 * ##########################
	 * 
	 * Might fix later
	 * 
	 * Copies a range of a static array, similar to sub string.
	 * Oh boy do I love generics!!!
	 * 
	 * Neverminds this doesn't rly work
	 * @param array
	 * //@deprecated
	 */
	/*
	@Deprecated
	public static<T> T[] subArray(T[] array, int start, int end) {
		if (start < 0 || end > array.length || start > end) {
			throw new IllegalArgumentException("Invalid subarray bounds.");
		}
		
		@SuppressWarnings("unchecked")
		T[] subArray = (T[]) new Object[end - start]; // this fails ):
		System.arraycopy(array, start, subArray, 0, end - start);
		return subArray;
	}
	*/

	/**
	 * Copies a static array into an array list.
	 * 
	 * @param array Static array.
	 * @return Generated array list.
	 */
	public static<T> ArrayList<T> staticToList(T[] array) {
		ArrayList<T> list = new ArrayList<T>();
		for (T element : array) list.add(element);
		return list;
	}

	/**
	 * Prints a static array prettierlyist.
	 * 
	 * @param array Static array
	 */
	public static<T> void printArray(T[] array) {
		String output = "[ ";
		for (T value : array) output += value + ", ";
		output = output.substring(0, output.length() - 2) + " ]";
		System.out.println(output);
	}
}
