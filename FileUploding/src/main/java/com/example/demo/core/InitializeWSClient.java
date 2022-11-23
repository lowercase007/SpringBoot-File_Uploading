package com.example.demo.core;

import java.io.File;





import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.mozilla.universalchardet.UniversalDetector;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.seclore.fs.helper.exception.FSHelperException;
import com.seclore.fs.helper.library.FSHelperLibrary;

public class InitializeWSClient {
	
	/**
	 * @param 	configPath
	 * 			Takes the application config file path as input
	 * @throws 	Exception
	 */
	public static void initializeHelperLibrary(String configPath) throws Exception
	{
        try
        {
        	String configContentXMLString = getConfigFileContent(configPath);
        	// Initialize method takes WSClient config content not the config file path.
            FSHelperLibrary.initialize(configContentXMLString);
           
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
	/**
	 * @param 	helperId
	 * 			Tenant Id
	 * @param 	helperResourcePath
	 * 			Resource Path for files of Seclore Policy Server. To be left empty here.
	 * @param 	helperConfigPath
	 * 			File path for 'tenant config.xml' file
	 * @throws 	Exception
	 * 			Code snippet won't run if any exception is thrown in this block
	 */
	public static void initializeHelper(String helperId, String helperResourcePath, String helperConfigPath) throws Exception
	{
        try
        {
        	String configContentXMLString = getConfigFileContent( helperConfigPath  );
        	// Initialize method takes WSClient config content not the config file path.
            FSHelperLibrary.initializeHelper(helperId, helperResourcePath, configContentXMLString);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
	/**
	 * This method reads an XML file and returns the content of the XML file in string format
	 * @param 	appPath
	 * 			XML file path
	 * @return	File content in string format
	 * @throws 	Exception
	 */
	private static String getConfigFileContent( String appPath ) throws Exception
	{
		validateConfigFile( appPath );
		
		InputStream inputStream = null;
		try
		{
			String encoding = detectEncoding(appPath);
			if( encoding == null || encoding.trim().isEmpty() )
			{
				encoding = "UTF-8";
			}
			
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(false);
			builderFactory.setValidating(false);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			inputStream = new FileInputStream(appPath);
			InputSource lInputSource = new InputSource( inputStream );
			lInputSource.setEncoding(encoding);
			Document document = builder.parse( lInputSource );
			
			
			TransformerFactory lTransFactory = TransformerFactory.newInstance();
			Transformer lTrans = lTransFactory.newTransformer();
			DOMSource lSource = new DOMSource(document);
			StringWriter lStringWriter = new StringWriter();
			lTrans.transform( lSource, new StreamResult( lStringWriter ) );
			return lStringWriter.toString();
		}
		catch (Exception e)
		{
			System.out.println("*** Config file should be in UTF-16LE encoding ***");
			e.printStackTrace();
			System.exit(0);
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
		return "";
   	}

	/**
	 * Validates whether the file exists at the given location & has valid BOM format
	 * @param configFilePath
	 * @throws Exception
	 * @return The file object representing the configFilePath
	 */
	private static File validateConfigFile(String configFilePath) throws Exception
	{
		
		if ( configFilePath == null || configFilePath.trim().isEmpty() )
		{
			throw new Exception("Configuration File path is not provided.");
		}

		File file = new File(configFilePath);
		if ( !file.exists() )
		{
			throw new Exception("Configuration File does not exist at '"+ file.getAbsolutePath() + "'");
		}

		if( !file.isFile() )
		{
			throw new Exception("'" + file.getAbsolutePath() + "' is not a file.");
		}
		
		// Length 3 is checked to make sure that enough bytes are available for
		// BOM reading.
		if (file.length() < 3)
		{
			throw new Exception("'" + file.getAbsolutePath() + "' is not a valid configuration file.");
		}
		return file;
	}
	
	/**
	 * Detect encoding format of file
	 * @param 	pConfigXmlPath
	 * 			File path whose encoding type is to be detected
	 * @return	encoding format (eg: utf-8, utf-16)
	 */
	public static String detectEncoding(String pConfigXmlPath)
	{
		FileInputStream lFis = null; 
		try
		{
			lFis = new FileInputStream(pConfigXmlPath);
			UniversalDetector lDetector = new UniversalDetector(null);
		    byte[] lBuff = new byte[512];
		    int liread;
		    while( (liread = lFis.read(lBuff)) > 0 && !lDetector.isDone() )
		    {
		    	lDetector.handleData(lBuff, 0, liread);
		    }
		    lDetector.dataEnd();
		    String lstrEncoding = lDetector.getDetectedCharset();
		    lDetector.reset();
		    return lstrEncoding;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if ( lFis != null)
			{
				try
				{
					lFis.close();
				} catch (IOException e) 
				{
					// Igniore
				}
			}
		}
		return null;
	}
	
	/**
	 * Terminate the created session and reset the initialized Server SDK library.
	 */
	public static void terminateWSClient()
	{
		 try
         {
             FSHelperLibrary.logInfo("Terminating FSHelper Library");
             if(FSHelperLibrary.isTerminated() == false)
             {
                 System.out.println("FSHelperLibrary.isTerminated(): " + FSHelperLibrary.isTerminated());
                 FSHelperLibrary.terminate();
             }
         }
         catch (FSHelperException e)
         {          
             FSHelperLibrary.logError(e.getMessage(), e);
         } 
	}
	

}
