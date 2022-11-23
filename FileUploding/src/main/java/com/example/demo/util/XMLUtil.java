package com.example.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLUtil
{
	private static XPath xpath = null;
	static
	{
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	
	/**
	 * @param xmlString
	 * @return
	 */
	public static Document parseDocument(String xmlString) 
	{
		InputStream inputStream = null;
		try
		{
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document xmlDocument = builder.parse( new  InputSource(new StringReader(xmlString)) );
			return xmlDocument;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//LoggerUtil.logError("Error while parsing - "+xmlString, e);
		}
		finally
		{
			if(inputStream != null)
			{
				try{
					inputStream.close();
				}
				catch(IOException ioException)
				{
					//ignore
				}
			}
		}
		return  null;
	}
	
	/**
	 * @param document
	 * @return
	 */
	public static Node getRootNode(Document document)
	{
		NodeList nodeList = document.getChildNodes();
		return nodeList.item(0);
	}
	
	/**
	 * @param XML String
	 * @return
	 */
	public static Node getRootNode(String xmlString)
	{
		Document document = parseDocument(xmlString);
		return  getRootNode(document);
	}

	/**
	 * @param xmlTagName
	 * @param parentNode
	 * @return
	 */
	public static Node parseNode(String xmlTagName, Node parentNode)
	{
		try
		{
			Node node = (Node) xpath.evaluate(xmlTagName, parentNode, XPathConstants.NODE);
			return node;
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param xmlTagName
	 * @param parentNode
	 * @return
	 */
	public static NodeList parseNodeList(String xmlTagName, Node parentNode)
	{
		try
		{
			NodeList nodeList = (NodeList) xpath.evaluate(xmlTagName, parentNode, XPathConstants.NODESET);
			return nodeList;
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param xmlTagName
	 * @param parentNode
	 * @return
	 */
	public static String parseString(String xmlTagName, Node parentNode)
	{
		try
		{
			String requestId = (String) xpath.evaluate(xmlTagName+"/text()", parentNode, XPathConstants.STRING);
			return requestId;
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static Integer parseInteger(String xmlTagName, Node parentNode)
	{
		try
		{
			Number li = (Number) xpath.evaluate(xmlTagName, parentNode, XPathConstants.NUMBER);
			return li.intValue();
		}
		catch (XPathExpressionException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String escapeForXML(String strXML)
	{
		if(strXML == null || strXML.trim().isEmpty())
		{
			return strXML;
		}
		
		return strXML.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
	}
}

