package com.interopbridges.tools.windowsazure;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WindowsAzureCacheTest {

    private WindowsAzureProjectManager wProj = null;
    private WindowsAzureRole role1 = null;
    private WindowsAzureRole role2 = null;
    @Before
    public final void setUp() {
        try {
            wProj = WindowsAzureProjectManager.load(
                    new File(Messages.getString(
                            "WinAzureTestConstants.WACache")));
            role1 = wProj.getRoles().get(0);
            role2 = wProj.getRoles().get(1);

        } catch (WindowsAzureInvalidProjectOperationException e) {
            fail("test case failed");
        }
    }


    //setCacheMemoryPercent
    @Test
    public void testSetCacheMemoryPercentToEnable()
    		throws WindowsAzureInvalidProjectOperationException {
    	role2.setCacheMemoryPercent(60);
    	assertEquals(60, role2.getCacheMemoryPercent());
    }

    @Test
    public void testSetCacheMemoryPercentToDisabel()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheMemoryPercent(0);
    	assertEquals(0, role1.getCacheMemoryPercent());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetCacheMemoryPercentToDisabelWithOverRange()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheMemoryPercent(111);
    	assertEquals(0, role1.getCacheMemoryPercent());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetCacheMemoryPercentToDisabelWithBelowRange()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheMemoryPercent(-1);
    	assertEquals(0, role1.getCacheMemoryPercent());
    }


    //getCacheMemoryPercent
    @Test
    public void testgetCacheMemoryPercentToEnabled()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals(60, role1.getCacheMemoryPercent());
    }

    @Test
    public void testgetCacheMemoryPercentToDisabel()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals(0, role2.getCacheMemoryPercent());
    }

    //setCacheStorageAccountName
    @Test
    public void testsetCacheStorageAccountName()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheStorageAccountName("MyAccount");
    	assertEquals("MyAccount", role1.getCacheStorageAccountName());
    }

    @Test()
    public void testsetCacheStorageAccountNameEmpty()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheStorageAccountName("");
    	assertEquals("", role1.getCacheStorageAccountName());
    }

    //setCacheStorageAccountKey

    @Test
    public void testsetCacheStorageAccountkey()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheStorageAccountKey("Mykey");
    	assertEquals("Mykey", role1.getCacheStorageAccountKey());
    }


    @Test
    public void testsetCacheStorageAccountkeyEmpty()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.setCacheStorageAccountKey("");
    	assertEquals("", role1.getCacheStorageAccountKey());
    }


    //getCacheStorageAccountName
    @Test
    public void testgetCacheStorageAccountName()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals("WAAccount", role1.getCacheStorageAccountName());
    }

    @Test
    public void testgetCacheStorageAccountNameEmpty()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals("", role2.getCacheStorageAccountName());
    }

    //getCacheStorageAccountKey
    @Test
    public void testgetCacheStorageAccountkey()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals("WAkey", role1.getCacheStorageAccountKey());
    }

    @Test
    public void testgetCacheStorageAccountkeyEmpty()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals("", role2.getCacheStorageAccountKey());
    }


    //addNamedCache
    @Test
    public void testaddNamedCache()
    		throws WindowsAzureInvalidProjectOperationException {
    	role2.setCacheMemoryPercent(56);
    	role2.addNamedCache("New MyCache", 25);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testaddNamedCacheWithNull()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.addNamedCache(null, 25);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testaddNamedCacheWithEmpty() throws WindowsAzureInvalidProjectOperationException {
    	role1.addNamedCache("", 25);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testaddNamedCacheWithInvalidPort()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.addNamedCache("test", -25);
    }


    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testaddNamedCacheWithDuplicateName()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.addNamedCache("test", 25);
    	//add same name again
    	role1.addNamedCache("test", 50);
    }

    //To do: need to un-comment this testcase after port validation
//    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
//    public void testaddNamedCacheWithDuplicatePort()
//    		throws WindowsAzureInvalidProjectOperationException {
//    	role1.addNamedCache("test", 25);
//    	//add same name again
//    	role1.addNamedCache("test1", 25);
//    }

    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testaddNamedCacheWithDisabedCache()
    		throws WindowsAzureInvalidProjectOperationException {
    	role2.addNamedCache("test", 25);
    }


    //getNamedCaches
    @Test
    public void testGetNamedCaches()
    		throws WindowsAzureInvalidProjectOperationException {
    	Map<String, WindowsAzureNamedCache> map = role1.getNamedCaches();
    	assertEquals(1, map.size());
    }

    @Test
    public void testGetNamedCachesWithEmpty()
    		throws WindowsAzureInvalidProjectOperationException {
    	Map<String, WindowsAzureNamedCache> map = role2.getNamedCaches();
    	assertEquals(0, map.size());
    }

    private WindowsAzureNamedCache getCache(String cachename) throws WindowsAzureInvalidProjectOperationException {
    	Map<String, WindowsAzureNamedCache> map = role1.getNamedCaches();
    	Set<Entry<String, WindowsAzureNamedCache>> entrySet = map.entrySet();
    	for (Iterator<Entry<String, WindowsAzureNamedCache>> it = entrySet.iterator(); it.hasNext();) {
			Entry<String, WindowsAzureNamedCache> entry = (Entry<String, WindowsAzureNamedCache>) it
					.next();
			if (entry.getKey().equalsIgnoreCase(cachename)) {
				return entry.getValue();
			}
		}
    	return null;
    }

    //setName
    @Test(expected=WindowsAzureInvalidProjectOperationException.class)
    public void testSetNameRenameDefault()
    		throws WindowsAzureInvalidProjectOperationException {
    	getCache("default").setName("default_new");
    	assertTrue(role1.getNamedCaches().containsKey("default_new"));
    }

    @Test
    public void testSetName()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.addNamedCache("test1", 123);
    	getCache("test1").setName("test1_new");
    	assertTrue(role1.getNamedCaches().containsKey("test1_new"));
    }


    @Test(expected=IllegalArgumentException.class)
    public void testSetNameWithEmpty()
    		throws WindowsAzureInvalidProjectOperationException {
    	getCache("default").setName("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSetNameWithNull()
    		throws WindowsAzureInvalidProjectOperationException {
    	getCache("default").setName(null);
    }

    //getEndpoint
    @Test
    public void testGetEndPoint()
    		throws WindowsAzureInvalidProjectOperationException {
    	String epName = "";
    	epName = getCache("default").getEndpoint().getName();
    	assertEquals("memcache_default", epName);
    }

    //getBackups
    @Test
    public void testGetBackups()
    		throws WindowsAzureInvalidProjectOperationException {
    	boolean backup = true;
    	backup = getCache("default").getBackups();
    	assertFalse(backup);
    }

    @Test
    public void testSetBackups()
    		throws WindowsAzureInvalidProjectOperationException {
    	boolean backup = false;
    	getCache("default").setBackups(true);
		backup = getCache("default").getBackups();
    	assertTrue(backup);
    }

    //getMinutesToLive
    @Test
    public void testGetMinutesToLive()
    		throws WindowsAzureInvalidProjectOperationException {
    	int min;
    	min = getCache("default").getMinutesToLive();
    	assertEquals(15, min);
    }

    @Test
    public void testSetMinutesToLive()
    		throws WindowsAzureInvalidProjectOperationException {
    	int min;
    	getCache("default").setMinutesToLive(15);
    	min = getCache("default").getMinutesToLive();
    	assertEquals(15, min);
    }


    //getExpirationPolicy
    @Test
    public void testGetExpirationPolicy()
    		throws WindowsAzureInvalidProjectOperationException {
    	assertEquals(WindowsAzureCacheExpirationPolicy.NEVER_EXPIRES,
    			getCache("default").getExpirationPolicy());
    }

    //setExpirationPolicy
    @Test
    public void testSetExpirationPolicy()
    		throws WindowsAzureInvalidProjectOperationException {
    	getCache("default").setExpirationPolicy(WindowsAzureCacheExpirationPolicy.NEVER_EXPIRES);
    	assertEquals(WindowsAzureCacheExpirationPolicy.NEVER_EXPIRES,
    			getCache("default").getExpirationPolicy());
    }

    //delete
    @Test
    public void testDelete()
    		throws WindowsAzureInvalidProjectOperationException {
    	role1.addNamedCache("test", 25);
    	int size = role1.getNamedCaches().size();
    	getCache("test").delete();
    	assertEquals(size-1,role1.getNamedCaches().size());
    }

}


