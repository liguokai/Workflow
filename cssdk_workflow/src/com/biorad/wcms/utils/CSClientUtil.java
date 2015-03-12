package com.biorad.wcms.utils;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.access.CSAuthenticationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.access.CSInvalidSessionStringException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.factory.CSFactory;
import com.interwoven.cssdk.factory.CSFactoryInitializationException;
import com.interwoven.serverutils100.InstalledLocations;

/**
* Utility class with a method to retrieve the CSClient object
* from a session string corresponding to a user's session on the TeamSite
* server.
*/
public class CSClientUtil {
	private static final Log LOGGER = LogFactory.getLog(CSClientUtil.class);

	public static final String SERVERNAME = "servername";
	public static final String SERVICEBASEURL = "serviceBaseURL";
	public static final String CSFACTORY = "csFactory";
	public static final String LOCALE = "locale";
	public static final String APPLICATION_CONTEXT = "appcontext";

	/**
	* Retrieves the CSClient object for the given sessionString
	* @param sessionString
	* @param param should contain the following parameters as keys
	* servername
	* serviceBaseURL
	* csFactory
	* locale [Optional]
	* appcontext [Optional]
	* @return
	* @throws CSInvalidSessionStringException
	* @throws CSExpiredSessionException
	* @throws CSAuthenticationException
	* @throws CSRemoteException
	* @throws CSException
	* @throws CSFactoryInitializationException
	*/
	public static CSClient getCSClient(String sessionString, Map param) throws CSInvalidSessionStringException, CSExpiredSessionException, CSAuthenticationException, CSRemoteException, CSException, CSFactoryInitializationException {

		// Start the task with a debug
		LOGGER.debug( ">> Starting getCSClient()" );
		LOGGER.debug( "session string :" + sessionString );
		LOGGER.debug( "map is :" + param );
		
		Locale locale = null;
		String serverName = null;
		CSFactory factory = null;
		String appcontext = null;
		//Read the properties from the Map and set it to the Properties object
		Properties props = new Properties();
		//TeamSite server name
		serverName = (String)param.get(SERVERNAME);
		//Read Service base URL; it should be of the format
		// http://<servername>:<port>
		//or
		// https://<servername>:<port>
		props.setProperty(SERVICEBASEURL, (String)param.get(SERVICEBASEURL));
		//Set the CSFactory object to be used
		//For example: com.interwoven.cssdk.factory.CSLocalFactory
		props.setProperty("com.interwoven.cssdk.factory.CSFactory",
		(String)param.get(CSFACTORY));
		//Get the CSFactory
		factory = CSFactory.getFactory(props);
		if( param.get(LOCALE) != null ) {
			locale = (Locale)param.get(LOCALE);
		}else {
			locale = Locale.getDefault();
		}
		if( param.get(APPLICATION_CONTEXT) != null ){
			appcontext = (String)param.get(APPLICATION_CONTEXT);
		}else {
			appcontext = "workflowModeler";
		}
		//Get the CSClient object
		return factory.getClient( sessionString, locale, appcontext, serverName );
	}

	
	public static CSClient getClientFromSession(HttpServletRequest request) throws CSException {
		Object iwclient = request.getAttribute("iw.csclient");
		if (iwclient != null && ((CSClient)iwclient).isValid()) {
			return (CSClient) iwclient;
		}
		return getClientFromSession(getSessionId(request));
    }

	/**
	 * Retrieves a CSSDK client object from the session ID
	 * passed along with the HTTP request
	 * 
	 * @param sessionid the Java session ID string
	 * @return a CSClient object
	 * @throws CSException if there's a connectivity or permissions problem
	 */
	public static CSClient getClientFromSession(String sessionid) throws CSException {
		CSClient rval = null;
		Properties localProperties = new Properties();
		localProperties.setProperty("cssdk.cfg.path", InstalledLocations.getIWHome() + "/cssdk/cssdk.cfg");
		CSFactory localFactory = (CSFactory) CSFactory.getFactory(localProperties);
		rval = localFactory.getClient(sessionid, Locale.getDefault(), "TeamSite", null);
		return rval;
    }

	private static String getSessionId(HttpServletRequest request) {
		Cookie cookies[] = request.getCookies();
		for (Cookie c : cookies) {
			if (c.getName().equals("IWAUTH")) {
				return c.getValue();
			}
		}
		return "";
	}

}
