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
package com.microsoftopentechnologies.azuremanagementutil.rest;

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
