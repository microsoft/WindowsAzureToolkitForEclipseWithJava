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

package com.microsoftopentechnologies.wacommon.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	
	public static String replaceLastSubString(String location, String find, String replaceWith) {
	    if (location == null || location.isEmpty())
	    	return location;
	    
		int lastIndex = location.lastIndexOf(find);
	    
		if (lastIndex < 0) 
	    	 return location;
	     
	     String end = location.substring(lastIndex).replaceFirst(find, replaceWith);
	     return location.substring(0, lastIndex) + end;
	}
	
	public static String getDefaultCNName() {
	    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	    	Date now = new Date();
	    	return "Self Signed Certificate " + dateFormat.format(now);
	}

}
