package org.texastorque.torquelib.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Parameters {

	/**
	 * Constants loaded from params file.
	 */
	public static ArrayList<Constant> constants = new ArrayList<>();

	private static File paramsFile;

	public static void makeFile() {
		paramsFile = new File("/home/admin/params.txt");
		try {
			paramsFile.createNewFile();
		} catch (IOException e) {
			SmartDashboard.putString("e", e.getMessage());
		}
	}

	/**
	 * Load the parameters file using this syntax:
	 * 
	 *
	 * nameOfParameter valueOfParameter<br>
	 * shooterMotorLeft 2
	 * 
	 *
	 * Constants listed in the file override hardcoded constants.
	 */
	public static void load() {
		makeFile();
		try (BufferedReader br = new BufferedReader(new FileReader(paramsFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				int pos = line.indexOf(" ");
				if (pos != -1) {
					for (Constant c : constants) {
						if (c.getKey().equals(line.substring(0, pos))) {
							c.value = Double.parseDouble(line.substring(pos));
						}
					}
				} else {
					System.out.println("Invalid line");
				}
			}
		} catch (Exception e) {
			System.out.println("Messed up reading constants");
		}
	}

	public static class Constant {

		private final String key;
		private volatile double value;

		/**
		 * Make a final Constant.
		 *
		 * @param key
		 *            Name of value.
		 * @param value
		 *            Value.
		 */
		public Constant(String key, double value) {
			this.key = key;
			this.value = value;

			constants.add(this);
		}

		public String getKey() {
			return key;
		}

		public Double getDouble() {
			return value;
		}

		public boolean getBoolean() {
			return value == 1;
		}

		public void override(double _value) {
			value = _value;
		}

		@Override
		public String toString() {
			return key + ": " + value;
		}
	}
}
