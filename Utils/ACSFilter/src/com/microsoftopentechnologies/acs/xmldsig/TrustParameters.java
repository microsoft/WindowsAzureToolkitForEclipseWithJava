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

package com.microsoftopentechnologies.acs.xmldsig; 

import java.security.Key;

import javax.crypto.SecretKey;

/**
 * Holder class for the parameters required to verify certificate chains.
 */
public class TrustParameters {
	private Key publicKey;
	private SecretKey secretKey;
	private boolean allowHttp;
	private String relyingPartyRealm;
	
	public TrustParameters(Key publicKey) {
		this.publicKey = publicKey;		
	}
	public TrustParameters(Key publicKey,SecretKey secretKey,boolean allowHttp,String relyingPartyRealm) {
		this.publicKey = publicKey;
		this.secretKey = secretKey;
		this.allowHttp = allowHttp;
		this.relyingPartyRealm = relyingPartyRealm;
	}

	public Key getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(Key publicKey) {
		this.publicKey = publicKey;		
	}
	
	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;		
	}
	
	public boolean getAllowHttp() {
		return allowHttp;
	}
	
	public void setAllowHttp(boolean allowHttp) {
		this.allowHttp = allowHttp;		
	}
	
	public String getRelyingPartyRealm() {
		return relyingPartyRealm;
	}

	public void setRelyingPartyRealm(String relyingPartyRealm) {
		this.relyingPartyRealm = relyingPartyRealm;		
	}

}
