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
