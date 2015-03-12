package com.biorad.wcms.workflow.tasks;

import java.util.Hashtable;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.biorad.wcms.utils.EmailUtil;
import com.biorad.wcms.utils.TaskWrapper;
import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSObjectNotFoundException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.workflow.CSInactiveTaskException;
import com.interwoven.sharedutils100.mail.MailConfigException;

public class NotificationTask extends BaseExternalTask
{
 
	private static final Log LOGGER = LogFactory.getLog(NotificationTask.class);

	// Some variables to handle the task transition
	private String transitionName = null;
	private String transitionDescription = null;
	private Boolean taskErrored = false;
	private String errorDescription = "Some Generic Error Message";
	  
	@Override
	public void execute(CSClient client, TaskWrapper task, Hashtable<String, String[]> params) throws CSException {
	
		  // Start the task with a debug
		  LOGGER.debug( "Starting Notification Task [" + task.getTask().getId() + " - " + task.getTask().getWorkflowId() + " - " + task.getTask().getName() + "]" );
	
		  String disabled = task.getTask().getVariable( "disabled" );
		  
		  if ( disabled.equals( "true" ) ) {
			  LOGGER.debug( "Not sending email, it has been disabled for this task" );
			  transitionName = "Continue";
			  transitionDescription = "No notification sent, disabled via workflow";
		  } else {
			  if( task.getTask().getVariable( "to" ).length() > 0 ) {
				  // Set up the Email Utility
				  EmailUtil util = new EmailUtil();
				  String to_name = task.getTask().getVariable( "to" );
				  String template = task.getTask().getVariable( "mail_template" );
				  Element bodyElement = util.createContentXmlDoc( task.getTask(), task.findTargetTask() );
				  util.config_to = util.getEmailMapping( to_name );
				  util.config_subject = "[CMS: " + task.getTask().getWorkflowId() + "-" + task.getTask().getId() + "] " + task.getTask().getName().replace( "Notification: ", "" ) + ": " + task.getTask().getWorkflow().getDescription() + " - " + task.getTask().getFiles().length + " files"; 
				  util.config_body = bodyElement.asXML();
				  util.config_template = template;
				  try {
					  util.send();
				  } catch (MessagingException e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  } catch (MailConfigException e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  }			  
				  transitionName = "Continue";
				  transitionDescription = "Email Notification Sent";
			  } else {
				  transitionName = "Continue";
				  transitionDescription = "No notification sent, no recipients set during workflow initiation";	  
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
