package com.biorad.wcms.workflow.datasources;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.biorad.wcms.utils.XPathUtil;
import com.interwoven.datasource.MapDataSource;
import com.interwoven.datasource.core.DataSourceContext;

public class JobNotificationDataSource implements MapDataSource {

	private static final Log LOGGER = LogFactory.getLog(JobNotificationDataSource.class);
	private static final String CONFIGPATH = "/app/Interwoven/TeamSite/local/config/wft/solutions/email_map.cfg";
	
	public Map<String, String> execute(DataSourceContext context) {
		// Start the task with a debug
		LOGGER.debug( "Starting a JobNotificationDataSource" );
		
		String sessionId = context.getSessionId();
		String vpath = context.getServerContext();
		Map<String,String> params = context.getAllParameters();
		String type = params.get( "Type" );
		
		LOGGER.debug( "\tSession Id: " + sessionId );
		LOGGER.debug( "\tVPath: " + vpath );
		LOGGER.debug( "\tType: " + type );
		
		String CONFIGQUERY = "/emailConf/group[@name = '" + type + "']/user/@name";
		
		// Set up the results map
		Map<String,String> results = new HashMap<String, String>();
		
		// Get the properties document
		XPathUtil util = new XPathUtil();
		NodeList nodes = util.getXPathResults( CONFIGPATH, CONFIGQUERY );
		if( nodes != null ) {
			LOGGER.debug( "Got " + nodes.getLength() + " nodes back" );
			for (int i = 0; i < nodes.getLength(); i++) {
				String value = nodes.item(i).getNodeValue();
				String query = "/emailConf/user[@name = '" + value + "']/@fullname";
				NodeList nameNodes = util.getXPathResults( CONFIGPATH, query );
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
		} else {
			LOGGER.debug( "There was an issue getting the nodelist" );
		}
		return results;
	}
}
