package de.ecconia.logicworld.issuemanager.data;

import de.ecconia.java.json.JSONObject;
import java.awt.Color;

public enum Type
{
	BUG(Color.orange),
	FEATURE(Color.blue),
	ENHANCEMENT(Color.cyan);
	
	private final Color color;
	
	Type(Color color)
	{
		this.color = color;
	}
	
	public static String asNameOrNull(Type type)
	{
		return type == null ? null : type.name();
	}
	
	public static Type fromString(String typeString)
	{
		for(Type type : values())
		{
			if(typeString.equals(type.name()))
			{
				return type;
			}
		}
		throw new RuntimeException("Could not find type '" + typeString + "'");
	}
	
	public Color getColor()
	{
		return color;
	}
	
	public static Type fromJSON(JSONObject ticketJSON)
	{
		String typeString = ticketJSON.getString("kind");
		if("FEATURE".equals(typeString))
		{
			return Type.FEATURE;
		}
		else if("ISSUE".equals(typeString))
		{
			return Type.BUG;
		}
		else
		{
			throw new RuntimeException("Unexpected type: " + typeString);
		}
	}
}
