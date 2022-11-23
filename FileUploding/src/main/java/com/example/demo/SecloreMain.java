package com.example.demo;

import java.util.ArrayList;

import java.util.List;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.demo.core.Config;
import com.example.demo.core.FSEntity;
import com.example.demo.core.InitializeWSClient;
import com.seclore.fs.helper.core.ProtectedFile;
import com.seclore.fs.helper.enums.ProtectionType;
import com.seclore.fs.helper.exception.FSHelperException;
import com.seclore.fs.helper.library.FSHelper;
import com.seclore.fs.helper.library.FSHelperLibrary;
import com.seclore.fs.protect.example.util.XMLUtil;



@SpringBootApplication
public class SecloreMain {
	
//	@Autowired 
//	private static XMLUtil xMLUtil;
	
	

		// FS Helper library is multi-tenant. Use unique tenant identifier for each tenant
		private static String tenantID = "Tenant-1";
			
		// This file contains information about the Application into which this code snippet is to be integrated. 
		private static final String appConfigXMLFilePath = "config/config.xml";
				
		// This file contains info for creating session with Seclore Policy Server and initializing Server SDK library
		private static final String tenantConfigXMLFilePath = "config/tenant config.xml";
			
		// Used to take user input
		private static Scanner scanner = null;
		
		/**
		 * <p>This block initializes the Server SDK library and creates a session with Seclore Policy Server</p>
		 * @exception  Code snippet won't run if any exception is thrown in this block.
		 */
		static
		{
			try
			{
				Config.initConfig("config/config.properties");
				InitializeWSClient.initializeHelperLibrary(appConfigXMLFilePath);
				InitializeWSClient.initializeHelper(tenantID, "", tenantConfigXMLFilePath);
				scanner = new Scanner(System.in);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.exit(0);
			}
		}

	public static void main(String[] args) {
		SpringApplication.run(SecloreMain.class, args);
		
		try
		{	
			String outputFilePath = protectAndWrapFile();
					  	
			if( outputFilePath != null && !outputFilePath.trim().isEmpty() )
			{
				 System.out.println("File '" + outputFilePath + "' is protected and wrapped successfully");	
			}
			else
			{
				 throw new Exception("Unexpected error");
			}
			
		}
		catch(Exception exp)
		{
			exp.printStackTrace();
		}
		finally
		{
			if( scanner != null )
			{
				scanner.close();
			}
			
			InitializeWSClient.terminateWSClient();
		}
	}
	
	/**
	 * Protect and wrap a file using Seclore SDK
	 * @return	the absolute file path of protected file
	 * @throws Exception
	 */
	public static String protectAndWrapFile() throws Exception
	{
		System.out.println("Enter absolute path of the file to be protected and wrapped:");
		String filePath = scanner.nextLine();
		
		// get the FSHelper object by specifying the tenantId.
		//note: This tenant id should be same which was used during initialization of Seclore SDK
        FSHelper tenantObj = FSHelperLibrary.getHelper(tenantID);
        
        // does basic validation checks to see if the file is supported by SDK and is not already protected
        validateIfFileSupportsProtection(tenantObj, filePath);
        
        // this display file name goes in activity log
      	// for more details see javadoc of FSHelper.protectAndWrap()
		String dispFileName = filePath;
		// get XML structure to protect with hot folder 
		String protectionDetails =  getProtectDetailsXML();
		// show details about protector
		String protectorDetails = "";
		// activity comments to be shown
		String actvityComments = "Protected by using FS Helper Library's protectAndWrap method";
		
		// calling Seclore SDK's protectAndWrap api 
		ProtectedFile protectedWrappedFile = tenantObj.protectAndWrap(null, 
																	  filePath, 
																	  dispFileName, 
																	  ProtectionType.PROTECT, 
																	  protectionDetails, 
																	  protectorDetails, 
																	  actvityComments);
		System.out.println("Protected File Id: " + protectedWrappedFile.getFileId());
		
		return protectedWrappedFile.getFilePath();
	}
	
	/**
	 * Validates if the file supports Seclore protection
	 * 
	 * @param tenantObj - FSHelper object created for the tenant.
	 * @param filePath	- Absolute path of the file to be protected.
	 * @throws Exception
	 */
	private static void validateIfFileSupportsProtection(FSHelper tenantObj, String filePath)
			throws FSHelperException, Exception
	{	
		//check if file is already Seclore protected
		if( tenantObj.isProtectedFile(filePath) || tenantObj.isHTMLWrapped(filePath) )
		{
			throw new Exception("File '"+filePath+"' is already protected");
		}
       
		//check if file extension is supported by Seclore FS Helper Library
        if( tenantObj.isSupportedFile(filePath) == false && tenantObj.isBasicProtectionSupported(filePath) == false )
		{	
        	 String fileExt = filePath.substring( filePath.lastIndexOf(".")+1 );
        	//note: if file is not of standard format and does not support basic protection then throw error
			throw new Exception("File with extention '"+fileExt+"' is not supported by Seclore FS Helper Library");
		}
        
        //check if file extension is supported for HTML wrapping by Seclore FS Helper Library
        if( tenantObj.isHTMLWrapSupported(filePath) == false )
		{	
			//note: file is of standard format but not supported for HTML wrapping
			throw new Exception("File '"+filePath+"' does not support HTML wrapping");
		}
	}
	
	/**
	 * Forms the protection XML request string to be sent to PS.
	 * 
	 * @return protection request XML structure 
	 */
	private static String getProtectDetailsXML() throws Exception
	{
		System.out.println("Enter owner mail id:");
		String ownerEmail = scanner.nextLine();
		
		System.out.println("Enter recipient(s) mail id: [if multiple seperate by using comma ,]");
		String recipientsEmailCommaSeperated = scanner.nextLine();
		
		System.out.println("Enter recipient(s) rights id: [if multiple seperate by using comma ,]");
		String rightsCommaSeparated = scanner.nextLine();
		//unlike the recipient's , you cannot create the owner 
		// i.e you cannot call createUser on owner emailId
		// This is because the create user method needs a referrer-email-id and 
		// this user should already exist in the policy server.
		FSEntity owner = searchUser(ownerEmail.trim());
		if(owner == null)
		{	
			throw new Exception("owner does not exist in Policy Server.");
		}
	
		String [] emails = recipientsEmailCommaSeperated.split(",");
		String [] rights = rightsCommaSeparated.split(",");
		
		List<FSEntity> entities = new ArrayList<FSEntity>();
		for (int i = 0; i < emails.length; i++)
		{
			//Searching the user in the Policy Server's repositories based on email id.
			//if not found creating the recipient user in the Policy Server's repository.
			//note: owner should already exist in the Policy Server 
			//else the protect request will fail.
			FSEntity recipient = searchAndCreateUser(emails[i].trim(),ownerEmail);
			//user does not exist
			if( recipient == null )
			{	// creation of the user failed , abort the request	
				System.err.println("*** No entity found for email id '" + emails[i] + "' ");
				return null;
			}
			entities.add(recipient);
		}
		
		if(rights.length != entities.size())
		{
			throw new Exception("Number of recipients and number of rights do not match.");
		}

		StringBuilder fileAccessRightMappings = new StringBuilder();
		int i = 0;
		for (FSEntity entity : entities)
		{
			String  fileAccessRightMapping = "<file-access-right-mapping>"+
											"<action>1</action>"+
											"<access-right>"+
												"<entity>"+
													"<id>"+XMLUtil.escapeForXML(entity.getId())+"</id>"+
												    
													"<rep-code>"+entity.getRepCode()+"</rep-code>"+
													"<type>"+entity.getType()+"</type>"+
												"</entity>"+
												"<primary-access-right>"+rights[i]+"</primary-access-right>"+
												"<offline>"+Config.getProperty("offline", "0")+"</offline>"+
												"<redistribute>"+Config.getProperty("redistribute", "0")+"</redistribute>"+
												"<lock-to-first-machine>"+Config.getProperty("lock.to.first.machine", "0")+"</lock-to-first-machine>"+
												"<no-of-days-since-protection>"+Config.getProperty("no.of.days.since.protection", "-1")+"</no-of-days-since-protection>"+
												"<no-of-days-since-first-access>"+Config.getProperty("no.of.days.since.first.access", "-1")+"</no-of-days-since-first-access>"+
											"</access-right>"+
										"</file-access-right-mapping>";
			fileAccessRightMappings.append(fileAccessRightMapping);
			i++;
		}
		
		String xml = "<protection-details>"
					+"<classification><id>1</id></classification>"+
					"<file-access-right-mappings>"+
					fileAccessRightMappings.toString()+
				"</file-access-right-mappings>"+
				"<owner>"+
					"<entity>"+
						"<id>"+owner.getId()+"</id>"+
						"<rep-code>"+owner.getRepCode()+"</rep-code>"+
						"<type>1</type>"+
					"</entity>"+
				"</owner>"+
				"</protection-details>";
		return xml;
	}
	
	/**
	 * A helper method to get user details if exists
	 * or create a new user in PS repository.
	 * For more details see searchUser & createUser.
	 * 
	 */
	private static FSEntity searchAndCreateUser(String emailId,String referrerEmailId) 
	throws FSHelperException,Exception
	{	
		FSEntity user = searchUser(emailId);
		//user not found in any repository
		if(user != null)
		{
			return user;
		}
		//note: referrer should already exist in policy server before calling create user
		return createUser(emailId,referrerEmailId);
	}
	
	/**
	 * searches a user in the policy server with given email id.
	 * 
	 * @param emailId - emailId of the user to be created in PS
	 * @return  -  FSEntity representing the repository information in which the user exists.
	 *             In case the user does not exist returns null
	 *             
	 * @throws FSHelperException 
	 *          - In case some error occurs while searching the user
	 *  
	 */
	private static FSEntity searchUser(String emailId) throws FSHelperException,Exception
	{
		FSHelper tenantObj = FSHelperLibrary.getHelper(tenantID); 
		// XML request structure to search a user in Policy Server , if does not exist creating a new user
		String searchUserXML = "<request>"
									+"<request-header>"
										+ "<protocol-version>2</protocol-version>"
									+ "</request-header>"
									+ "<request-details>"
										+ "<email-id>"+XMLUtil.escapeForXML(emailId)+"</email-id>"
									+"</request-details>"
							  +"</request>";
		//sending search user request to Policy Server
		String responseXML = tenantObj.sendRequest(null,74, searchUserXML);
		Node rootNode = XMLUtil.getRootNode(responseXML); 
		Node requestStatusNode = XMLUtil.parseNode("request-status", rootNode);
		String returnValue = XMLUtil.parseString("return-value", requestStatusNode);
		if( "1".equals(returnValue) )
		{
			NodeList nodeList = XMLUtil.parseNodeList("entities/entity", rootNode);
			if ( nodeList != null && nodeList.getLength() > 0 )
			{	
				// Get first entity
				Node node = nodeList.item(0); 
				String id = XMLUtil.parseString("id", node);
				int repCode = XMLUtil.parseInteger("rep-code", node);
				int type = XMLUtil.parseInteger("type", node);
				return new FSEntity(id, repCode, type);
			}
		}
		// if no such user found in Policy Server then it will return this error '-220372 :- No Entities Found.'
		// checking if there is some processing other than error code  -220372 then throwing the exception
		if( "-220372".equals(returnValue) == false )
		{
			String errorMessage = XMLUtil.parseString("error-message", requestStatusNode);
			String displayMessage = XMLUtil.parseString("display-message", requestStatusNode);
			if( displayMessage == null || displayMessage.trim().isEmpty() ){
				displayMessage = errorMessage;
			}
			throw new Exception( displayMessage +"("+returnValue+")" );
		}
		
		return null;
	}
	
	/** 
	 * to create a new user in policy server with given emailId.
	 * Before calling this method check if the user with given emailId 
	 * already exists in policy server using searchUser method.
 	 * 
	 * @param emailId 
	 *        emailId of the new user to be created in policy server
	 * @param referrerEmailId 
	 *        emailId of the person who is referring this person.
	 *        This user should already exist in the policy server.
	 *        
	 * @throws Exception if user already exists or 
	 *         no user exists in policy server with given referrerEmailId        
	 */
	private static FSEntity createUser(String emailId , String referrerEmailId) throws Exception
	{
		FSHelper tenantObj = FSHelperLibrary.getHelper(tenantID); 	
		// request structure for creating a user
		String createUserXML = "<request>"
									+"<request-header>"
										+ "<protocol-version>2</protocol-version>"
									+ "</request-header>"
									+ "<request-details>"
										+ "<im-user>"
											+ "<email-id>"+XMLUtil.escapeForXML(emailId)+"</email-id>"
											+ "<requestor-comments>User created by MFP</requestor-comments>"
											+ "<referrer-email-id>"+referrerEmailId+"</referrer-email-id>"
										+ "</im-user>"
									+ "</request-details>"
							  + "</request>";
		// sending create user request to Policy Server 
		String responseXML = tenantObj.sendRequest(null,109,createUserXML);
		Node rootNode = XMLUtil.getRootNode(responseXML);
		Node requestStatusNode = XMLUtil.parseNode("request-status", rootNode);
		String returnValue = XMLUtil.parseString("return-value", requestStatusNode);
		// if return-value is not 1 this means user creation failed , hence throwing the error
		if("1".equals(returnValue) == false ) 
		{
			String errorMessage = XMLUtil.parseString("error-message", requestStatusNode);
			String displayMessage = XMLUtil.parseString("display-message", requestStatusNode);
			if( displayMessage == null || displayMessage.trim().isEmpty() )
			{
				displayMessage = errorMessage;
			}
			//user cannot be created in Policy Server , hence throwing exception
			System.out.println("user can't be created "+displayMessage);
			throw new Exception( displayMessage +"("+returnValue+")" );
		}
		// user already exist
		// -220394 :- User with email id ''{0}'' already exist. 
		// -220518 :- User with email id ''{0}'' already exist in a repository which is not visible. 
		Node node = XMLUtil.parseNode("im-user", rootNode);
		String id = XMLUtil.parseString("id", node);
		int repCode = XMLUtil.parseInteger("rep-code", node);
		int type = 1;
		return new FSEntity(id, repCode, type);
	}
	
		
		
		
		
		
		
		
	}


