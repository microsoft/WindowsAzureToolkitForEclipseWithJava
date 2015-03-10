package com.microsoftopentechnologies.windowsazure.tools.build;
/**
 * Class to implement showing progress dots during long running tasks
 */
public class ProgressBar implements Runnable {
	private final int intervalMilliseconds;
	private final String text;
	public ProgressBar(int intervalMilliseconds, String text) {
		this.intervalMilliseconds = intervalMilliseconds;
		this.text = text;
	}

	public void run() {
		final long startMiliseconds = System.currentTimeMillis();
		while(true) {
			try {
				Thread.sleep(intervalMilliseconds);
			} catch (InterruptedException e) {
				break;
			}

			System.out.println("..." + text + " (elapsed time: " + String.valueOf((System.currentTimeMillis() - startMiliseconds)/1000) + " sec.)...");
		}
	}
}
