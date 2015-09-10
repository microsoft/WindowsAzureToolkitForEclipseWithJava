/**
 * Copyright (c) Microsoft Corporation
 * 
 * All rights reserved. 
 * 
 * MIT License
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files 
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, 
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR 
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH 
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
