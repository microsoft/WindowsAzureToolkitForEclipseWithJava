package com.microsoftopentechnologies.acs.saml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoftopentechnologies.acs.saml.SAMLAssertion;
import com.microsoftopentechnologies.acs.saml.SAMLAssertion.Conditions;

public class ConditionsTest {

	private static Calendar calendar;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		calendar = Calendar.getInstance();
	}
	
	@Test
	public void testValidNotBeforeAndNotAfter() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the past as NotBefore
		calendar.set(2011, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotBefore(calendar.getTimeInMillis());
		// Set some time in the future as NotOnOrAfter		
		calendar.set(2020, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotOnOrAfter(calendar.getTimeInMillis());
		// Condition must be valid wrt time
		assertTrue("A condition with past NotBefore and future notOnOrAfter is incorrectly evaluated to valid in time", conditions.isValidInTime());
	}
	
	@Test
	public void testExpiredNotBeforeAndNotAfter() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the past as NotBefore
		calendar.set(2011, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotBefore(calendar.getTimeInMillis());
		// Set some time in the past as NotOnOrAfter		
		calendar.set(2011, Calendar.SEPTEMBER, 15, 23, 32, 56);
		conditions.setNotOnOrAfter(calendar.getTimeInMillis());
		// Condition must be invalid wrt time
		assertFalse("A condition with past NotBefore and past notOnOrAfter is incorrectly evaluated to valid in time", conditions.isValidInTime());
	}
	
	@Test
	public void testFutureNotBeforeAndNotAfter() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the future as NotBefore
		calendar.set(2020, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotBefore(calendar.getTimeInMillis());
		// Set some time in the future as NotOnOrAfter		
		calendar.set(2022, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotOnOrAfter(calendar.getTimeInMillis());
		// Condition must be invalid wrt time
		assertFalse("A condition with future NotBefore and future notOnOrAfter is incorrectly evaluated to valid in time", conditions.isValidInTime());
	}
	
	@Test
	public void testUnspecifiedNotBeforeAndNotAfter() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Don't specify NotBefore and NonOnOrAfter
		// Conditions must always be valid wrt timing
		assertTrue("A condition with no notBefore and no OnOrAfter is incorrectly evaluated as invalid in time", conditions.isValidInTime());
	}
	
	@Test
	public void testValidNotBefore() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the past as NotBefore
		calendar.set(2011, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotBefore(calendar.getTimeInMillis());
		// Condition must be valid wrt timing
		assertTrue("A condition with valid notBefore and no OnOrAfter is incorrectly evaluated as invalid in time", conditions.isValidInTime());
	}
	@Test
	public void testFutureNotBefore() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the future as NotBefore
		calendar.set(2020, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotBefore(calendar.getTimeInMillis());
		// Condition must be invalid wrt timing
		assertFalse("A condition with futrue notBefore and no OnOrAfter is incorrectly evaluated as valid in time", conditions.isValidInTime());
	}
	
	@Test
	public void testValidNotAfter() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the future as NotOnOrAfter
		calendar.set(2020, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotOnOrAfter(calendar.getTimeInMillis());
		// Condition must be valid wrt timing
		assertTrue("A condition with no notBefore and valid OnOrAfter is incorrectly evaluated as invalid in time", conditions.isValidInTime());
	}
	
	@Test
	public void testPastNotAfter() {
		Conditions conditions = new SAMLAssertion.Conditions();
		// Set some time in the past as NotOnOrAfter
		calendar.set(2010, Calendar.AUGUST, 15, 23, 32, 56);
		conditions.setNotOnOrAfter(calendar.getTimeInMillis());
		// Condition must be invalid wrt timing
		assertFalse("A condition with no notBefore and past OnOrAfter is incorrectly evaluated as valid in time", conditions.isValidInTime());
	}

}
