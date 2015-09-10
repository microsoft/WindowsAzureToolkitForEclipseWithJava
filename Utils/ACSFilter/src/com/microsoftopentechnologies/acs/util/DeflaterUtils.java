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

package com.microsoftopentechnologies.acs.util; 

import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class DeflaterUtils {
	public static byte[] deflate(byte[] inputBytes)	{
		/*
		 * For smaller inputs, the size of deflated content can be bigger than the input.
	     * So, adding some buffer for that.
		 */
		int additionalBufferSize = 10;
		int deflaterBufferSize = inputBytes.length + additionalBufferSize;
		// Deflate the content
		byte[] deflatedBytes = new byte[deflaterBufferSize];
		// Set nowrap to true, to ignore the ZLIB headers
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
		deflater.setInput(inputBytes);
		deflater.finish();
		int number_deflated_bytes = deflater.deflate(deflatedBytes);
		return Arrays.copyOf(deflatedBytes, number_deflated_bytes);
	}

	public static byte[] inflate(byte[] inputBytes) throws DataFormatException	{
		// Inflate the content, assuming the size won't increase more than 10 times
		byte[] inflatedBytes = new byte[inputBytes.length * 10];
		/*
		 * As we are using the 'nowrap' option,  it is necessary to provide an extra "dummy" byte as input.
		 * Seems to be working even without this extra dummy byte. But javadoc recommends to add a dummy byte.
		 * Adding 0 add the dummy byte at the end of the input byte array.
		 */
		byte[] inputWithDummyByteAdded = Arrays.copyOf(inputBytes, inputBytes.length + 1);

		// Set nowrap to true, to ignore the ZLIB headers
		Inflater inflater = new Inflater(true);
		inflater.setInput(inputWithDummyByteAdded);
		int number_inflated_bytes = inflater.inflate(inflatedBytes);
		inflater.end();
		return Arrays.copyOf(inflatedBytes, number_inflated_bytes);
	}

}
