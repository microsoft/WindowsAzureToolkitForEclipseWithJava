package com.gigaspaces.azure.rest;

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
