package com.example.demo.core;

public class FSEntity {
	
private String id;
	
	/**
	 * repository code 
	 */
	private int repCode;
	/**
	 * 
	 * entity type
	 * 	 1 for user
	 *   2 for group
	 */
	private int type;
	
	// Default constructor
	public FSEntity()
	{
		
	}
	
	public FSEntity(String id, int repCode, int type)
	{
		this.id = id;
		this.repCode = repCode;
		this.type = type;
	}
	
	public String getId() 
	{
		return id;
	}
	public void setId(String id) 
	{
		this.id = id;
	}
	public int getRepCode() 
	{
		return repCode;
	}
	public void setRepCode(int repCode) 
	{
		this.repCode = repCode;
	}
	public int getType() 
	{
		return type;
	}
	public void setType(int type)
	{
		this.type = type;
	}
	

}
