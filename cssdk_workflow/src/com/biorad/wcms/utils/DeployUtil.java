package com.biorad.wcms.utils;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.cssdk.access.CSAuthorizationException;
import com.interwoven.cssdk.access.CSExpiredSessionException;
import com.interwoven.cssdk.common.CSClient;
import com.interwoven.cssdk.common.CSException;
import com.interwoven.cssdk.common.CSRemoteException;
import com.interwoven.cssdk.filesys.CSFile;
import com.interwoven.cssdk.filesys.CSHole;
import com.interwoven.cssdk.filesys.CSSimpleFile;
import com.interwoven.cssdk.filesys.CSVPath;
import com.biorad.wcms.utils.CltUtil;

public class DeployUtil {

	private static final Log LOGGER = LogFactory.getLog(DeployUtil.class);
	private static final String IWMNT = "";

	public static CSVPath[] iwptCompile(CSClient client, CSFile source, CSFile template, String output) throws CSException, IOException {
		LOGGER.debug( ">> Starting iwptCompile" );
		if (source.getKind() != CSHole.KIND && source.getKind() != CSSimpleFile.KIND) {
			LOGGER.error("Invalid file type for deployment: " + source.getClass().getSimpleName());
			throw new IllegalArgumentException("Invalid file type for deployment: " + source.getClass().getSimpleName());
		}
		//IWMNT + template.getVPath().getPathNoServer().toString() 
		String cmd = CltUtil.TRANSFORM_CMD; // + 
		String[] args = { 
				"-pt '" + IWMNT + template.getVPath().getPathNoServer().toString() + "'", 
				"-iw_pt-dcr '" + IWMNT + source.getVPath().getPathNoServer().toString() + "'" 
				}; 
		
		LOGGER.debug( "\tThe cmd is: " + cmd );

		String results = null;
		
		try {
			results = CltUtil.runCLT( cmd, true, args );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			results = "ERROR: " + e.getMessage();
			e.printStackTrace();
		}
		LOGGER.debug( "\tThe results were: " + results );

		CSVPath[] files = null;
		if( results.contains( "<ERROR>" ) ) {
			return null;
		} else {
			
			
		}
		return files;
	}
	
	public static CSFile getTplName( CSClient client, CSFile source ) throws CSAuthorizationException, CSExpiredSessionException, CSRemoteException, CSException {
		LOGGER.debug( ">> Starting getTplName" );
		LOGGER.debug( "\tSource: " + source.getVPath().toString() );
		String dcrPath = source.getVPath().getPathNoServer().toString();
		String[] parts = dcrPath.split( "/data/" );
		String tplPath = parts[ 0 ] + "/presentation/preview.tpl";
		LOGGER.debug( "\ttplPath: " + tplPath );
		CSFile template = client.getFile( new CSVPath( tplPath ) );
		LOGGER.debug( "\t\tOutput: " + template.getVPath().getPathNoServer().toString() );
		LOGGER.debug( "<< Ending getTplName" );
		return template;
	}
	
	public static String getOutputName( CSClient client, CSFile source ) throws CSAuthorizationException, CSExpiredSessionException, CSRemoteException, CSException {
		LOGGER.debug( ">> Starting getOutputName" );
		LOGGER.debug( "\tSource: " + source.getVPath().toString() );
		String dcrName = source.getVPath().getName().toString().replace( ".xml", "" );
		String outputName = source.getVPath().getPathNoServer().getArea().toString() + "/properties/" + dcrName + ".md.properties";
		LOGGER.debug( "\toutputName: " + outputName );
		LOGGER.debug( "<< Ending getOutputName" );
		return outputName;
	}
	
	
}
