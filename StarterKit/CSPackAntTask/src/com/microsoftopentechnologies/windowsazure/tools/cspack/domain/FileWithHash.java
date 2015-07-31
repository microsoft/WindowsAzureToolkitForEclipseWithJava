/*
 Copyright Microsoft Corp.

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
package com.microsoftopentechnologies.windowsazure.tools.cspack.domain;

public class FileWithHash {
    private String name;
    private String hashValue;

    public FileWithHash(String name, String hashValue) {
        this.name = name;
        this.hashValue = hashValue;
    }

    public String getName() {
        return name;
    }

    public String getHashValue() {
        return hashValue;
    }
}
