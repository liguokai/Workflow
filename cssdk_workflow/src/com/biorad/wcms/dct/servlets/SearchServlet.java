package com.biorad.wcms.dct.servlets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.biorad.wcms.utils.CSClientUtil;
import com.biorad.wcms.utils.XPathUtil;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.filesys.CSArea;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.search.*;



@SuppressWarnings("unused")
public class SearchServlet {
    private static final long serialVersionUID = -3679688130723814119L;
    private static final Log LOGGER = LogFactory.getLog( SearchServlet.class );
    
    private int SEARCH_PAGESIZE = 100;
    
    public String datatype = "";
    public String constraint = "";
    public String wa = "";
    public String linkDCR = "";
    
    public String PATHTOCONFIG = "/app/Interwoven/TeamSite/custom/bio-rad/conf/forms.search.cfg";
    public String PATHTOEDITDCR = "/iw-cc/command/iw.group.ccpro.edit?vpath=";
    
    public String execute( HttpServletRequest request ) throws CSException {
    	LOGGER.debug( ">> Starting a search execution" );
    	String results = "<ul>";
    
    	LOGGER.debug( "\tGetting Client" );
    	CSClient client = CSClientUtil.getClientFromSession( request );
    	LOGGER.debug( "\tClient gotten" );
    	
    	LOGGER.debug( "\tDataType: " + this.datatype );
    	LOGGER.debug( "\tTerms: " + this.constraint );
    	LOGGER.debug( "\tWorkarea: " + this.wa );
    	LOGGER.debug( "\tLink To DCR: " + this.linkDCR );
    	
    	LOGGER.debug( "\tDoing Transformations" );
    	String[] dct = this.datatype.split( "," );
    	String[] terms = this.constraint.split( " " );
    	CSArea[] workareas = { client.getWorkarea( new CSVPath( this.wa ), true ) };
    	
    	LOGGER.debug( "\tTransformations complete" );
    	LOGGER.debug( "\tBuilding Constraint" );
    	for ( int i = 0; i < terms.length; i++ ) {
    		terms[ i ] = "*" + terms[ i ] + "*";    		
    	}
    	
    	CSConstraint query = new CSAndConstraint(new CSConstraint[] {
    			new CSFieldConstraint( "TeamSite/Templating/DCR/Type", new CSContainsAllConstraint( dct ) ),
    			new CSContainsAnyConstraint( terms )
    	});
    	
    	LOGGER.debug( "\tConstraint Built" );
    	LOGGER.debug( "\tGetting Search Engine" );
    	
    	CSSearchEngine se = client.getSearchEngine();
    	
    	LOGGER.debug( "\tSearch Engine Retrieved" );
    	LOGGER.debug( "\tBranch Diagnostic" );
    	try {
    		String branches[] = se.getIndexedBranches();
    		for( int i = 0; i < branches.length; i++ ) {
    			LOGGER.debug( "\t\t" + branches[ i ] );    		
    		}
    	} catch ( CSException e ) {
    		LOGGER.debug( "ERROR: " + e.getMessage() );
    	}
    	
	
    	LOGGER.debug( "\tExecuting Query" );
    	
    	CSSearchResult sr = se.search( workareas, query, SEARCH_PAGESIZE );
  
    	LOGGER.debug( "\tQuery Executed" );

    	if( sr != null ){
    		CSSearchRecord[] records = sr.getRecords();

    		for( int i = 0; i < records.length; i++ ) {

	    		CSSearchRecord item = records[ i ];
	    		String vpath		= "/iwmnt" + item.getFile().getVPath().getPathNoServer().toString();
	    		if( vpath.endsWith( ".xml" ) && vpath.contains( this.datatype ) ) {
		    		String label 		= getLabel( vpath );
		    		String value		= getValue( vpath );
		    		String path			= item.getFile().getVPath().getAreaRelativePath().toString();
		    		LOGGER.debug( "\t\t" + vpath );
		    		if( this.linkDCR.equals( "true" ) ) {
		    			results = results + "<li><input type='radio' name='file' vpath='" + path + "' value='" + value  + "' id='file" + i + "' /><label for='file" + i + "' class='iw-base-listview-data-nowrap'><a href='" + PATHTOEDITDCR + vpath.replace( "/iwmnt", "" ) + "' target='_blank'>" + label + "</a></label></li>";		    			
		    		} else {
		    			results = results + "<li><input type='radio' name='file' vpath='" + path + "' value='" + value  + "' id='file" + i + "' /><label for='file" + i + "' class='iw-base-listview-data-nowrap'>" + label + "</label></li>";
		    		}
	    		} else {
	    			LOGGER.debug( "\t\tNon XML File, skipping (" + vpath + ")" );
	    		}

	    	}
    	} else {
    		results = "<li class='iw-base-listview-data-nowrap'>No Results</li>";
    	}
    	results = results + "</ul>";
    	LOGGER.debug( "<< Ending a search execution" );
    	return results;
    }

    public String getLabel( String vpath ) {
    	LOGGER.debug( ">> getLabel()" );
    	String query = "/settings/search[@datatype = '" + datatype + "' ]/@label";
    	LOGGER.debug( "\tvpath: " + vpath );
    	LOGGER.debug( "\tquery: " + query );
     	String result = "";

    	XPathUtil util = new XPathUtil();
    	NodeList labels = util.getXPathResults( this.PATHTOCONFIG, query );
    	LOGGER.debug( "\tResult Count: " + labels.getLength() );
    	if( labels.getLength() > 0 ) {
    		LOGGER.debug( "\tConfig found, using value from DCR" );
    		String dcrQuery	= labels.item( 0 ).getTextContent();
    		LOGGER.debug( "\tDCR Query: " + dcrQuery );
    		NodeList label = util.getXPathResults( vpath, dcrQuery );
    		result = label.item( 0 ).getTextContent();
    	} else {
    		LOGGER.debug( "\tNo config found, using filename" );
    		CSVPath vPath = new CSVPath( vpath );
    		result = vPath.getName().toString();
    	} 	
    	LOGGER.debug( "\tResult: " + result );
    	LOGGER.debug( "<< getLabel()" );
    	return result;
    }
    
    public String getValue( String vpath ) {
    	LOGGER.debug( ">> getValue()" );
    	String query = "/settings/search[@datatype = '" + datatype + "' ]/@value";
    	LOGGER.debug( "\tvpath: " + vpath );
    	LOGGER.debug( "\tquery: " + query );
    	XPathUtil util = new XPathUtil();
    	NodeList labels = util.getXPathResults( this.PATHTOCONFIG, query );
    	LOGGER.debug( "\tResult Count: " + labels.getLength() );
    	String result = "";
    	if( labels.getLength() > 0 ) {
    		LOGGER.debug( "\tConfig found, using value from DCR" );
    		String dcrQuery	= labels.item( 0 ).getTextContent();
    		LOGGER.debug( "\tDCR Query: " + dcrQuery );
    		NodeList label = util.getXPathResults( vpath, dcrQuery );
    		result = label.item( 0 ).getTextContent();
    	} else {
    		LOGGER.debug( "\tNo config found, using filepath" );
    		CSVPath vPath = new CSVPath( vpath );
    		result = vPath.getPathNoServer().toString();
    	} 	
    	LOGGER.debug( "\tResult: " + result );
    	LOGGER.debug( "<< getLabel()" );
    	return result;
    }
}
