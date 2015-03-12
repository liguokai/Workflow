package com.biorad.wcms.workflow.tasks;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.biorad.wcms.utils.BaseTaskWrapper;
import com.biorad.wcms.utils.TaskWrapper;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSNameValuePair;
import com.interwoven.cssdk.workflow.CSExternalTask;
import com.interwoven.cssdk.workflow.CSTransitionableTask;
import com.interwoven.cssdk.workflow.CSURLExternalTask;




public abstract class BaseExternalTask implements CSURLExternalTask {
    private static final Log LOGGER = LogFactory.getLog(BaseExternalTask.class);

    @SuppressWarnings("unchecked")
    public final void execute(CSClient client, CSExternalTask task, Hashtable params) throws CSException {
	
    	if (LOGGER.isInfoEnabled()) {
		    LOGGER.info("Launching " + this.getClass().getSimpleName() + " for task " + task.getId());
		}
		if (LOGGER.isDebugEnabled()) {
		    logDiagnostics(task);
		}

		// This try/catch just logs things for us in our log rather than in the
		// main content_center.log
		try {
		    execute(client, new BaseTaskWrapper(task), (Hashtable<String, String[]>) params);
		} catch (CSException e) {
		    LOGGER.error(e.getClass().getSimpleName() + " occurred in the execution of task " + this.getClass().getSimpleName(), e);
		    throw e;
		} catch (RuntimeException e) {
		    LOGGER.error(e.getClass().getSimpleName() + " occurred in the execution of task " + this.getClass().getSimpleName(), e);
		    throw e;
		}
	}

    /**
     * This is the new interface that all subclasses should implement.
     * 
     * @param client
     *            the CSClient interface to the CSSDK API
     * @param task
     *            the wrapper for the workflow task being run
     * @param params
     *            a hashtable of special URL parameters passed to the task
     * @throws CSException
     *             if there's a connectivity or permissions problem
     */
    public abstract void execute(CSClient client, TaskWrapper task, Hashtable<String, String[]> params) throws CSException;

    /**
     * Helper function used to print the variables attached to the current
     * workflow. Callers should wrap this call in a
     * <code>if (LOGGER.isDebugEnabled())</code> block
     * 
     * @param task
     *            the workflow task object being run
     * @throws CSException
     *             if there's a connectivity or permissions problem
     */
    public void logDiagnostics(CSTransitionableTask task) throws CSException {
		// Skip this whole method if not logging debug
		if ( !LOGGER.isDebugEnabled() ) {
		    return;
		}
	
		LOGGER.debug( "Entering Job <" + task.getWorkflow().getId() + "> owned by " + task.getWorkflow().getOwner().getNormalizedName() + "." );
	
		CSNameValuePair[] vars = task.getVariables(null);
		LOGGER.debug( "---- TASK VARIABLES ---- " + vars.length );
		for (int i = 0; i < vars.length; i++) {
		    CSNameValuePair nameValuePair = vars[i];
		    LOGGER.debug( nameValuePair.name + " :: " + nameValuePair.value );
		}
	
		CSNameValuePair[] wvars = task.getWorkflow().getVariables(null);
		LOGGER.debug( "---- WORKFLOW VARIABLES ---- " + wvars.length );
		for (int i = 0; i < wvars.length; i++) {
		    CSNameValuePair nameValuePair = wvars[i];
		    LOGGER.debug( nameValuePair.name + " :: " + nameValuePair.value );
		}
    }
}
