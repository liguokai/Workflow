package com.biorad.wcms.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.w3c.dom.NodeList;

import com.interwoven.cssdk.access.CSUser;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSNameValuePair;
import com.interwoven.cssdk.common.xml.ElementableUtils;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.transform.XSLTransformer;
import com.interwoven.cssdk.workflow.CSComment;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSTransitionableTask;
import com.interwoven.cssdk.workflow.CSWorkflow;
import com.interwoven.sharedutils100.mail.MailConfigException;
import com.interwoven.sharedutils100.mail.Mailer;
import com.interwoven.sharedutils100.mail.MailerConfig;
import com.interwoven.ui.teamsite.workflow.task.TaskContext;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


@SuppressWarnings("unused")
public class EmailUtil {
	private static final Log LOGGER = LogFactory.getLog(EmailUtil.class);
	private static final String CONFIGPATH = "/app/Interwoven/TeamSite/local/config/wft/solutions/email_map.cfg";
	private static final String BASEPATH = "/app/Interwoven/TeamSite/custom/bio-rad/conf/xsl/";
	private static final String DELIMITER = ",";
	private static final String IWWEBD_HOST = "usherlx311.hdc.bio-rad.com";
	private static final String IWWEBD_PORT = "";
	public String config_to;
	public String config_from = "TeamSite@bio-rad.com";
	public String config_subject = "TeamSite Email Notificaton";
	public String config_body;
	public String config_server = "smtp.bio-rad.com";
	public String config_template = null;
	

	
	public void send() throws MessagingException, MailConfigException {
		LOGGER.debug( "Sending Email" );
		LOGGER.debug( "\tTo: " + config_to );
		LOGGER.debug( "\tFrom: " + config_from );
		LOGGER.debug( "\tSubject: " + config_subject );
		LOGGER.debug( "\tBody: " + config_body );
		// config_server = IWConfig.getConfig().getString("iwsend_mail", "mailserver");
		LOGGER.debug( "\tServer: " + config_server );
		LOGGER.debug( "\tTemplate: " + config_template );
		
		MailerConfig config = new MailerConfig();
		config.setHost( config_server );
		if( config_to.contains( "," ) ) {
			String[] pieces = config_to.split( "," );
			List<String> to = Arrays.asList( pieces );
			config.setToRecipients( to );
		} else {
			config.addToRecipient( config_to );
		}
		
		config.setSubject( config_subject );
		config.setSender( config_from );
		if( config_template != null ) {
			String body = getEmailBody( config_template, config_body );
			LOGGER.debug( "\tBody: \t" + body );
			config.addDataSource( new ByteArrayDataSource( body.getBytes() , "text/html" ) );
		} else {
			config.addDataSource(  new ByteArrayDataSource( config_body.getBytes(), "text/html") );
		}
		LOGGER.debug( "Email Body: \n" + config.getDataSources().toString() );
		Mailer mailer = new Mailer( config );
		mailer.send();
	}

	public String getEmailMapping( String name ) {
		LOGGER.debug( "Starting getEmailMapping" );
		LOGGER.debug( "\tName Passed: " + name );
		
		XPathUtil util = new XPathUtil();
		String emails = "";
		if( name.contains( DELIMITER ) ) {
			LOGGER.debug( "\tFound delimited Entry" );
			String[] names = name.split( DELIMITER );
			for( int i = 0; i < names.length; i++ ) {
				names[ i ] = names[ i ].trim();
				LOGGER.debug( "\t\t" + i + ": " + names[ i ] );
				String query = "/emailConf/user[@name = '" + names[ i ] + "']/@email";
				NodeList nodes = util.getXPathResults( CONFIGPATH, query );
				LOGGER.debug( "\t\tLook Up returned " + nodes.getLength() + " results" );
				if( nodes.getLength() < 1 ) {
					LOGGER.debug( "\t\tNo Value found, cannot add this user to notification" );
				} else {
					LOGGER.debug( "\t\tMapped to this user: " + nodes.item( 0 ).getNodeValue() );
					emails = emails + nodes.item( 0 ).getNodeValue() + ",";
				}
			}	
		} else {
			LOGGER.debug( "\tFound a single Entry" );
			String query = "/emailConf/user[@name = '" + name + "']/@email";
			NodeList nodes = util.getXPathResults( CONFIGPATH, query );
			if( nodes.getLength() < 1 ) {
				LOGGER.debug( "\t\tNo Value found, cannot add this user to notification" );
			} else {
				LOGGER.debug( "\t\tMapped to this user: " + nodes.item( 0 ).getNodeValue() );
				emails = emails + nodes.item( 0 ).getNodeValue() + "";
			}
		}
		LOGGER.debug( "\tReturning: " + emails );
		return emails;
	}
	
	public Element createContentXmlDoc(CSTask currentTask, CSTask targetTask) throws CSException {
		CSWorkflow job = currentTask.getWorkflow();

		Element mailContentRoot = ElementableUtils.newElement("MailContent");
		
		Element jobElement = mailContentRoot.addElement("Job");
		jobElement.addAttribute("description", job.getDescription());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Current Task ID: " + currentTask.getId());
		}
		// Fix iwov bug caused by trying to call toElement() on a task with CSHoles attached
		//Element currentTaskElement = currentTask.toElement("CurrentTask", ElementableUtils.OPTIONS_RECURSIVE_ALL);
		Element currentTaskElement = convertTaskToElement(currentTask, "CurrentTask");
		currentTaskElement.addAttribute("branchName", currentTask.getArea().getBranch().getName());
		currentTaskElement.addAttribute("areaName", currentTask.getArea().getName());
		jobElement.add(currentTaskElement);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Target Task ID: " + targetTask.getId());
		}
		// Fix iwov bug caused by trying to call toElement() on an unassigned group task
		//Element targetTaskElement = targetTask.toElement("TargetTask", ElementableUtils.OPTIONS_RECURSIVE_ALL);
		Element targetTaskElement = convertTaskToElement(targetTask, "TargetTask");
		if (targetTask.getKind() == CSTask.GROUP_TASK && "<no user>".equalsIgnoreCase(targetTask.getOwner().getName())) {
			targetTaskElement.remove(targetTaskElement.element("Owner"));
		}
		jobElement.add(targetTaskElement);

		// Web Host, for use as the base of all links in the email
		mailContentRoot.addElement("WebHost").addText(IWWEBD_HOST + IWWEBD_PORT);

		// Job priority
		String priority = job.getVariable("priority");
		if (priority != null) {
			jobElement.addAttribute("priority", priority);
		}
		
		// Due Date, if set
		String strDueDate = job.getVariable("due_date");
		if (strDueDate != null) {
			Date dueDate = TaskContext.parseDate(strDueDate);
			if (dueDate != null) {
				jobElement.add(ElementableUtils.toElement("DueDate", dueDate));
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Job variable due_date=[" + strDueDate
							+ "] in job id=[" + job.getId()
							+ "] could not be parsed.");
				}

				jobElement.addElement("DueDate");
			}
		}
		
		// Job owner
		CSUser jobOwner = job.getOwner();
		jobElement.add(jobOwner.toElement("Owner"));

		// Job comments (same as task comments?)
		CSComment[] jobComments = job.getComments();
		if (jobComments != null) {
			Element jobCommentsElement = jobElement.addElement("Comments");
			for (int i = 0; i < jobComments.length; ++i) {
				jobCommentsElement.add(jobComments[i].toElement());
			}
		}

		return mailContentRoot;
	}
	
	private Element convertTaskToElement(CSTask task, String elementName) throws CSException {
		LOGGER.debug("Converting task to Element: " + task.getKindName() + "(" + task.getId() + ")");
		Element ele = ElementableUtils.newElement(elementName, this);
		try {
			// Base properties
			ele.addAttribute("id", Integer.toString(task.getId()));
			ele.addAttribute("type", task.getKindName());
			ele.addAttribute("jobId", Integer.toString(task.getWorkflowId()));
			ele.addAttribute("active", Boolean.toString(task.isActive()));
			ele.addAttribute("lock", Boolean.toString(task.isAcquireLocksSet()));
			ele.addAttribute("needsAttention", task.getNeedsAttention());
			ele.addElement("Name").setText(task.getName());
			
			// This check pretty much only takes out invalid tasks and dummy tasks
			if (task.getArea() != null) {
				// Additional base properties
				ele.addElement("AreaVpath").setText(task.getArea().getVPath().getPathNoServer().toString());
				ele.addElement("Description").setText(task.getDescription());
				Date activationDate = task.getActivationDate();
				if (activationDate != null) {
					ele.add(ElementableUtils.toElement("ActivationDate",activationDate));
				}

				// this is the bit that fails when the Owner is not set
				//ele.add(task.getOwner().toElement("Owner"));
				if (task.getOwner().isValid()) {
					ele.add(task.getOwner().toElement("Owner"));
				}

				// Task variables
				CSNameValuePair[] variables = task.getVariables(null);
				Element variablesElement = ele.addElement("Variables");
				for (int i = 0; i < variables.length; ++i) {
					Element varElement = variablesElement.addElement("Variable");
					varElement.addElement("Name").setText(variables[i].getName());
					varElement.addElement("Value").setText(variables[i].getValue());
				}

				// Task files
				Element filesElement = ele.addElement("Files");
				CSAreaRelativePath[] paths = task.getFiles();
				LOGGER.debug("File count: " + paths.length);
				for (int i = 0; i < paths.length; ++i) {
					CSFile file = task.getArea().getFile(paths[i]);
					Element fileElement = file.toElement("File");
					filesElement.add(fileElement);
					Element commentsElement = fileElement.addElement("Comments");
					if (file.getKind() == CSHole.KIND) {
						LOGGER.debug("File is a hole, skipping comments: " + file.getVPath().toString());
						continue;
					}
					// this is the call that fails if files[i] instanceof CSHole
					CSComment[] comments = task.getFileComments(file);
					if (comments != null) {
						for (int j = 0; j < comments.length; ++j) {
							commentsElement.add(comments[j].toElement("Comment"));
						}
					}
				}

				// Task Comments
				CSComment[] taskComments = task.getComments();
				Element commentsElement = ele.addElement("Comments");
				for (int i = 0; i < taskComments.length; ++i)
					commentsElement.add(taskComments[i].toElement("Comment"));
				
				// Possible Transitions
				CSTransitionableTask tTask = (CSTransitionableTask) task;
				String[] transitions = tTask.getTransitions();
				Element transitionElement = ele.addElement( "Transitions" );
				for ( int i = 0; i < transitions.length; i++ ) {
					Element tElement = transitionElement.addElement( "Transition" );
					tElement.addText( transitions[ i ] );
				}
				
			}
			
		} catch (CSException e) {
			throw new RuntimeException("Error while communicating with TeamSite server: " + e.getMessage(), e);
		}

		return ele;
	}
	
	private String getEmailBody( String xslTemplate, String contentXml ) {
		InputStream inputStream = new  ByteArrayInputStream( contentXml.getBytes() );
		OutputStream outputStream = new ByteArrayOutputStream();
		InputStream templateFile = null;
		try {
			templateFile = new FileInputStream( BASEPATH + xslTemplate );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			XSLTransformer.transform(inputStream, templateFile, outputStream);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outputStream.toString();
	}
	
}
