package com.microsoftopentechnologies.acs.federation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import javax.servlet.http.Cookie;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.microsoftopentechnologies.acs.federation.StatelessFilterCommand;
import com.microsoftopentechnologies.acs.saml.AssertionNotFoundException;
import com.microsoftopentechnologies.acs.saml.SAMLAssertion;

public class StatelessFilterCommandTest {

	@Test
	public void testSortAndAppendCookieValues() throws Exception {
		List<Cookie> cookieList = new ArrayList<Cookie>();
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "0", "Dummy asse"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "4", "for the test"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "2", "ue"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "5", " case"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "3", " created "));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "1", "rtion val"));
		String valueExtractedFromCookies = new StatelessFilterCommand(null).extractAssertionContentFromCookies(cookieList);
		String expectedOutput = "Dummy assertion value created for the test case";
		assertEquals("Sorting and appending cookie values falied.", expectedOutput, valueExtractedFromCookies);
	}
	
	@Test
	public void testSortAndAppendCookieValues4LargeValue() throws Exception{
		String expectedOutput = "rZRvb5swEMa/CuI9mBAIBBG2tJWqSP0jNVGm7U112AdxBzazTdN8+5msSVq1yl5siBfoOD/34547\\n8rnWqAyXwllc" +
				"zdzHOKIhTQE9VoWJF0XV2CtZEnnjYJwCS6OYInOdhdY9LoQ2IMzMDYPRyAtSL4xX\\nYZCNwmw09aNp8sN11qi0lbYpfuA6L20j9Mztl" +
				"cgkaK4zAS3qzNBsOb+9yWxOBgcat8j3RVSxMabT\\nGSHP/Al2ugOKPlCKWlMpjJKND11XQak4baDUPpUtycnr2XzZl09ITZHf2VKLq6PY" +
				"drv1aynrBvcH\\nrKDshdFEpoSzL5zN5gtzD9sGYhH1Na2Tx4vgTrBFKJ88urp5Ga9F9evnt9ucvAofKl1KUXHVwr6j\\nt2g2kp3/YtpmJ" +
				"YJC5X6qcQUGnIV4QN1JoXElrUcBY2w6ZoE3GceVF8GEeelkknq0qtJyEk+TKJm4\\nzp009+JezSuD6oNHyeBRPAqtRw9IecdxMHLojm1OIy" +
				"k0G6lNltqLzC+XK8WhIa5Dipx8AnmKFrmN\\nMz4E9UBwgZVUeG5EzmGGp7win/fMUlK0rTDW7D91D8HiL+g5OWaeHt8pkRO4lTX2RdkbX" +
				"Bow2Nrm\\nvIk5g+XHZmm6wRa0b4dbS+h8qWqy1SQMgpjYmzN7mJsdsdPJW01sLm+AMWUH2H0juoamx2JXyw6a\\nr/WQNAymZX2f8Cbwv4iGk" +
				"fxIsh62DZzrgcf5DkpBK1DwfwJqOVVSy8ocdu60xHbrnzlFCzkKSJAc\\n2A6snZLP9ll95LzeL/F5LPKZneT44yt+Aw==";
		List<Cookie> cookieList = new ArrayList<Cookie>();
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX, "rZRvb5swEMa/CuI9mBAIBBG2tJWqSP0jNVGm7U112AdxBzazTdN8+5msSVq1yl5siBfoOD/34547\\n8rnWqAyXwllc"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "4", "drv1aynrBvcH\\nrKDshdFEpoSzL5zN5gtzD9sGYhH1Na2Tx4vgTrBFKJ88urp5Ga9F9evnt9ucvAofKl1KUXHVwr6j\\nt2g2kp3/YtpmJ"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "2", "cgkaK4zAS3qzNBsOb+9yWxOBgcat8j3RVSxMabT\\nGSHP/Al2ugOKPlCKWlMpjJKND11XQak4baDUPpUtycnr2XzZl09ITZHf2VKLq6PY"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "9", "k0G6lNltqLzC+XK8WhIa5Dipx8AnmKFrmN\\nMz4E9UBwgZVUeG5EzmGGp7win/fMUlK0rTDW7D91D8HiL+g5OWaeHt8pkRO4lTX2RdkbX"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "7", "YJC5X6qcQUGnIV4QN1JoXElrUcBY2w6ZoE3GceVF8GEeelkknq0qtJyEk+TKJm4\\nzp009+JezSuD6oNHyeBRPAqtRw9IecdxMHLojm1OIy"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "1", "zdzHOKIhTQE9VoWJF0XV2CtZEnnjYJwCS6OYInOdhdY9LoQ2IMzMDYPRyAtSL4xX\\nYZCNwmw09aNp8sN11qi0lbYpfuA6L20j9Mztl"));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "94", "fxIsh62DZzrgcf5DkpBK1DwfwJqOVVSy8ocdu60xHbrnzlFCzkKSJAc\\n2A6snZLP9ll95LzeL/F5LPKZneT44yt+Aw=="));
		cookieList.add(new Cookie(StatelessFilterCommand.COOKIE_PREFIX + "50", "Bow2Nrm\\nvIk5g+XHZmm6wRa0b4dbS+h8qWqy1SQMgpjYmzN7mJsdsdPJW01sLm+AMWUH2H0juoamx2JXyw6a\\nr/WQNAymZX2f8Cbwv4iGk"));
		String valueExtractedFromCookies = new StatelessFilterCommand(null).extractAssertionContentFromCookies(cookieList);
		
		assertEquals("Sorting and appending cookie values falied.", expectedOutput, valueExtractedFromCookies);
	}
	
	

}
