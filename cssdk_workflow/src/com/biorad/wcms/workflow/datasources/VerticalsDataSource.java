package com.biorad.wcms.workflow.datasources;

import java.util.HashMap;
import java.util.Map;

import com.biorad.wcms.utils.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.interwoven.datasource.MapDataSource;
import com.interwoven.datasource.core.DataSourceContext;

public class VerticalsDataSource implements MapDataSource {
	
	private static final Log LOGGER = LogFactory.getLog(VerticalsDataSource.class);
	private static final String PROPERTIESPATH = "/app/Interwoven/TeamSite/custom/bio-rad/conf/dcrValues.properties";
	private static final String PROPERTIESQUERY = "/dct-properties/verticals/vertical";

	public Map<String, String> execute( DataSourceContext context ) {
		// Start the task with a debug
		LOGGER.debug( "Starting a VerticalsDataSource" );
		
		String sessionId = context.getSessionId();
		String vpath = context.getServerContext();
		
		LOGGER.debug( "\tSession Id: " + sessionId );
		LOGGER.debug( "\tVPath: " + vpath );

		// Set up the results map
		Map<String,String> results = new HashMap<String, String>();
		
		// Get the properties document
		XPathUtil util = new XPathUtil();
		NodeList nodes = util.getXPathResults( PROPERTIESPATH, PROPERTIESQUERY );
		if( nodes != null ) {
			LOGGER.debug( "Got " + nodes.getLength() + " nodes back" );
			for (int i = 0; i < nodes.getLength(); i++) {
				String value = nodes.item(i).getAttributes().getNamedItem( "value" ).getNodeValue();
				String label = nodes.item(i).getAttributes().getNamedItem( "name" ).getNodeValue();
				LOGGER.debug( "\t" + i + " label: " + label + " value: " + value );
				results.put( value , label );
			}
		} else {
			LOGGER.debug( "There was an issue getting the nodelist" );
		}

		return results;
	}

}
