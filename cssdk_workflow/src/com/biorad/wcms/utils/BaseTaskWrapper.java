package com.biorad.wcms.utils;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSIterator;
import com.interwoven.cssdk.common.query.CSQuery;
import com.interwoven.cssdk.common.query.CSSelector;
import com.interwoven.cssdk.common.query.CSSelectorAttribute;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.filesys.CSDir;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.interwoven.cssdk.filesys.CSWorkarea;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSTransitionableTask;
import com.interwoven.cssdk.workflow.CSWorkflow;




/**
 * Base implementation of TaskWrapper that applies to any type of
 * CSTransitionableTask.
 */
public class BaseTaskWrapper implements TaskWrapper {
   
	private static final Log LOGGER = LogFactory.getLog(BaseTaskWrapper.class);

    /**
     * The underlying task object
     */
    protected CSTransitionableTask task;

    /**
     * Builds a new wrapper around the task object
     * 
     * @param task
     *            the task to wrap
     */
    public BaseTaskWrapper(CSTransitionableTask task) {
    	this.task = task;
    }

    /**
     * Builds a new wrapper, attempting to resolve the
     * task on behalf of the caller
     * @param client the CSSDK client
     * @param taskid the task ID as a String
     * @throws NumberFormatException if the task ID can't be parsed as a number
     * @throws CSException if there's a connectivity or permissions issue
     */
    public BaseTaskWrapper(CSClient client, String taskid) throws NumberFormatException, CSException {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug( ">> BaseTaskWrapper()" );
		}
		CSTask t = client.getTask( Integer.valueOf( taskid ) );
		if (t == null) {
		    throw new IllegalArgumentException( "Cannot resolve task with ID: " + taskid );
		}
		if (CSTransitionableTask.class.isAssignableFrom(t.getClass())) {
		    throw new UnsupportedOperationException( "This interface does not support non-transitionable tasks. Given: " + t.getClass().getSimpleName() );
		}
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug( "<< BaseTaskWrapper()" );
		}
    }

  
    public CSTask findTargetTask() throws CSException {
		String taskName = task.getVariable( TASK_VAR_TARGET_TASK );
		if (taskName == null || taskName.trim().length() == 0) {
		    throw new IllegalArgumentException( "Required task variable not specified: " + TASK_VAR_TARGET_TASK );
		}
		try {
		    return findTaskByName(taskName);
		} catch (IllegalArgumentException e) {
		    throw new IllegalArgumentException( "Could not find a task with name " + taskName + " as specified by task variable " + TASK_VAR_TARGET_TASK );
		}
    }

    private CSTask findTaskByName( String taskName ) throws CSException {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug( "Looking in job for task named: " + taskName );
		}

		if (taskName == null || taskName.trim().length() == 0) {
		    throw new IllegalArgumentException("Required task name improperly specified");
		}
		CSTask[] tasks = task.getWorkflow().getTasks();
		for (int i = 0; i < tasks.length; i++) {
		    if (taskName.equals(tasks[i].getName())) {
			if (LOGGER.isDebugEnabled()) {
			    LOGGER.debug( "Task found: " + tasks[i].toString() );
			}
			return tasks[i];
		    }
		}

		// Should have returned before now
		if (LOGGER.isErrorEnabled()) {
		    LOGGER.error( "No task found with name: " + taskName );
		}
		throw new IllegalArgumentException("Could not find a task with name: " + taskName);
    }


    public void execFirstTransition(String comment) throws CSException {
		String[] transitions = task.getTransitions();
		if (transitions.length == 0) {
		    LOGGER.error( "Task has no available transitions" );
		    throw new IllegalArgumentException( "Task has no available transitions!" );
		}
		task.chooseTransition(transitions[0], comment);
		LOGGER.info( comment );
    }


    public CSTransitionableTask getTask() {
    	return task;
    }

    public CSAreaRelativePath[] getFilesNoDirs(CSClient client) throws CSException {
		List<CSAreaRelativePath> result = new ArrayList<CSAreaRelativePath>();
	
		CSAreaRelativePath[] paths = task.getFiles();
		if (task.getArea().getKind() == CSWorkarea.KIND && paths != null && paths.length > 0) {
		    CSVPath taskArea = task.getArea().getVPath();
		    for (CSAreaRelativePath path : paths) {
				CSVPath vpath = new CSVPath(taskArea.concat(path.toString()));
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug( "::getFiles file vpath:" + vpath.toString() );
				}
				CSFile file = client.getFile(vpath);
				if (file.getKind() == CSDir.KIND) {
				    getDirFilesRecurse((CSDir) file, result);
				} else {
				    result.add( path );
				}
			}
		}
		return result.toArray(new CSAreaRelativePath[result.size()]);
    }

    /**
     * Helper method for getFilesNoDirs, which handles the recursive bit
     * 
     * @param dir
     *            the directory to parse
     * @param files
     *            the list to which the files should be appended
     * @throws CSException
     *             if there's a problem with the connection to the server
     */
    protected void getDirFilesRecurse(CSDir dir, List<CSAreaRelativePath> files) throws CSException {
	     /*
		 * Optimization to only get the file name and type, and no other
		 * properties
		 */
		CSSelectorAttribute[] selectAttrs = new CSSelectorAttribute[2];
		selectAttrs[0] = new CSSelectorAttribute("name", null, null, -1);
		selectAttrs[1] = new CSSelectorAttribute("kind", null, null, -1);
		CSSelector select = new CSSelector(selectAttrs);
		CSQuery query = new CSQuery(select, null, null);
	
		CSIterator iter = dir.getFiles(query.toString(), 0, -1);
		while (iter.hasNext()) {
		    CSFile file = (CSFile) iter.next();
		    if (file.getKind() == CSDir.KIND) {
		    	getDirFilesRecurse((CSDir) file, files);
		    } else {
				files.add(file.getVPath().getAreaRelativePath());
				if (LOGGER.isDebugEnabled()) {
				    LOGGER.debug("::getDirFilesRecurse file vpath:" + file.getVPath().toString());
				}
		    }
		}
    }

    /**
     * Convenience method to get the wrapped task's name
     * @return the task name as a String
     * @throws CSException if there's a connectivity or permissions issue
     */
    public String getTaskName() throws CSException {
    	return task.getName();
    }

    /**
     * Convenience method to get the area VPath for the contained task
     * @return the area VPath as a String
     * @throws CSException if there's a connectivity or permissions issue
     */
    public String getAreaPath() throws CSException {
	return task.getArea().getVPath().toString();
    }

    /**
     * Return the contained task's workflow. If the provided client is
     * null, returns the shallow copy contained within the task object,
     * otherwise resolves the full CSWorkflow object.
     * @param client the CSSDK client
     * @return the workflow object for the wrapped task
     * @throws CSException if there's a connectivity or permission issue
     */
    public CSWorkflow getWorkflow(CSClient client) throws CSException {
		if (client == null) {
		    return task.getWorkflow();
		}
		return client.getWorkflow(task.getWorkflowId(), true);
    }
   

}
