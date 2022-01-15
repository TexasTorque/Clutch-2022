package org.texastorque.torquelib.torquelog;

public class LogData {

	public enum Priority {
		// Keep in ascending order of priority
		DEFAULT(250), NORMAL(100), HIGH(25);

		public final int CYCLEHZ;

		Priority(int cycleHz) {
			this.CYCLEHZ = cycleHz;
		}

	}

	public final String KEY;
	public final Priority P;

	public LogData(String key) {
		this.KEY = key;
		P = Priority.DEFAULT;
	}

	public LogData(String key, Priority p) {
		this.KEY = key;
		this.P = p;
	}
	
}
