/*
* Copyright Microsoft Corp.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
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
