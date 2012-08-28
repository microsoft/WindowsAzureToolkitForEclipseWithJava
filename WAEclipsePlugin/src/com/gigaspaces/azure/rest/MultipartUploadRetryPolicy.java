package com.gigaspaces.azure.rest;

public interface MultipartUploadRetryPolicy {
	
	/**
	 * this method implements the decrease logic for a specific retry policy
	 */
	public MultipartUploadParameters decreaseParameters();
	
	/**
	 * 
	 * @return - returns whether or not we can decrease the parameters further
	 */
	public boolean canDecreaseParameters();
	
	/**
	 * @return - the current upload parameters set
	 */
	public MultipartUploadParameters getMultipartUploadParameters();

}
