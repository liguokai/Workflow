package com.biorad.wcms.workflow.tasks;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biorad.wcms.utils.TaskWrapper;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.workflow.CSInactiveTaskException;


public class TransitionTask extends BaseExternalTask
{
	  private static final Log LOGGER = LogFactory.getLog(TransitionTask.class);
	  
	  // Some variables to handle the task transition
	  private String transitionName = null;
	  private String transitionDescription = null;
	  private Boolean taskErrored = false;
	  private String errorDescription = "Some Generic Error Message";
	 
	  @Override
	  public void execute(CSClient client, TaskWrapper task, Hashtable<String, String[]> params) throws CSException {
		  // Start the task with a debug
		  LOGGER.debug( "Starting Transition Task [" + task.getTask().getId() + " - " + task.getTask().getWorkflowId() + " - " + task.getTask().getName() + "]" );

		  String taskName = task.getTask().getName();
		  String EditorTwo = task.getTask().getVariable( "EditorTwo" );
		  String EditorThree = task.getTask().getVariable( "EditorThree" );
		  
		  // Process the logic for a transition from Editor One
		  if ( taskName.equals( "Process Next Step (Editor One)" ) ) {
			  if( EditorTwo.length() > 0 ) {
				  transitionName = "Editor Two";
				  transitionDescription = "Transitioning to Editor Two";
			  } else {
				  if( EditorThree.length() > 0 ) {
					  transitionName = "Editor Three";
					  transitionDescription = "Transitioning to Editor Three";				  				  
				  } else {
					  transitionName = "Editor Four";
					  transitionDescription = "Transitioning to Editor Four";
				  }
			  }
		  }
	
		  // Process the logic for a transition from Editor Two
		  if ( taskName.equals( "Process Next Step (Editor Two)" ) ) {
			  if( EditorThree.length() > 0 ) {
				  transitionName = "Editor Three";
				  transitionDescription = "Transitioning to Editor Three";				  				  
			  } else {
				  transitionName = "Editor Four";
				  transitionDescription = "Transitioning to Editor Four";
			  }
		  }
		  transition( task );
	  }
	  
	  /**
	   * Method to handle task transitions
	   * @param task	Current TaskWrapper object.
	   * @throws CSAuthorizationException
	   * @throws CSRemoteException
	   * @throws CSObjectNotFoundException
	   * @throws CSInactiveTaskException
	   * @throws CSExpiredSessionException
	   * @throws CSException
	   */
	  public void transition( TaskWrapper task ) throws CSAuthorizationException, CSRemoteException, CSObjectNotFoundException, CSInactiveTaskException, CSExpiredSessionException, CSException {
		  LOGGER.debug( ">> Starting transition()" );
		  LOGGER.debug( "\tTransition Name: " + transitionName );
		  LOGGER.debug( "\tTransition Description: " + transitionDescription );
		  if( taskErrored ) {
			  task.getTask().chooseTransition( "Error", errorDescription );
		  } else {
			  task.getTask().chooseTransition( transitionName, transitionDescription );
		  }
		  LOGGER.debug( "<< Leaving transition()" );
	  }
	  
}
