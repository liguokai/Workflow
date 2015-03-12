package com.biorad.wcms.utils;

import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.filesys.CSAreaRelativePath;
import com.interwoven.cssdk.workflow.CSTask;
import com.interwoven.cssdk.workflow.CSTransitionableTask;



/**
 * This interface defines a wrapper to provide additional
 * commonly-used functionality on a CSTransitionableTask.
 */
public interface TaskWrapper {
	/**
	 * The name of the task variable that specifies the name of the target task
	 * (which has various contextual meanings)
	 */
	public static final String TASK_VAR_TARGET_TASK = "target_task_name";

	/**
	 * Helper method to resolve a task in the workflow based on its name.
	 * @return returns the CSTask object pointed to (by name) by a variable on the current task
	 * @throws CSException if there's a communication problem with the server
	 */
	public CSTask findTargetTask() throws CSException;

	/**
	 * Execute the task's first transition
	 * @param comment the transition comment
	 * @throws CSException if there's a communication problem with the server
	 */
	public void execFirstTransition(String comment) throws CSException;
	
	/**
	 * Helper method to get the owners of any kind of workflow task.
	 * The returned set always has exactly one element if the task
	 * is anything but a GroupTask (0 to N) or DummyTask (0).
	 * @return a Set of users
	 * @throws CSException if there's a communication problem with the server
	 */
	
	/**
	 * Convenience method to avoid having to pass the underlying task
	 * whenever we already pass this object
	 * @return the underlying task object
	 */
	public CSTransitionableTask getTask();
	
	/**
	 * Get the array of area relative paths for all the files attached. Any dirs
	 * that are attached will be expanded.
	 * @param client the CSClient object
	 * @return array of file relative paths
	 * @throws CSException on error
	 */
	public CSAreaRelativePath[] getFilesNoDirs(CSClient client) throws CSException;

}