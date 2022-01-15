package org.texastorque.util;

public class StringUtils {
	
	private StringUtils() { }
	
	public static String reverse(String str) {
		String reverse = "";
		while (str.length() > 0) {
			reverse = str.substring(0, 1) + reverse;
			str = str.substring(1);
		}
		return reverse;
	}

}
