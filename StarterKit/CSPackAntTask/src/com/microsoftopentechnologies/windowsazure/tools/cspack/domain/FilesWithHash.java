/*
 Copyright 2015 Microsoft Open Technologies, Inc.

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

import com.microsoftopentechnologies.windowsazure.tools.cspack.domain.xsd.servicedefinition.Role;

import java.util.List;

public class FilesWithHash {
    private List<FileWithHash> filesWithHash;
    private boolean isRoot;
    private List<Role> roles;

    public FilesWithHash(List<FileWithHash> filesWithHash, boolean isRoot, List<Role> roles) {
        this.filesWithHash = filesWithHash;
        this.isRoot = isRoot;
        this.roles = roles;
    }

    public List<FileWithHash> getFilesWithHash() {
        return filesWithHash;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public List<Role> getRoles() {
        return roles;
    }
}
