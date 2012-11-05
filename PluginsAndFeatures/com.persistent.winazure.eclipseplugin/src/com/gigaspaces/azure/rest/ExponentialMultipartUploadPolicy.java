package com.gigaspaces.azure.rest;

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
