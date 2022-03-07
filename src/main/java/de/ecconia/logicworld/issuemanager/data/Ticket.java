package de.ecconia.logicworld.issuemanager.data;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.logicworld.issuemanager.downloader.IssueDownloader;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class Ticket
{
	//Issue data:
	private String id;
	private int number;
	private Author author;
	//Issue content:
	private Type type;
	private String title;
	private String body;
	//Issue dev state:
	private boolean open;
	private String closedVersion;
	private List<Tag> tags = new LinkedList<>();
	
	private List<Comment> comments;
	
	public Ticket(JSONObject json)
	{
		//Ignore "product", since currently only dealing with game issues.
		
		//General information:
		id = json.getString("id");
		number = json.getInt("number");
		type = Type.fromJSON(json);
		//Issue state:
		open = json.getBool("isOpen");
		closedVersion = json.getString("closedVersion");
		if(open && !closedVersion.isEmpty())
		{
			throw new RuntimeException("Closed issue, with version string?");
		}
		//Tags:
		JSONArray tagsJSON = json.getArray("tags");
		for(Object node : tagsJSON.getEntries())
		{
			JSONObject tagJSON = JSONArray.asObject(node);
			tags.add(new Tag(tagJSON));
		}
		//Issue content:
		title = json.getString("title");
		author = new Author(json.getObject("author"));
		//Content:
		body = json.getString("body");
		
		//TODO: 'product', if other type of issues get support, the category is relevant.
		//TODO: 'rating', currently neither comments not like state are parsed.
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void debug()
	{
		System.out.print("https://logicworld.net/tracker/" + id + "");
		System.out.print(" by \033[38;2;150;150;150m" + author.getName() + "\033[m");
		for(Tag tag : tags)
		{
			Color color = tag.getColor();
			System.out.print(" [\033[38;2;" + color.getRed() + ";" + color.getGreen() + ";" + color.getBlue() + "m" + tag.getName() + "\033[m]");
		}
		System.out.println();
		System.out.print(" -> ");
		if(type == Type.FEATURE)
		{
			System.out.print("\033[38;2;100;100;255m[FEATURE]\033[m");
		}
		else
		{
			System.out.print("\033[38;2;255;100;100m[BUG]\033[m");
		}
		System.out.println(" \033[38;2;255;255;255m" + title + "\033[m");
	}
	
	public List<Tag> getTags()
	{
		return tags;
	}
	
	public Type getType()
	{
		return type;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public Author getAuthor()
	{
		return author;
	}
	
	public String getClosedVersion()
	{
		return closedVersion;
	}
	
	public String getBody()
	{
		return body;
	}
	
	public String getId()
	{
		return id;
	}
	
	public boolean isOpen()
	{
		return open;
	}
	
	public void loadComments()
	{
		if(comments == null)
		{
			comments = IssueDownloader.loadCommentsFor(id);
		}
	}
	
	public List<Comment> getComments()
	{
		return comments;
	}
}
