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


package com.microsoftopentechnologies.azurecommons.deploy.model;

import java.io.Serializable;
import java.util.Date;

public class RemoteDesktopDescriptor implements Serializable {

	private static final long serialVersionUID = -5473860018650709333L;
	private final String userName;
	private final String password;
	private final Date expirationDate;
	private final String publicKey;
	private final boolean startRemoteRDP;
	private final boolean enabled;
	
	public RemoteDesktopDescriptor(String userName, String password,
			Date expirationDate, String publicKey,
			boolean startRemoteRDP, boolean enabled) {
		this.userName = userName;
		this.password = password;
		this.expirationDate = expirationDate;
		this.publicKey = publicKey;
		this.startRemoteRDP = startRemoteRDP;
		this.enabled = enabled;
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the expirationDate
	 */
	public Date getExpirationDate() {
		return expirationDate;
	}

	/**
	 * @return the publicKey
	 */
	public String getPublicKey() {
		return publicKey;
	}

	public boolean isStartRemoteRDP() {
		return startRemoteRDP;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
