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

package com.gigaspaces.azure.wizards;

import java.io.File;

import com.gigaspaces.azure.util.FileSearchListerner;

public class PathListener implements FileSearchListerner {

	private String fileName;
	private String dirName;
	private String result;

	public PathListener(String fileName, String dirName) {
		this.fileName = fileName;
		this.dirName = dirName;
	}

	@Override
	public void onDirectory(File directory) {
	}

	@Override
	public void onFile(File file) {
		if (file.getName().endsWith(fileName)
				&& file.getParent().endsWith(dirName)) {
			result = file.getPath();
		}
	}

	public String getResult() {
		return result;
	}

}
