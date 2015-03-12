package com.biorad.wcms.workflow.datasources;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.biorad.wcms.utils.CSClientUtil;

import com.interwoven.cssdk.access.CSAuthenticationException;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.access.CSGroup;
import com.interwoven.cssdk.access.CSInvalidSessionStringException;
import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.factory.CSFactoryInitializationException;

import com.interwoven.datasource.MapDataSource;
import com.interwoven.datasource.core.DataSourceContext;
import com.biorad.wcms.utils.*;

public class GroupListDataSource implements MapDataSource {

	private static final Log LOGGER = LogFactory.getLog(GroupListDataSource.class);
	private static String PATH_TSGROUPS = "/app/Interwoven/TeamSite/conf/tsgroups.xml";
	private static final String CONFIGPATH = "/app/Interwoven/TeamSite/local/config/wft/solutions/email_map.cfg";
	
	public Map<String, String> execute( DataSourceContext context ) {
		// Start the task with a debug
		LOGGER.debug( "Starting a GroupListDataSource" );
	
		String sessionId = context.getSessionId();
		String vpath = context.getServerContext();
		Map<String,String> params = context.getAllParameters();
		String type = params.get( "Type" );
		String groupName = "";
		
		LOGGER.debug( "\tSession Id: " + sessionId );
		LOGGER.debug( "\tVPath: " + vpath );
		LOGGER.debug( "\tType: " + type );
		
		if( ( type.equals( "CountryEditor" ) ) && ( !vpath.contains( "/templatedata/internet/taxonomy" ) ) ) {
			String country = getCountryFromVPath( vpath );
			LOGGER.debug( "\tCountry: " + country );
			groupName = country + "-Contributors";			
		}
		if( ( type.equals( "CountryEditor" ) ) && ( vpath.contains( "/templatedata/internet/taxonomy" ) ) ) {
			groupName = "Taxonomy-Contributors";
		}
		
		if( type.equals( "HQEditor" ) ) {
			groupName = "HQ-Contributors";			
		}
		if( type.equals( "HQMigrator" ) ) {
			groupName = "HQ-Migrators";			
		}

		
		LOGGER.debug( "\tGroup Name: " + groupName );
		
		// Set up the results map
		Map<String,String> results = new HashMap<String, String>();
		
		// Get a client
		LOGGER.debug( "\tAttempting to get a client" );
		CSClient client = null;

		try {
			client = CSClientUtil.getCSClient( (String)sessionId, params );
		} catch (CSInvalidSessionStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSFactoryInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSAuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if( client != null ) {
			CSGroup group = null;
			try {
				group =	client.getGroup( groupName, true );
			} catch (CSAuthorizationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CSExpiredSessionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CSRemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( group != null ) {
				try {
					CSIterator users = group.getUsers( true );
					while( users.hasNext() ) {
						CSUser user = (CSUser)users.next();
						if( user.getDisplayName() != null ) {
							results.put( user.getShortName(), user.getDisplayName() );
						} else {
							results.put( user.getShortName(), user.getShortName() );
						}	
					}
					
				} catch (CSAuthorizationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CSRemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CSExpiredSessionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else {
				results.put( "", "Could not find group: " + groupName );			
			}
		} else {
			XPathUtil utils = new XPathUtil();
			NodeList userIds = utils.getXPathResults( PATH_TSGROUPS, "/iwgroups/iwgroup[@name = '" + groupName + "']/user/@name" );
			for( int i = 0; i < userIds.getLength(); i++ ){
				String value = userIds.item(i).getNodeValue();
				String query = "/emailConf/user[@name = '" + value + "']/@fullname";
				NodeList nameNodes = utils.getXPathResults( CONFIGPATH, query );
				LOGGER.debug( "\tValue: " + value );
				LOGGER.debug( "\t\tQuery: " + query );
				if( nameNodes != null ) {
					if( nameNodes.getLength() > 0 ) {
						String label = nameNodes.item( 0 ).getNodeValue();
						LOGGER.debug( "\t\t" + i + " label: " + label + " value: " + value );
						results.put( value , label );
					} else {
						LOGGER.debug( "\tWas unable to find a fullname for " + value );
						results.put( value, value );
					}
				} else {
					LOGGER.debug( "\tWas unable to find a fullname for " + value );
					results.put( value, value );
				}
			}  
		}
		return results;
	}
	
	public String getCountryFromVPath( String vPath ) {
		String result = "";
		if( vPath.contains( "webroot" ) ) {
			if( vPath.contains( "webroot/web/messages") ) {
				result = "HQResourcebundle";
			} else {
				result = "HQWebroot";				
			}
		} else {
			String[] parts = vPath.split( "/" );
			result = parts[ 12 ];			
		}
		return result;
	}
}
