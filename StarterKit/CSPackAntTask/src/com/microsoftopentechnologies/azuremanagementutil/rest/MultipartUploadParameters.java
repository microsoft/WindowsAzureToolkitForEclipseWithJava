/**
* Copyright Microsoft Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.microsoftopentechnologies.azuremanagementutil.rest;

public class MultipartUploadParameters {
	
	private static final int MAX_CHUNK_SIZE = 4 * 1048576; //4MB
		
	private int chunkSize;
	private int threadpoolSize;
	
	public MultipartUploadParameters(int maxSize) {
		this.chunkSize = MAX_CHUNK_SIZE;
		this.threadpoolSize = maxSize;
	}
	
	public int getChunkSize() {
		return chunkSize;
	}
	
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}
	
	public int getThreadpoolSize() {
		return threadpoolSize;
	}
	
	public void setThreadpoolSize(int size) {
		this.threadpoolSize = size;
	}	
}
