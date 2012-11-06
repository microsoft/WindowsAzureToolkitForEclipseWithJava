/*
 Copyright 2012 Microsoft Open Technologies, Inc. 

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
 package com.microsoftopentechnologies.acs.federation; 

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.microsoftopentechnologies.acs.saml.SAMLAssertion;
import com.microsoftopentechnologies.acs.util.Utils;
import com.microsoftopentechnologies.acs.xmldsig.TrustParameters;

public class ACSFederationAuthFilter implements Filter {
	private static final Logger LOG = Logger.getLogger(ACSFederationAuthFilter.class.getName());

	private static final String PASSIVE_REQUESTOR_ENDPOINT = "PassiveRequestorEndpoint";
	private static final String RELYING_PARTY_REALM = "RelyingPartRealm";
	private static final String CERTIFICATE_PATH = "CertificatePath";
	private static final String SECRET_KEY = "SecretKey";
	private static final String ALLOW_HTTP = "AllowHTTP";	
	public static final String ACS_SAML = "ACSSAML";
	public static final String EMBEDDED_CERT_LOC = "/WEB-INF/cert/_acs_signing.cer";
	
	protected String passiveRequestorEndPoint;
	protected String relyingPartyRealm;
	protected String certificatePath;
	protected String secretKey;
	protected TrustParameters trustParams;
	protected boolean allowHttp = false;
	private StatelessFilterCommand filterCommand;
	private static final String UNAUTHORIZED_ERROR_MESSAGE = "Provided authentication details are invalid.";

	public void init(FilterConfig filterConfig) throws ServletException {
		Utils.logDebug("Initializing the filter..", LOG);

		passiveRequestorEndPoint = filterConfig.getInitParameter(PASSIVE_REQUESTOR_ENDPOINT);
		Utils.logInfo("Passive Requestor Endpoint is:" + passiveRequestorEndPoint, LOG);
		if (passiveRequestorEndPoint == null) {
			throw new ServletException(PASSIVE_REQUESTOR_ENDPOINT + " init parameter not proivded in the filter configuration.");
		}
		// Remove query parameters if any
		passiveRequestorEndPoint = (passiveRequestorEndPoint != null && passiveRequestorEndPoint.indexOf('?') > 0 ) ? 
									passiveRequestorEndPoint.substring(0, passiveRequestorEndPoint.indexOf('?')):
									passiveRequestorEndPoint;

		relyingPartyRealm = filterConfig.getInitParameter(RELYING_PARTY_REALM);
		Utils.logInfo("Relying Party Realm is:" + relyingPartyRealm, LOG);
		if (relyingPartyRealm == null) {
			throw new ServletException(RELYING_PARTY_REALM + " init parameter not proivded in the filter configuration.");
		}

		certificatePath = filterConfig.getInitParameter(CERTIFICATE_PATH);
		Utils.logInfo("Certificate path:" + certificatePath, LOG);
		if (certificatePath == null) {  
			//1. check for embedded cert and if exists set certPath to cert/acs_signing.cer
			if(filterConfig.getServletContext().getResourceAsStream(EMBEDDED_CERT_LOC) != null )
				certificatePath = EMBEDDED_CERT_LOC;
			else
				throw new ServletException(CERTIFICATE_PATH + " init parameter not proivded in the filter configuration" +
						" or Embeddded Cert is not found at /WEB-INF/cert/_acs_signing.cer");
		}
		
		secretKey = filterConfig.getInitParameter(SECRET_KEY);
		if (secretKey == null) {
			throw new ServletException(SECRET_KEY + " init parameter not proivded in the filter configuration.");
		}
		
		allowHttp = Boolean.parseBoolean(filterConfig.getInitParameter(ALLOW_HTTP));
		
		//create keystore
		Key publicKey = getPublicKey(certificatePath,filterConfig);
		trustParams = new TrustParameters(publicKey,Utils.getSecretKey(secretKey),allowHttp,relyingPartyRealm);

		// Create the command which performs actual filtering
		Utils.logDebug("Creating stateless filter...", LOG);
		filterCommand = new StatelessFilterCommand(this);

	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,	FilterChain chain) throws IOException, ServletException {
		Utils.logDebug("In the doFilter method..", LOG);

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		this.filterCommand.execute(httpRequest, httpResponse, chain);
	}

	SAMLAssertion getSAMLAssertionFromACSResponse(HttpServletRequest request) {
		String securityTokenResponse = request.getParameter("wresult");
		Utils.logDebug("wsresult in the response from ACS is " + securityTokenResponse, LOG);

		if (securityTokenResponse == null) {
			return null;
		}

		// None of Java XML objects are thread-safe. Better to create instance on demand rather than caching.
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilderFactory.setNamespaceAware(true); // very important, must
		DocumentBuilder docBuilder;
		SAMLAssertion assertion = null;

		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
			Document respDoc = docBuilder.parse(new ByteArrayInputStream(Utils.getUTF8Bytes(securityTokenResponse)));
			// Find the response token
			Element responseToken = (Element) respDoc.getDocumentElement().getElementsByTagNameNS("http://schemas.xmlsoap.org/ws/2005/02/trust", "RequestedSecurityToken").item(0);
			assertion = SAMLAssertion.getAssertionFromSecurityToken(responseToken);
		} catch (Exception e) {
			Utils.logError("Exception while parsing the security token response from ACS.", e, LOG);
		}
		return assertion;
	}

	void invokeChainWithRemoteUser(FilterChain chain, HttpServletRequest httpRequest, HttpServletResponse httpResponse, SAMLAssertion assertion) throws IOException, ServletException	{
		// set assertion as an attribute in the request
		try {
			httpRequest.setAttribute(ACS_SAML, Utils.getXMLStringFromNode(assertion.getAssertionXMLElement()));
		}catch(Exception e ){
			Utils.logError("Invalid Saml Content.", e, LOG);
			throw new ServletException("Invalid SAML Content");
		}
		String remoteUser = getUserFromAssertion(assertion);
		invokeChainWithRemoteUser(chain, httpRequest, httpResponse, remoteUser);
	}

	private String getUserFromAssertion(SAMLAssertion assertion) {
		String user = null;
		// Check name claim attribute. If exists set as remote user else use NameID
		SAMLAssertion.Attribute[] attributes = assertion.getAttributes();
		for (SAMLAssertion.Attribute attribute : attributes) {
			if (attribute.getName().endsWith("claims/name")) {
				user = attribute.getValues()[0];
				break;
			}
		}

		if (user == null) {
			Utils.logDebug("No name claim found in the assertion, so assuming subject's name identifier as the remote user.", LOG);
			user = assertion.getSubject().getNameIdentifier();
		}
		return user;
	}
	
	String getCompleteRequestURL(HttpServletRequest httpRequest) {
		StringBuffer completeRequestURL = httpRequest.getRequestURL();
		String queryString = httpRequest.getQueryString();
		if (queryString != null && !queryString.isEmpty()) {
			completeRequestURL.append('?').append(queryString);
		}
		return completeRequestURL.toString();
	}

	private static Key getPublicKey(String certificatePath, FilterConfig filterConfig) throws ServletException {
		Certificate certificate = null;
		InputStream is  = null;
		try	{
			if(certificatePath != null)
				certificatePath = certificatePath.replace('\\', '/');
			certificatePath = getCertificatePath(certificatePath);
			File certFile   = new File(certificatePath);
			if(certFile.isAbsolute())
				is = new FileInputStream(certificatePath);
			else
				is = filterConfig.getServletContext().getResourceAsStream(EMBEDDED_CERT_LOC); 
			BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
			CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
			while (bufferedInputStream.available() > 0) {
				certificate = certificateFactory.generateCertificate(bufferedInputStream);
			}			
		} catch (FileNotFoundException fnfe)	{
			throw new ServletException("File not found "+certificatePath);			
		} catch (Throwable t)	{
			throw new ServletException("Error while retrieving public key from certificate");
		}
		return certificate.getPublicKey();
	}

	private static String getCertificatePath(String rawPath) {
		String certPath = null;
		if (rawPath != null && rawPath.length() > 0) {
			String[] result = rawPath.split("/");
			StringBuilder  path = new StringBuilder();

			for (int x = 0; x < result.length; x++) {
				if (result[x].startsWith("${env")) {
					String envValue = System.getenv(result[x].substring("${env.".length(), (result[x].length() - 1)));
					path.append(envValue).append(File.separator);
				} else {
					path.append(result[x]).append(File.separator);
				}
			}

			//Delete last trailing slash
			path = path.deleteCharAt(path.length() - 1);
			certPath = path.toString();
		}
		return certPath;
	}
	
	protected void invokeChainWithRemoteUser(FilterChain chain, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			String remoteUser) throws IOException, ServletException	{
		Utils.logDebug("Invoking the request with remote user : " + remoteUser, LOG);
		HttpServletRequest httpRequestWithRemoteUser = setRemoteUserInServletRequest(httpRequest, remoteUser);
		chain.doFilter(httpRequestWithRemoteUser, httpResponse);
	}
	
	protected HttpServletRequest setRemoteUserInServletRequest(HttpServletRequest httpRequest, final String remoteUser)	{
		return new HttpServletRequestWrapper(httpRequest){
			public String getRemoteUser() {
				return remoteUser;
			}			
		};
	}
	
	protected void invokeChainWithRemoteUserAndOldRequest(FilterChain chain, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			String remoteUser, HttpServletRequestDetails requestDetails) throws IOException, ServletException {
		Utils.logDebug(String.format("Invoking the request with remote user: %s and the details of the request that caused a redirect to ACS", remoteUser), LOG);
		HttpServletRequest httpRequestWithRemoteUser = setRemoteUserAndOldRequestDetailsInServletRequest(httpRequest, remoteUser, requestDetails);
		chain.doFilter(httpRequestWithRemoteUser, httpResponse);
	}
	
	protected HttpServletRequest setRemoteUserAndOldRequestDetailsInServletRequest(HttpServletRequest httpRequest, final String remoteUser, final HttpServletRequestDetails requestDetails)	{
		return new HttpServletRequestWrapper(httpRequest){
			public String getRemoteUser() {
				return remoteUser;
			}

			public String getParameter(String name) {
				String[] paramValues = requestDetails.getParameterMap().get(name);
				if(paramValues == null || paramValues.length == 0) {
					return null;
				} else {
					return paramValues[0];
				}
			}

			public Map<String, String[]> getParameterMap() {
				return requestDetails.getParameterMap();
			}

			public Enumeration<String> getParameterNames() {
				Set<String> paramNamesSet = requestDetails.getParameterMap().keySet();
				final Iterator<String> paramNamesIterator = paramNamesSet.iterator();
				return new Enumeration<String>() {
					public boolean hasMoreElements() {
						return paramNamesIterator.hasNext();
					}

					public String nextElement() {
						return paramNamesIterator.next();
					}
					
				};
			}

			public String[] getParameterValues(String name) {
				return requestDetails.getParameterMap().get(name);
			}
			
			public String getMethod() {
				return requestDetails.getMethod();
			}
		};
	}
	
	protected void sendLogOnFailureResponse(HttpServletResponse httpResponse) throws IOException {
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_ERROR_MESSAGE);
	}
	
	protected void sendLogOnFailureResponse(HttpServletResponse httpResponse, String cause) throws IOException {
		String errorMessage = UNAUTHORIZED_ERROR_MESSAGE + " " + cause;
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
	}
}
