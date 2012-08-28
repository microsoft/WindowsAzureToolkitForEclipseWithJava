/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.gigaspaces.azure.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import waeclipseplugin.Activator;

import com.gigaspaces.azure.deploy.Notifier;
import com.gigaspaces.azure.model.Base64Persistent;
import com.gigaspaces.azure.model.BlockList;
import com.gigaspaces.azure.model.EnumerationResults;
import com.gigaspaces.azure.model.Response;
import com.gigaspaces.azure.util.CommandLineException;

public class WindowsAzureStorageServices extends WindowsAzureServiceImpl {

	private static final int MAX_SIZE = 64 * 1048576; //64MB
	private static final int CHUNK_SIZE = 4 * 1048576; //4MB
	private static int CHUNK_COUNT;

	private class UploadingTask implements Callable<String> {

		private final String blockId;
		private final String container;
		private final String blobName;
		private final byte[] chunck;
		private final HashMap<String, String> headers;
		private final String[] blocks;
		private final int index;
		private Notifier notifier;

		@SuppressWarnings("unchecked")
		public UploadingTask(String blockId, String container, String blobName, byte[] chunck, Object headers, String[] blockList, int index, Notifier notifier) {
			this.blockId = blockId;
			this.container = container;
			this.blobName = blobName;
			this.chunck = chunck;
			this.headers = (HashMap<String, String>) headers;
			this.blocks = blockList;
			this.index = index;
			this.notifier = notifier;
		}

		@Override
		public String call() throws Exception {
			Response<?> response = putBlock(container, blobName, blockId,chunck, headers);

			validateResponse(response);

			synchronized (blocks) {
				blocks[index] = decodeBlockId(blockId);
			}

			if (notifier != null) {
				double step = (1 / (double) CHUNK_COUNT) * 100;
				notifier.notifyProgress((int)step);
			}
			return blockId;
		}

	}

	private String storageAccount;
	private String storageKey;
	private final static int NTHREAD = 4;

	public WindowsAzureStorageServices(String storageAccount, String storageKey) throws NoSuchAlgorithmException, InvalidKeyException {
		super();
		if (storageAccount == null || storageAccount.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidStorageAccount);
		this.storageAccount = storageAccount;
		if (storageKey == null || storageKey.isEmpty())
			throw new InvalidRestAPIArgument(Messages.invalidStorageKey);
		this.storageKey = storageKey;
	}

	public EnumerationResults listContainers() throws InvalidKeyException, RestAPIException, InterruptedException, CommandLineException {
		String url = Storage_Service_URL.replace(storage_account,storageAccount).concat(List_Containers);

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, Messages.x_msVersion);

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.GET, url,storageKey, headers, null);

		Response<?> response = (Response<?>) deserialize(result);

		validateResponse(response);

		return (EnumerationResults) response.getBody();
	}

	public void putBlob(String container, String blobName, File file, Notifier notifier) throws URISyntaxException, RestAPIException, InterruptedException, CommandLineException, ExecutionException, IOException {

		String url = Storage_Service_URL.replace(storage_account, storageAccount).concat(Put_Blob).replace(storage_container, container).concat("/" + blobName);

		int fileLength = (int) file.length();

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put(X_MS_VERSION, Messages.x_msVersion2);
		headers.put(CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
		if (fileLength > MAX_SIZE) {
			
			MultipartUploadRetryPolicy retryPolicy = new ExponentialMultipartUploadPolicy(NTHREAD);
			MultipartUploadParameters uploadParameters = retryPolicy.getMultipartUploadParameters();
			
			do {
				try {
					int threadpoolSize = uploadParameters.getThreadpoolSize();
					int chunkSize = uploadParameters.getChunkSize();
					putBlobAsBlockList(container, blobName, url, storageKey, headers, file,chunkSize, threadpoolSize, notifier);
					return;
				} catch (final Exception e) {
					Activator.getDefault().log(Messages.error, e);
					uploadParameters = retryPolicy.decreaseParameters();
					continue;
				}				
			}
			while (retryPolicy.canDecreaseParameters());
			throw new CommandLineException("Failed uploading deployment package (.cspkg).");
			
		} 
		else {
			putBlobAsSingleBlock(url, storageKey, headers, file);
		}
	}

	private void putBlobAsSingleBlock(String url, String storageKey, HashMap<String, String> headers, File cspkgFile) throws InterruptedException, CommandLineException, RestAPIException, IOException {

		FileInputStream stream = null;
		int fileLength = (int) cspkgFile.length();

		headers.put("x-ms-blob-type", "BlockBlob");
		headers.put(CONTENT_LENGTH, "" + fileLength);

		byte[] cspkg = new byte[fileLength];

		try {
			stream = new FileInputStream(cspkgFile);
			readToByteArray(stream, cspkg);
		} finally {
			if (stream != null) {
				stream.close();
			}
		}

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.PUT,url, storageKey, headers, cspkg);

		Response<?> response = (Response<?>) deserialize(result);

		validateResponse(response);		
	}

	private void readToByteArray(InputStream stream, byte[] chunk) throws IOException {

		long length = chunk.length;

		if (length > Integer.MAX_VALUE) {
			throw new IOException("File is too large!");
		}

		int offset = 0;
		int numRead = 0;

		while (offset < chunk.length && (numRead = stream.read(chunk, offset, chunk.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < chunk.length) {
			throw new IOException("Failed to read into byte array");
		}
	}

	private void putBlobAsBlockList(String container, String blobName, String url, String storageKey, HashMap<String, String> headers, File cspkgFile, int chunkSize, int threadpoolSize ,Notifier notifier) throws InterruptedException, CommandLineException, RestAPIException, ExecutionException ,IOException{

		FileInputStream stream = null;

		try {
			int fileLength = (int) cspkgFile.length();

			CHUNK_COUNT = fileLength / chunkSize;

			ExecutorService executorService = Executors.newFixedThreadPool(threadpoolSize);

			String blockId = "";

			BlockList blockList = new BlockList();


			stream = new FileInputStream(cspkgFile);

			int i = 0;

			List<Future<String>> result = new ArrayList<Future<String>>();

			String[] blocks = new String[CHUNK_COUNT];

			for (; i < CHUNK_COUNT; i++) {

				if (i % NTHREAD == 0 && i > 0) {
					join(result);

					result.clear();
				}

				blockId = encodeBlockId(String.format("b_%05d", i));

				byte[] chunck = new byte[CHUNK_SIZE];

				try {
					readToByteArray(stream, chunck);
				} 
				catch (IOException e) {
					Activator.getDefault().log(Messages.error, e);
					throw e;
				}

				Future<String> future = executorService.submit(new UploadingTask(blockId, container, blobName,chunck, headers.clone(), blocks, i, notifier));

				result.add(future);
			}

			join(result);

			stream = new FileInputStream(cspkgFile);

			checkUploadCompletion(stream, blocks, result, CHUNK_SIZE ,executorService, container, blobName, headers);

			blockList.getLatest().addAll(Arrays.asList(blocks));

			int remainderSize = fileLength % CHUNK_SIZE;

			if (remainderSize != 0) {
				blockId = encodeBlockId(String.format("b_%05d", i));

				byte[] chunck = new byte[remainderSize];

				try {
					readToByteArray(stream, chunck);
				} catch (IOException e) {
				}

				Response<?> response = putBlock(container, blobName, blockId, chunck,headers);
				if (notifier != null) {
					int progress = 100 - notifier.getPercent();
					notifier.notifyProgress(progress);
				}

				if (response.getStatus() == 201)
					blockList.getLatest().add(decodeBlockId(blockId));
				else
					blockList.getUncommitted().add(decodeBlockId(blockId));
			}

			putBlockList(container, blobName, blockList);
		}
		finally {
			if (stream != null) {
				stream.close();
			}
		}

	}


	public Response<?> putBlockList(String container, String blobName,
			BlockList body) throws RestAPIException, InterruptedException, CommandLineException {

		String url = Storage_Service_URL
				.replace(storage_account, storageAccount).concat(Put_Blob)
				.replace(storage_container, container).concat("/" + blobName)
				.concat(Put_Block_List);

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, Messages.x_msVersion3);

		headers.put(CONTENT_TYPE, MediaType.APPLICATION_XML);

		byte[] buff = null;

		if (body != null) {
			buff = addContentLength(headers, body);
		}

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.PUT, url,storageKey, headers, buff);

		Response<?> response = (Response<?>) deserialize(result);

		validateResponse(response);

		return response;
	}

	public String createContainer(String container) throws RestAPIException, InterruptedException, CommandLineException {

		String url = Storage_Service_URL.replace(storage_account,
				storageAccount).concat(
						container.toLowerCase() + "?restype=container");

		HashMap<String, String> headers = new HashMap<String, String>();

		headers.put(X_MS_VERSION, "2011-08-18");

		headers.put("x-ms-meta-Name", container);

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.PUT, url,
				storageKey, headers, null);

		Response<?> response = (Response<?>) deserialize(result);

		return getXRequestId(response);
	}

	private Response<?> putBlock(String container, String blobName,
			String blockId, byte[] chunck, HashMap<String, String> headers) throws InterruptedException, CommandLineException {

		String url = Storage_Service_URL
				.replace(storage_account, storageAccount).concat(Put_Blob)
				.replace(storage_container, container).concat("/" + blobName)
				.concat(Put_Block).replace(block_id, blockId);

		headers.put(CONTENT_LENGTH, "" + chunck.length);

		String result = WindowsAzureRestUtils.getInstance().runStorage(HttpVerb.PUT, url,
				storageKey, headers, chunck);

		Response<?> response = (Response<?>) deserialize(result);

		return response;
	}

	private String encodeBlockId(String blockId) {
		String encode = "";

		try {
			encode = URLEncoder.encode(Base64Persistent.encode(blockId.getBytes("UTF-8")),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			Activator.getDefault().log(Messages.error, e);
		}
		return encode;
	}

	private String decodeBlockId(String blockId) {
		String decode = "";

		try {
			decode = URLDecoder.decode(blockId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Activator.getDefault().log(Messages.error, e);
		}

		return decode;
	}

	private void join(List<Future<String>> result) throws InterruptedException, ExecutionException {

		for (int j = 0; j < result.size(); j++) {
			result.get(j).get();
		}
	}

	private void checkUploadCompletion(InputStream stream, String[] blocks,List<Future<String>> result, int chunckSize,
			ExecutorService executorService, String container, String blobName,
			HashMap<String, String> headers) throws InterruptedException, ExecutionException {

		for (int i = 0; i < blocks.length; i++) {

			byte[] chunck = new byte[chunckSize];

			try {
				readToByteArray(stream, chunck);
			} catch (IOException e) {
				throw new ExecutionException(e);
			}

			if (blocks[i] != null)
				continue;

			if (i % NTHREAD == 0 && i > 0) {
				join(result);
				result.clear();
			}

			String blockId = encodeBlockId(String.format("b_%05d", i));

			Future<String> future = executorService.submit(new UploadingTask(blockId, container, blobName, chunck, headers.clone(),blocks, i, null));

			result.add(future);

		}

		join(result);
	}

}
