package com.biorad.wcms.workflow.tasks;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.biorad.wcms.utils.TaskWrapper;
import com.biorad.wcms.utils.XPathUtil;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSInactiveTaskException;

public class AttachFilesTask extends BaseExternalTask {
	
	private String PATHTOCONFIG = "/app/Interwoven/TeamSite/custom/bio-rad/conf/attachall.cfg";
	
	// Some variables to handle the task transition
	private String transitionName = null;
	private String transitionDescription = null;
	private String attachComments = "";
	private Boolean taskErrored = false;
	private String errorDescription = "Something went wrong with the attach all functionality";

	private static final Log LOGGER = LogFactory.getLog( AttachFilesTask.class );

	@Override
	public void execute( CSClient client, TaskWrapper task, Hashtable<String, String[]> params ) throws CSException {

		// Start the task with a debug
		LOGGER.debug( "Starting AttachFilesTask Task [" + task.getTask().getId() + " - " + task.getTask().getWorkflowId() + " - " + task.getTask().getName() + "]" );
		
		String runAttach = task.getTask().getVariable( "script_attachFiles" );
		if( runAttach.equals( "true" ) ) {
			CSAreaRelativePath[] files = task.getTask().getFiles();			
			for( int i = 0; i < files.length; i++ ) {
				LOGGER.debug( "Processing File: " + files[ i ] );
				if( isDCR( files[ i ].toString() ) ) {
					LOGGER.debug( "\tThis is a DCR, process" );
					processDCR( files[ i ], task );					
				} else {
					LOGGER.debug( "\tThis is not a DCR, ignore it" );
				}				
			}
			transitionName = "Continue";
			transitionDescription = "Attached files (see log for details)";
		} else {
			LOGGER.debug( "Not Attaching Dependant Files" ) ;
			transitionName = "Continue";
			transitionDescription = "Continuing to next step, not attaching dependant files";
		}
		transition( task );
	}

	public String[] getDependantFiles( CSAreaRelativePath vpath, CSWorkarea workarea ) {
		String files = "";
		LOGGER.debug( ">> getDependantFiles" );
		String path = "/iwmnt" + workarea.getVPath().getPathNoServer().toString() + "/" + vpath.toString();
		LOGGER.debug( "\t DCR Path: " + path );
		XPathUtil util = new XPathUtil();
		int count = 0;
		NodeList queries = util.getXPathResults( this.PATHTOCONFIG, "/queries/query" );
		for( int i = 0; i < queries.getLength(); i++ ) {
			String query = queries.item( i ).getTextContent();
			LOGGER.debug( "\t\tQuery " + i + ": " + query );
			try {
				NodeList results = util.getXPathResults( path , query, true );
				for( int j = 0; j < results.getLength(); j++ ) {
					String result = results.item( j ).getTextContent();
					if( result.contains( "javascript" ) ){
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
					LOGGER.debug( "\t\t\tFound #" + count + ": " + result );
					count++;
					if ( files.length() > 0 ) {
						files = files + "\n" + result;
					} else {
						files = files + result;
					}
					attachComments = attachComments + result + "\n";
				}				
			} catch( Exception e ){
				LOGGER.debug( "\t\t\tFailed to process: " + vpath.toString() );
				attachComments = attachComments + "**************************<br />" + "Failed to process: " + vpath.toString() + "<br />**************************<br />";				
			}
		}
		LOGGER.debug( "<< getDependentFiles" );
		String[] results = files.split( "\\n" );
		return results;
	}
	
	  public void transition( TaskWrapper task ) throws CSAuthorizationException, CSRemoteException, CSObjectNotFoundException, CSInactiveTaskException, CSExpiredSessionException, CSException {
		  LOGGER.debug( ">> Starting transition()" );
		  LOGGER.debug( "\tTransition Name: " + transitionName );
		  LOGGER.debug( "\tTransition Description: " + transitionDescription );
		  if( taskErrored ) {
			  task.getTask().addComment( attachComments );
			  task.getTask().chooseTransition( "Error", errorDescription );
		  } else {
			  task.getTask().chooseTransition( transitionName, transitionDescription + "\n\n" + attachComments );
		  }
		  LOGGER.debug( "<< Leaving transition()" );
	  }
	  
	  public boolean isDCR( String vpath ) {
		  if( ( vpath.startsWith( "templatedata" ) || vpath.startsWith( "/templatedata" ) ) && vpath.endsWith( "xml" ) ) {
			  return true;
		  } else {
			  return false;
		  }
	  }
	  
	  public boolean isFileAttached( CSAreaRelativePath file, TaskWrapper task ) throws CSAuthorizationException, CSRemoteException, CSObjectNotFoundException, CSExpiredSessionException, CSException {
		  CSAreaRelativePath[] files = task.getTask().getFiles();
		  LOGGER.debug( ">> isFileAttached" );
		  LOGGER.debug( "\tFile: " + file.toString() );
		  boolean found = false;
		  for( int i = 0; i < files.length; i++ ) {
			  LOGGER.debug( "\tAttached File " + i + ": " + files[ i ].toString() );
			  if ( files[ i ].equals( file ) ) {
				  found = true;
			  }
		  }
		  LOGGER.debug( "\tResult: " + found );
		  return found;		  
	  }
	  
	public void processDCR( CSAreaRelativePath file, TaskWrapper task ) throws CSAuthorizationException, CSRemoteException, CSObjectNotFoundException, CSExpiredSessionException, CSException {
		String[] filesToAttach = getDependantFiles( file, (CSWorkarea) task.getTask().getArea() );
		if( filesToAttach != null ) {
			for( int j = 0; j < filesToAttach.length; j++ ) {
				LOGGER.debug( "\tFound: " + filesToAttach[ j ] );
				String fullPath = "";
				if( filesToAttach[ j ].startsWith( "/" ) ) {
					fullPath = task.getTask().getArea().getVPath().getPathNoServer() + filesToAttach[ j ];
				} else {
					fullPath = task.getTask().getArea().getVPath().getPathNoServer() + "/" + filesToAttach[ j ];		
				}
				LOGGER.debug( "\tFull Path: " + fullPath );
				CSVPath vpath = new CSVPath( fullPath );
				boolean exists = (new File( "/iwmnt" + fullPath )).exists();
				LOGGER.debug( "\tThe file exists in the filesystem: " + exists );
				LOGGER.debug( "\tCSVPath Exists: " + vpath.toString() );
				CSAreaRelativePath[] filefound = { vpath.getAreaRelativePath() };
				if( ( isFileAttached( filefound[ 0 ], task ) == false ) && ( exists == true ) ) {
					if( filesToAttach[ j ].toString().startsWith( "/webroot" ) ) {
						LOGGER.debug( "\t\tIts a web root file - attach" );
						task.getTask().attachFiles( filefound );
						task.getTask().addFileComment( filefound[ 0 ] , "Web File found through 'Attach All'" );		
					}
					if( filesToAttach[ j ].toString().startsWith( "/templatedata" ) ){
						LOGGER.debug( "\t\tIts a dcr - attach and process" );
						task.getTask().attachFiles( filefound );
						task.getTask().addFileComment( filefound[ 0 ] , "DCR found through 'Attach All'" );
						processDCR( filefound[ 0 ], task );
					}
					if( filesToAttach[ j ].toString().startsWith( "/properties" ) ){
						LOGGER.debug( "\t\tIts a properties file - attach" );
						task.getTask().attachFiles( filefound );
						task.getTask().addFileComment( filefound[ 0 ] , "Properties file through 'Attach All'" );								
					}	
					if( filesToAttach[ j ].toString().startsWith( "webroot" ) ) {
						LOGGER.debug( "\t\tIts a web root file - attach" );
						task.getTask().attachFiles( filefound );
						task.getTask().addFileComment( filefound[ 0 ] , "Web File found through 'Attach All'" );		
					}
					if( filesToAttach[ j ].toString().startsWith( "templatedata" ) ){
						LOGGER.debug( "\t\tIts a dcr - attach and process" );
						task.getTask().attachFiles( filefound );
						task.getTask().addFileComment( filefound[ 0 ] , "DCR found through 'Attach All'" );
						processDCR( filefound[ 0 ], task );
					}
					if( filesToAttach[ j ].toString().startsWith( "properties" ) ){
						LOGGER.debug( "\t\tIts a properties file - attach" );
						task.getTask().attachFiles( filefound );
						task.getTask().addFileComment( filefound[ 0 ] , "Properties file through 'Attach All'" );								
					}	
				} else {
					LOGGER.debug( "\t\tThe file has already been attached - skipping" );
				}
			}
		} else {
			LOGGER.debug( "\tNo files found" );
		}  
	  }
	
	public void utilityAttachFiles( TaskWrapper task, CSAreaRelativePath[] filefound ){
		
		try {
			task.getTask().attachFiles( filefound );
		} catch (CSAuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSRemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSObjectNotFoundException e) {
			// TODO Auto-generated catch block
			errorDescription = errorDescription + filefound[ 0 ] + " not found\n";
			e.printStackTrace();
		} catch (CSInactiveTaskException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSExpiredSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}


