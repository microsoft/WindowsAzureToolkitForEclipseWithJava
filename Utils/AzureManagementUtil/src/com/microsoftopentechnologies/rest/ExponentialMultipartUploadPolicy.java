/**
* Copyright 2014 Microsoft Open Technologies, Inc.
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
package com.microsoftopentechnologies.rest;

public class ExponentialMultipartUploadPolicy implements MultipartUploadRetryPolicy {

	private MultipartUploadParameters parameters;
	
	public ExponentialMultipartUploadPolicy(int maxThreadpoolSize) {
		parameters = new MultipartUploadParameters(maxThreadpoolSize);
	}
	
	/**
	 * this implementations decreases both parameters at once in an exponential matter
	 */
	@Override
	public MultipartUploadParameters decreaseParameters() {
		int newThreadpoolSize = parameters.getThreadpoolSize() / 2;
		int newChunkSize = parameters.getChunkSize() / 2;
		parameters.setChunkSize(newChunkSize);
		parameters.setThreadpoolSize(newThreadpoolSize);
		return parameters;
	}

	/**
	 * if the current thread pool size is 1, no further decrease is effective
	 */
	@Override
	public boolean canDecreaseParameters() {
	
		if (parameters.getThreadpoolSize() == 1) {
			return false;
		}
		
		return true;
	}

	@Override
	public MultipartUploadParameters getMultipartUploadParameters() {
		return parameters;
	}
}
