package com.biorad.wcms.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CltUtil {
	
	private static final Log LOGGER = LogFactory.getLog(CltUtil.class);

	protected static final String TRANSFORM_CMD = "/app/Interwoven/TeamSite/bin/iwpt_compile.ipl";

    public static String runCLT(String command, boolean appendStderr, String... args ) throws Exception {
    	LOGGER.debug( ">> Starting runCLT" );
    	LOGGER.debug( "\tcmd: " + command );
        // Execute command
    	
    	String[] pbArgs = new String[ args.length + 1 ];
    	pbArgs[0] = command;
    	
    	for (int i = 0; i < args.length; i++) {
    		LOGGER.debug( "\t\tArg " + i + ": " + args[ i ] );
    	    pbArgs[i + 1] = args[ i ];
    	}

    	ProcessBuilder pb = new ProcessBuilder(pbArgs);
 
    	pb.redirectErrorStream(appendStderr);
    	Process pr = pb.start();

    	BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
    	String line = null;
    	StringBuffer result = new StringBuffer();
    	while ((line = input.readLine()) != null) {
    	    result.append(line);
    	}

    	try {
    	    input.close();
    	    pr.destroy();
    	} catch (Exception e) {
    	    LOGGER.warn("Failed to kill the subprocess: " + command, e);
    	}
    	LOGGER.debug( "\tResults was: " + result.toString() );
    	return result.toString();
    	
 
    }
    
    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
          sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
      }
}
