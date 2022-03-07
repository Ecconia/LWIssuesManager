package de.ecconia.logicworld.issuemanager.data;

import de.ecconia.java.json.JSONObject;

public class Author
{
	private int id;
	private String name;
	private String flair;
	//TODO: Image path: "picture"
	
	public Author(JSONObject object)
	{
		id = object.getInt("id");
		name = object.getString("username");
		flair = object.getString("flair");
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getFlair()
	{
		return flair;
	}
}
