package com.biorad.wcms.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.biorad.wcms.utils.XPathUtil;

public class AttachAll {
    private static final Log LOGGER = LogFactory.getLog( AttachAll.class );
    public String dcr;
	private String PATHTOCONFIG = "/app/Interwoven/TeamSite/custom/bio-rad/conf/attachall.cfg";
	public String status_parsable = "true";
	public String status_foundJS = "false";
	public String status_foundDCR = "true";;
	public String html = "";
	
    public void find() {
    	LOGGER.debug( ">> Starting AttachAll Action" );
    	// Add iwmnt to path if required
    	if( !dcr.startsWith( "/iwmnt" ) ) {
    		dcr = "/iwmnt" + dcr;
    	}
    	LOGGER.debug( "\tDCR: " + dcr );

    	// Get the list of queries
		XPathUtil util = new XPathUtil();
		int count = 0;
		NodeList queries = util.getXPathResults( this.PATHTOCONFIG, "/queries/query" );
    	LOGGER.debug( "\tThere are " + queries.getLength() + " queries to execute" );
    	
    	// Loop through each of the queries
    	for( int i = 0; i < queries.getLength(); i++ ) {
			String query = queries.item( i ).getTextContent();
			if( status_parsable.equals( "true" ) ){
				LOGGER.debug( "\t\tQuery " + i + ": " + query );
				try {
					NodeList results = util.getXPathResults( dcr , query, true );
					for( int j = 0; j < results.getLength(); j++ ) {
						String result = results.item( j ).getTextContent();
						if( result.contains( "javascript" ) ){
							status_foundJS = "true";
							// Handle single quotes 1st
							if( result.contains( "'" ) ) {
								String[] parts = result.split( "'" );
								result = parts[ 1 ];
							}
							// Handle double quotes
							if( result.contains( "\"" ) ) {
								String[] parts = result.split( "\"" );
								result = parts[ 1 ];					
							}
							// Handles escaped double quotes
							if( result.contains( "&quot;" ) ) {
								String[] parts = result.split( "&quot;" );
								result = parts[ 1 ];					
							}
						} 
						html = html + "<li>" +  result + "</li>";
						LOGGER.debug( "\t\t\tFound #" + count + ": " + result );
						count++;
					}				
				} catch( Exception e ){
					LOGGER.debug( "\t\tFailed to process: " + dcr );
					status_parsable = "false";
				}
			}
		}	
    	LOGGER.debug( "<< Ended AttachAll Action" );
    }
    
    public String parseAsHTML() {
    	LOGGER.debug( ">> Starting parseAsHTML()" );
    	String result = "<ul>";
    	LOGGER.debug( "\tParsable: " + status_parsable );
    	if ( status_parsable.equals( "true" ) ) {
    		result = result + html;
    	} else {
    		result = result + "<li>Was unable to parse this record</li>";    		
    	}
    	result = result + "</ul>";
    	LOGGER.debug( result );
    	LOGGER.debug( "<< Ending parseAsHTML()" );
    	return html;
    }
    
    
}
