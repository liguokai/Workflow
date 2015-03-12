package com.biorad.wcms.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XPathUtil {

	private static final Log LOGGER = LogFactory.getLog(XPathUtil.class);

	public Document getXmlDocument( String filepath ) {
		LOGGER.debug( "Starting a getXmlDocument" );
		LOGGER.debug( "\tFile Path is: " + filepath );
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.debug( "\tError trying to create a new Document Builder" );
			e.printStackTrace();
		}
		Document doc = null;
		if( builder != null ) {
			try {
				doc = builder.parse( filepath );
			} catch (SAXException e) {
				LOGGER.debug( "\tError trying to parse the file (SAXException)" );
				e.printStackTrace();
			} catch (IOException e) {
				LOGGER.debug( "\tError trying to parse the file (IOException)" );
				e.printStackTrace();
			}
		}
		return doc;
	}
	
	public NodeList getXPathResults( String filepath, String query ) {
		LOGGER.debug( "Starting a getXPathResults" );
		LOGGER.debug( "\tFile Path is: " + filepath );
		LOGGER.debug( "\tQuery is: " + query );
		Document document = getXmlDocument( filepath );
		if( document != null ) {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = null;
			try {
				expr = xpath.compile( query );
			} catch (XPathExpressionException e) {
				LOGGER.debug( "\tError trying to compile the xpath query" );
				e.printStackTrace();
				return null;
			}
			if( expr != null ) {
				Object result = null;
				try {
					result = expr.evaluate(document, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					LOGGER.debug( "\tError trying to evaluate the xpath query" );
					e.printStackTrace();
					return null;
				}
				return (NodeList) result;
			}
		} else {
			return null;
		}
		return null;	
	}
	
	public NodeList getXPathResults( String filepath, String query, Boolean encoded ) {
		LOGGER.debug( "Starting a getXPathResults - from encoded" );
		LOGGER.debug( "\tFile Path is: " + filepath );
		LOGGER.debug( "\tQuery is: " + query );
		Document document = getXmlDecodedDocument( filepath );
		if( document != null ) {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = null;
			try {
				expr = xpath.compile( query );
			} catch (XPathExpressionException e) {
				LOGGER.debug( "\tError trying to compile the xpath query" );
				e.printStackTrace();
				return null;
			}
			if( expr != null ) {
				Object result = null;
				try {
					result = expr.evaluate(document, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					LOGGER.debug( "\tError trying to evaluate the xpath query" );
					e.printStackTrace();
					return null;
				}
				return (NodeList) result;
			}
		} else {
			return null;
		}
		return null;	
	}
	public Document getXmlDecodedDocument( String filepath ){
		LOGGER.debug( "Starting a getXmlDecodedDocument" );
		LOGGER.debug( "\tFile Path is: " + filepath );
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOGGER.debug( "\tError trying to create a new Document Builder" );
			e.printStackTrace();
		}
		String xml = null;
		try {
			LOGGER.debug( "\t\tReading the file as string" );
			xml = readFileAsString( filepath );
			LOGGER.debug( "\t\tDoing the 1st replacement" );
			xml = xml.replaceAll( "&gt;", ">" );
			LOGGER.debug( "\t\tDoing the 2nd replacement" );
			xml = xml.replaceAll( "&lt;", "<" );		
			LOGGER.debug( "\t\tDoing the 3rd replacement" );
			xml = xml.replaceAll( "<br>", "<br />" );
		} catch (IOException e1) {
			LOGGER.debug( "\tError trying to parse the string (IOException)" );
			e1.printStackTrace();
		}
		
		Document doc = null;
		if( xml != null ) {
			if( builder != null ) {
				try {
					InputStream is = new ByteArrayInputStream( xml.getBytes() );
					doc = builder.parse( is );
				} catch (SAXException e) {
					LOGGER.debug( "\tError trying to parse the file (SAXException)" );
					e.printStackTrace();
				} catch (IOException e) {
					LOGGER.debug( "\tError trying to parse the file (IOException)" );
					e.printStackTrace();
				}
			}
		}
		return doc;			
	}
	
	private static String readFileAsString(String filePath) throws java.io.IOException{
		LOGGER.debug( ">> readFileAsString" );
		LOGGER.debug( "\tFile Path is: " + filePath );
		byte[] buffer = new byte[(int) new File(filePath).length()];
		FileInputStream f = new FileInputStream(filePath);
		f.read(buffer);
		return new String(buffer);
	}
	
}
