package com.example.demo.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

	
private static Properties props = null;
	
	/**
	 * read the config/config.properties file
	 * @param appPath
	 */
	public static void initConfig(String appPath)
	{
		String configPath = appPath  ;
		InputStream lInputStream = null;
		try
    	{
    		Properties lProps = new Properties();       
    		lInputStream = new FileInputStream(configPath);
        	lProps.load(lInputStream);
        	props = lProps;	
    	}
    	catch(Exception lEx)
    	{
    		lEx.printStackTrace();
    	}
		finally
		{
			if( lInputStream != null)
			{
				try
				{
					lInputStream.close();
				}
				catch(IOException ioException)
				{
					// Ignore
				}
			}
		}
	}
	
	/**
	 * gets the value of property key defined in config/config.properties
	 * 
	 */
	public static String getProperty(String key, String defaultValue)
	{
		return props.getProperty(key, defaultValue);
	}
}
