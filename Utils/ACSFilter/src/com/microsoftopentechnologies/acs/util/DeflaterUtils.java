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
