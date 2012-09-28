package com.interopbridges.tools.windowsazure;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WindowsAzureNamedCache {

    private String name;
    private boolean isBackup;
    private int minToLive;
    private WindowsAzureCacheExpirationPolicy expPolicy;
    private WindowsAzureRole wRole;
    private WindowsAzureProjectManager wProj;

    public WindowsAzureNamedCache(WindowsAzureRole waRole, WindowsAzureProjectManager waProj) {
        wRole = waRole;
        wProj = waProj;
    }

    private Element getCachedNode() throws WindowsAzureInvalidProjectOperationException {
        Element ele = null;
        try {
            Document doc = wProj.getConfigFileDoc();
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expr = String.format(WindowsAzureConstants.CONFIG_SETTING_ROLE,wRole.getName(),
                    WindowsAzureConstants.SET_NAMEDCACHE);
            ele = (Element) xPath.evaluate(expr, doc,
                    XPathConstants.NODE);

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
        return ele;
    }

    private String getCache() throws WindowsAzureInvalidProjectOperationException {
        try {
            Element ele = getCachedNode();
            String encCompCache = ele.getAttribute(WindowsAzureConstants.ATTR_VALUE);
            Map<String, String> cacheMap =  JSONHelper.getCaches(encCompCache);
            return cacheMap.get(this.name);

        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }

    }

    private String setCache(String newCache)
            throws WindowsAzureInvalidProjectOperationException {
        try {
            Element ele = getCachedNode();
            String encCompCache = ele.getAttribute(WindowsAzureConstants.ATTR_VALUE);
            String newEncCompCache = JSONHelper.setCache(encCompCache, this.name, newCache);
            return newEncCompCache;
        } catch (WindowsAzureInvalidProjectOperationException ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }

    /**
     * This API will sets the name of cache.
     * Throw if
     *  name is null or in use by another cache (case insensitive comparison)
     *  the associated endpoint cannot be renamed to "memchache_" + name (e.g. it's duplicate)
     *  the current name is "default" – that one cannot be renamed
     * - Else:
     * change the name of the cache in JSON (name)
     * rename the associated endpoint to "memcache_" + name
     * @param name
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setName(String name) throws WindowsAzureInvalidProjectOperationException {
        if(name==null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        if ("default".equalsIgnoreCase(this.name)) {
            throw new WindowsAzureInvalidProjectOperationException(
                    "Default cache can not be renamed");
        }
        String epName = "memcache_" + name;
        if(!name.equalsIgnoreCase(this.name) && this.name != null && !this.name.isEmpty()) {
            if (wRole.getEndpoint(epName) != null) {
                throw new WindowsAzureInvalidProjectOperationException(
                         epName + " already exist");
            }
        }

        if (this.name == null) {
            this.name = name;
        }

        String cache = getCache();
        String newCache = JSONHelper.setParamValue(cache,
                WindowsAzureConstants.ATTR_NAME, name);
        String newEncCompCache = setCache(newCache);
        getCachedNode().setAttribute(WindowsAzureConstants.ATTR_VALUE, newEncCompCache);
        if(getEndpoint() != null) {
            getEndpoint().setName("memcache_" + name);
        }

        wRole.cacheMap.remove(getName());
        wRole.cacheMap.put(name, this);
        this.name = name;
    }

    /**
     * This API will gets the name of cache
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * This API will give the endpoint associated with this cache.
     * @return
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public WindowsAzureEndpoint getEndpoint() throws WindowsAzureInvalidProjectOperationException {
        WindowsAzureEndpoint endPt =  null;
        try {
            List<WindowsAzureEndpoint> eplist = wRole.getEndpoints();
            for (WindowsAzureEndpoint waEp : eplist) {
                if(waEp.getName().equalsIgnoreCase("memcache_" + name)) {
                    endPt = waEp;
                    break;
                }
            }
            return endPt;
        }catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }

    /**
     * This API will give the backup option.
     * @return
     */
    public boolean getBackups(){
        return isBackup;
    }

    /**
     * This will sets the backup option
     * @param isbackup
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setBackups(boolean isbackup) throws WindowsAzureInvalidProjectOperationException {
        Element ele = getCachedNode();
        String backup ="0";
        if(isbackup) {
            backup = "1";
        }

        String newCache = JSONHelper.setParamValue(getCache(),
                "secondaries", backup);
        ele.setAttribute(WindowsAzureConstants.ATTR_VALUE, setCache(newCache));
        this.isBackup = isbackup;
    }

    /**
     * Gets time to live
     * @return
     */
    public int getMinutesToLive() {
        return minToLive;
    }

    /**
     * Sets time to live
     * @param minutes
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setMinutesToLive(int minutes) throws WindowsAzureInvalidProjectOperationException {
        if(minutes < 0) {
            new IllegalArgumentException(
                    WindowsAzureConstants.INVALID_ARG);
        }
        Element ele = getCachedNode();
        String newCache = JSONHelper.setParamValue(getCache(),
                "policy.expiration.defaultTTL", String.valueOf(minutes));
        ele.setAttribute(WindowsAzureConstants.ATTR_VALUE, setCache(newCache));
        minToLive = minutes;
    }

    /**
     * Returns the value of policy.expiration.type mapped to the enum.
     * @return
     */
    public WindowsAzureCacheExpirationPolicy getExpirationPolicy() {
        return expPolicy;
    }

    /**
     * Sets policy.expiration.type based on the enum constant.
     * @param policy
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void setExpirationPolicy(WindowsAzureCacheExpirationPolicy policy)
            throws WindowsAzureInvalidProjectOperationException {
        Element ele = getCachedNode();
        if (policy  == WindowsAzureCacheExpirationPolicy.NEVER_EXPIRES) {
            String newCache = JSONHelper.setParamValue(getCache(),
                    "policy.expiration.isExpirable", String.valueOf(policy.ordinal()));
            ele.setAttribute(WindowsAzureConstants.ATTR_VALUE, setCache(newCache));
        }
        String newCache = JSONHelper.setParamValue(getCache(),
                "policy.expiration.type", String.valueOf(policy.ordinal()));
        ele.setAttribute(WindowsAzureConstants.ATTR_VALUE, setCache(newCache));
        expPolicy = policy;
    }

    /**
     * This API is to delete the named cache
     * Throw exception if this is the “default” cache, because that cache cannot be deleted- Else:
     * remove the corresponding cache object declaration from the JSON (rewrite the JSON)
     * remove the corresponding endpoint
     * @throws WindowsAzureInvalidProjectOperationException
     */
    public void delete() throws WindowsAzureInvalidProjectOperationException {
        try {
            if(wRole.isCachingEnable() && getName().equalsIgnoreCase("default")) {
                throw new WindowsAzureInvalidProjectOperationException(
                        "Can not delete default cache");
            }
            getCachedNode().setAttribute("value", setCache(""));
            wRole.getNamedCaches().remove(getName());
            wRole.getEndpoint("memcache_" + getName()).delete();
        } catch (Exception ex) {
            throw new WindowsAzureInvalidProjectOperationException(
                    WindowsAzureConstants.EXCP, ex);
        }
    }
}
