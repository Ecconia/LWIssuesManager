package de.ecconia.logicworld.issuemanager.data;

import java.awt.Color;

import de.ecconia.java.json.JSONObject;

public class Tag
{
	private String name;
	private Color color;
	
	public Tag(JSONObject object)
	{
		name = object.getString("name");
		String colorString = object.getString("color");
		if(colorString.isEmpty())
		{
			throw new RuntimeException("Tag without color flair...");
		}
		color = Color.decode(colorString);
	}
	
	public String getName()
	{
		return name;
	}
	
	public Color getColor()
	{
		return color;
	}
}
