package ch.pschatzmann.jflightcontroller4pi.integration;

import java.util.TimerTask;

public class Utils {
	public static TimerTask timerTask(Runnable r) {
		return new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		};
	}

	public static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
		}
		
	}
}
