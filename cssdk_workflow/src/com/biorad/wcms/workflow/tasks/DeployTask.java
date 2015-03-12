package com.biorad.wcms.workflow.tasks;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.biorad.wcms.utils.DeployUtil;
import com.biorad.wcms.utils.TaskWrapper;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.workflow.CSInactiveTaskException;

public class DeployTask extends BaseExternalTask {

	private static final Log LOGGER = LogFactory.getLog(DeployTask.class);

	// Some variables to handle the task transition
	private String transitionName = "Continue";
	private String transitionDescription = "Deployment Testing";
	private Boolean taskErrored = false;
	private String errorDescription = "Some Generic Error Message";
	
	public void execute(CSClient client, TaskWrapper task, Hashtable<String, String[]> params) throws CSException {

		LOGGER.debug( ">> Starting DeployTask" );
		CSAreaRelativePath[] files = task.getTask().getFiles();
		LOGGER.debug( "\tThere are " + files.length + " files currently attached" );
		// Generate any dcrs attached to the task
		CSAreaRelativePath[] genFiles = null;	
		for( int i = 0; i < files.length; i++ ) {
			String file = task.getTask().getArea().getVPath().getPathNoServer().toString() + "/" + files[ i ].toString();
			LOGGER.debug( "\t\t" + i + ": " + file );
			if( ( file.contains( "templatedata" ) && ( file.endsWith( "xml" ) ) ) ) {
				LOGGER.debug( "\t\tThis is a dcr" );
				CSFile source = client.getFile( new CSVPath( file ) );
				CSFile template = DeployUtil.getTplName( client, source );
				// This is a string instead of a CSFile, because it may not exist (the first time its deployed)
				String output = DeployUtil.getOutputName( client, source );
				LOGGER.debug( "\t\tTemplate: " + template.getVPath().toString() );
				LOGGER.debug( "\t\tOutput: " + output );

				try {
					CSVPath[] createdFiles = DeployUtil.iwptCompile( client, source, template, output );
					if( createdFiles != null ) {
						// Add the files to the list to be attached
						// genFiles[ genFiles.length ] = new CSVPath( output ).getAreaRelativePath();
					} else {
						taskErrored = true;
						errorDescription = "There was issues trying to generate the dcrs, see biorad.log for details";
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LOGGER.debug( "\t\tERROR: " + e.getMessage() );				
					e.printStackTrace();
				}
			} else {
				LOGGER.debug( "\tThis is not a dcr" );		
			}
		}
		// Attach the dcrs if there hasn't been any errors
		if(taskErrored != true ) {
			LOGGER.debug( "\tAttaching " + genFiles.length + " files" );
			task.getTask().attachFiles( genFiles );
			// Files are attached lets do the deployment
		}
		
		LOGGER.debug( ">> Ending DeployTask" );

		transition( task );
	}

  	public void transition( TaskWrapper task ) throws CSAuthorizationException, CSRemoteException, CSObjectNotFoundException, CSInactiveTaskException, CSExpiredSessionException, CSException {
		LOGGER.debug( ">> Starting transition()" );
		LOGGER.debug( "\tTransition Name: " + transitionName );
		LOGGER.debug( "\tTransition Description: " + transitionDescription );
		if( taskErrored ) {
			task.getTask().chooseTransition( "Fail", errorDescription );
		} else {
			task.getTask().chooseTransition( transitionName, transitionDescription );
		}
		LOGGER.debug( "<< Leaving transition()" );
  	}
	
}
