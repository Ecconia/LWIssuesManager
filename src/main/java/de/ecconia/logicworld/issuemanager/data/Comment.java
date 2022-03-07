package de.ecconia.logicworld.issuemanager.data;

import de.ecconia.java.json.JSONObject;

public class Comment
{
	private final String id;
	private final String parentID;
	private final Author author;
	private final String body;
	private final long creationTime;
	
	public Comment(JSONObject commentJSON)
	{
		creationTime = commentJSON.getLong("createdat");
		//String renderedBody = commentJSON.getString("renderedbody");
		author = new Author(commentJSON.getObject("author"));
		// long editTimestamp = commentJSON.getLong("editedat");
		//rating : { score likedBy : [ <int> ] }
		id = commentJSON.getString("id");
		body = commentJSON.getString("body");
		parentID = commentJSON.getString("parentid"); //Empty if none
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getParentID()
	{
		return parentID;
	}
	
	public Author getAuthor()
	{
		return author;
	}
	
	public String getBody()
	{
		return body;
	}
	
	public long getCreationTime()
	{
		return creationTime;
	}
}
