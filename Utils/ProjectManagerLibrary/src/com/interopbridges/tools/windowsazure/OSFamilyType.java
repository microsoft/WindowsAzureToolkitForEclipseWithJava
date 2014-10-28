/**
* Copyright 2014 Microsoft Open Technologies, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*	 http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.interopbridges.tools.windowsazure;

public enum OSFamilyType {
    WINDOWS_SERVER_2008_R2(2,"Windows Server 2008 R2"),
    WINDOWS_SERVER_2012(3,"Windows Server 2012"),
    WINDOWS_SERVER_2012_R2(4,"Windows Server 2012 R2");

    private int value;
    private String name;

    private OSFamilyType(int value,String name){
        this.value = value;
        this.name  = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
