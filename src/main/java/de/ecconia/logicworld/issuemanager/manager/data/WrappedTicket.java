package de.ecconia.logicworld.issuemanager.manager.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import de.ecconia.logicworld.issuemanager.data.Comment;
import de.ecconia.logicworld.issuemanager.data.Ticket;
import de.ecconia.logicworld.issuemanager.data.Type;

public class WrappedTicket
{
	private final Ticket original;
	
	//Dirty, but efficient way of distributing this reused component in the window system:
	private JComponent component;
	
	private Type customType;
	private String customTitle;
	private String customBody;
	
	//Custom stuff:
	private String shortComment;
	
	private String ticketMaintainerComment;
	
	private List<WrappedComment> comments;
	
	public WrappedTicket(Ticket original)
	{
		this.original = original;
	}
	
	public Ticket getOriginal()
	{
		return original;
	}
	
	public String getTitle()
	{
		if(customTitle != null)
		{
			return customTitle;
		}
		return original.getTitle();
	}
	
	public Type getType()
	{
		if(customType != null)
		{
			return customType;
		}
		return original.getType();
	}
	
	public String getShortComment()
	{
		return shortComment == null ? "" : shortComment;
	}
	
	public void setShortComment(String shortComment)
	{
		this.shortComment = "".equals(shortComment) ? null : shortComment;
	}
	
	public boolean hasShortComment()
	{
		return shortComment != null;
	}
	
	public void setComponent(JComponent component)
	{
		this.component = component;
	}
	
	public JComponent getComponent()
	{
		return component;
	}
	
	public String getBody()
	{
		if(customBody != null)
		{
			return customBody;
		}
		return original.getBody();
	}
	
	public void overwriteType(Type type)
	{
		if(type == original.getType())
		{
			customType = null;
		}
		else
		{
			customType = type;
		}
	}
	
	public boolean isModified()
	{
		return customType != null || customTitle != null || customBody != null || shortComment != null;
	}
	
	public Type getRawType()
	{
		return customType;
	}
	
	public String getRawTitle()
	{
		return customTitle;
	}
	
	public String getRawBody()
	{
		return customBody;
	}
	
	public void overwriteTitle(String title)
	{
		if(title == null)
		{
			customTitle = null;
		}
		else if(title.equals(original.getTitle()))
		{
			customTitle = null;
		}
		else
		{
			customTitle = title;
		}
	}
	
	public void overwriteBody(String body)
	{
		if(body == null)
		{
			customBody = null;
		}
		else if(body.equals(original.getBody()))
		{
			customBody = null;
		}
		else
		{
			customBody = body;
		}
	}
	
	public void loadComments()
	{
		original.loadComments();
		Map<String, WrappedComment> wrappedComments = new HashMap<>();
		for(Comment comment : original.getComments())
		{
			wrappedComments.put(comment.getId(), new WrappedComment(comment));
		}
		comments = new ArrayList<>();
		for(WrappedComment comment : wrappedComments.values())
		{
			String parentKey = comment.getOriginal().getParentID();
			if(parentKey != null && !parentKey.isEmpty())
			{
				WrappedComment parent = wrappedComments.get(parentKey);
				if(parent == null)
				{
					throw new RuntimeException("Comment '" + comment.getOriginal().getId() + "' has parent '" + parentKey + "', but its not known in this comment section of object '" + getOriginal().getId() + "'.");
				}
				parent.addComment(comment);
			}
			else
			{
				comments.add(comment);
			}
		}
		for(WrappedComment comment : wrappedComments.values())
		{
			comment.sortComments();
		}
		comments.sort((o1, o2) -> (int) (o2.getOriginal().getCreationTime() - o1.getOriginal().getCreationTime()));
	}
	
	public List<WrappedComment> getComments()
	{
		return comments;
	}
}
