/*
 Copyright 2013 Microsoft Open Technologies, Inc.
 
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WindowsAzureManager {
	public static class Commands {
		private static abstract class Command {
			protected String commandLine;
			protected boolean expectResponse;
			protected int progressInterval;
			protected String progressText;
		}
		public static class Container {
			public static class Create extends Command {
				public final String commandlineTemplate = "container create \"$containerName\" \"$storageName\" \"$accessKey\" \"$baseURL\"";
				public Create(String containerName, String storageName, String accessKey, String baseURL) {
					expectResponse = true;
					progressInterval = 0;
					commandLine = commandlineTemplate
							.replace("$containerName", containerName)
							.replace("$storageName", storageName)
							.replace("$accessKey", accessKey)
							.replace("$baseURL", baseURL);
				}
			}
		}
		
		public static class Blob {
			public static class Use extends Command {
				public final String commandlineTemplate = "blob use \"$blobName\" \"$containerName\" \"$storageName\" \"$accessKey\" \"$baseURL\"";
				public Use(String blobName, String containerName, String storageName, String accessKey, String baseURL) {
					expectResponse = true;
					progressInterval = 0;
					commandLine = commandlineTemplate
							.replace("$blobName", blobName)
							.replace("$containerName", containerName)
							.replace("$storageName", storageName)
							.replace("$accessKey", accessKey)
							.replace("$baseURL", baseURL);
				}
			}
			
			public static class Upload extends Command {
				public final String commandlineTemplate = "blob upload \"$filePath\" \"$blobName\" \"$containerName\" \"$storageName\" \"$accessKey\" \"$baseURL\"";
				public Upload(String filePath, String blobName, String containerName, String storageName, String accessKey, String baseURL) {
					expectResponse = true;
					progressInterval = 5000;
					progressText = "still uploading";
					commandLine = commandlineTemplate
							.replace("$filePath", filePath)
							.replace("$blobName", blobName)
							.replace("$containerName", containerName)
							.replace("$storageName", storageName)
							.replace("$accessKey", accessKey)
							.replace("$baseURL", baseURL);
				}
			}
		}
	}
	
	private Process process;
	private OutputStream outStream;
	private InputStream inStream;
	private InputStream errStream;
	
	public String execute(Commands.Command cmd)  {
		clear();
		if(cmd == null) {
			return null;
		} 

		// Start showing progress bar if needed
		ProgressBar progressBar = null;
		Thread progressBarThread = null;
		if(cmd.progressInterval > 0) {
			progressBar = new ProgressBar(cmd.progressInterval, cmd.progressText);
			progressBarThread = new Thread(progressBar);
			progressBarThread.start();
		}
		
		String response;
		if(!cmd.expectResponse) {
			response = writeInput(cmd.commandLine);
		} else {
			response = writeInputExpectResponse(cmd.commandLine, false);
		}
		
		// Stop the progress bar
		if(progressBarThread != null) {
			progressBarThread.interrupt();
			try {
				progressBarThread.join();
			} catch (InterruptedException e) {
				;
			}
		}
		
		return response;
	}
	
	/**
	 * Starts the utility command line process
	 * @param utilPath
	 * @return
	 */
	public Process start(File utilPath) {
				
		try {
			if(process != null) {
				stop();
			}
			
			process = new ProcessBuilder(utilPath.getPath()).start();
			outStream = process.getOutputStream();
			inStream = process.getInputStream();
			errStream = process.getErrorStream();
			
			// Expect initial prompt
			if(null == readOutput()) {
				stop();
				return null;
			} else {
				return process;
			}
		} catch (IOException e) {
			return process = null;
		}
	}

	
	/** Stops the utility process and cleans up the pipeline streams
	 * @param inStream
	 * @param outStream
	 * @param errStream
	 * @param process
	 */
	public void stop() {
		try {
			if(inStream != null) {
				inStream.close();
			}
			if(outStream !=null) {
				outStream.close();
			}
			if(errStream !=null) {
				errStream.close();
			}
		} catch(IOException e) {
			;
		}
		
		if(process != null) {
			process.destroy();
			process = null;
		}
	}

	/**
	 * Clears the streams
	 */
	private void clear() {
		byte[] content = new byte[2048];
		try {
			while(inStream != null && inStream.available() > 0) {
				inStream.read(content);
			}
			
			while(errStream != null && errStream.available() > 0) {
				errStream.read(content);
			}
		} catch (IOException e) {
			;
		}
	}
	

	/**
	 * Waits for content from the process'es stdout, unless there is content on stderr
	 * @return stdout content
	 */
	private String readOutput() {
		return expectStreamResponse(inStream, errStream, inStream);
	}
	
	/**
	 * Waits for content from stderr, unless there is content on stdout
	 * @return stderr content
	 */
	private String readError() {
		return expectStreamResponse(errStream, errStream, inStream);
	}
	
	/**
	 * Sends content to the process'es stdin and expects a response on either stderr or stdout
	 * @param text
	 * @param expectErrorResponse If true, expect a response on stderr, else stdout
	 * @return null if any error or the response if not on the expected stream
	 */
	private String writeInputExpectResponse(String text, boolean expectErrorResponse) {
		if(writeInput(text) == null) {
			return null;
		}
		if(expectErrorResponse) {
			return readError();
		} else {
			return readOutput();
		}
	}
	
	/**
	 * Sends content to the process's stdin
	 * @param text Content to send
	 * @return Returns null if failed to send the content, else the content sent
	 */
	private String writeInput(String text) {
		if(text == null) {
			return null;
		} else if(!text.endsWith("\n")) {
			text += "\n";
		}
		
		try {
			outStream.write(text.getBytes());
			outStream.flush();
			return text;
		} catch (IOException e) {
			return null;
		}		
	}
	
	/**
	 * Returns the text content from the expected stream if the content is received on that stream, else null
	 * @param expectedStream Stream on which content is expected
	 * @param stream1 The stream to check first
	 * @param stream2 The stream to check second
	 * @return Text content from the expected stream, if that's the stream where the content was detected first
	 */
	private static String expectStreamResponse(InputStream expectedStream, InputStream stream1, InputStream stream2) {
		final byte[] responseBytes1 = new byte[2048];
		final byte[] responseBytes2 = new byte[2048];
		byte[] responseBytes = null;
		InputStream respondingStream = null;
				
		try {
			while(respondingStream == null) {
				if(stream1.available() > 0) {
					stream1.read(responseBytes1);
					respondingStream = stream1;
					responseBytes = responseBytes1;
				}

				if(stream2.available() > 0) {
					stream2.read(responseBytes2);
					if(respondingStream == null) {
						respondingStream = stream2;
						responseBytes = responseBytes2;
					}
				}

				try {
					if(respondingStream == null) {
						Thread.sleep(200);
					}
				} catch (InterruptedException e) {
					;
				}
			}
		} catch (IOException e) {
			return null;
		}

		if(expectedStream != respondingStream) {
			return null; // Swallow response from the unexpected stream
		} else {
			return new String(responseBytes);
		}		
	}
	
	
	/**
	 * Class to implement showing progress dots during long running tasks
	 */
	private class ProgressBar implements Runnable {
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
}
