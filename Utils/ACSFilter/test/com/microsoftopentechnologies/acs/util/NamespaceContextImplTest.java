package com.microsoftopentechnologies.acs.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.xml.XMLConstants;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoftopentechnologies.acs.util.NamespaceContextImpl;

public class NamespaceContextImplTest {

	private static HashMap<String, String> prefixesToNSURIMappings;
	private static final String DEFAULT_NAMESPACE_URI = "http://schemas.microsoft.com/default";
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		prefixesToNSURIMappings = new HashMap<String, String>();
		prefixesToNSURIMappings.put("one", "http://schemas.microsoft.com/one");
		prefixesToNSURIMappings.put("two", "http://schemas.microsoft.com/two");
		prefixesToNSURIMappings.put("three", "http://schemas.microsoft.com/three");
		prefixesToNSURIMappings.put("four", "http://schemas.microsoft.com/four");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetNamespaceURI() {
		NamespaceContextImpl context = new NamespaceContextImpl(DEFAULT_NAMESPACE_URI, prefixesToNSURIMappings);
		String nameSpaceURIForOne = context.getNamespaceURI("one");
		String expectedNameSpaceURIForOne = "http://schemas.microsoft.com/one";
		assertEquals("Namespace context returned incorrect namespace URI for a prefix", expectedNameSpaceURIForOne, nameSpaceURIForOne);
		
		String nameSpaceURIForThree = context.getNamespaceURI("three");
		String expectedNameSpaceURIForThree = "http://schemas.microsoft.com/three";
		assertEquals("Namespace context returned incorrect namespace URI for a prefix", expectedNameSpaceURIForThree, nameSpaceURIForThree);
	}
	
	@Test
	public void testDefaultNameSpaceURI() {
		NamespaceContextImpl context = new NamespaceContextImpl(DEFAULT_NAMESPACE_URI, prefixesToNSURIMappings);
		String actualDefaultNamespaceURI = context.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
		assertEquals("Namespace context returned incorrect namespace URI default namespace prefix", DEFAULT_NAMESPACE_URI, actualDefaultNamespaceURI);
		
		NamespaceContextImpl context2 = new NamespaceContextImpl(DEFAULT_NAMESPACE_URI);
		actualDefaultNamespaceURI = context2.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
		assertEquals("Namespace context returned incorrect namespace URI default namespace prefix", DEFAULT_NAMESPACE_URI, actualDefaultNamespaceURI);
	}
	
	@Test
	public void testGetNamespaceURIForStandardPrefixes() {
		NamespaceContextImpl context = new NamespaceContextImpl(DEFAULT_NAMESPACE_URI);
		String actualNSURIForXML = context.getNamespaceURI(XMLConstants.XML_NS_PREFIX);
		assertEquals("Namespace context returned incorrect namespace URI for prefix 'xml'", XMLConstants.XML_NS_URI, actualNSURIForXML);
				
		String actualNSURIForXMLNS = context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE);
		assertEquals("Namespace context returned incorrect namespace URI for prefix 'xmlns'", XMLConstants.XMLNS_ATTRIBUTE_NS_URI, actualNSURIForXMLNS);
	}
	
	@Test
	public void testGetNamesapceURIForUnknownPrefix() {
		NamespaceContextImpl context = new NamespaceContextImpl(DEFAULT_NAMESPACE_URI, prefixesToNSURIMappings);
		String actualNSURIForUnknownPrefix = context.getNamespaceURI("UnknownPrefix");
		assertEquals("Namespace context returned incorrect namespace URI for unknown prefix", XMLConstants.NULL_NS_URI, actualNSURIForUnknownPrefix);
	}

}
