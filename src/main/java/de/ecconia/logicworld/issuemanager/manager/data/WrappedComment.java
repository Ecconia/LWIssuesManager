package de.ecconia.logicworld.issuemanager.manager.data;

import de.ecconia.logicworld.issuemanager.data.Comment;
import java.util.ArrayList;
import java.util.List;

public class WrappedComment
{
	private final Comment comment;
	
	private final List<WrappedComment> childComments = new ArrayList<>();
	
	public WrappedComment(Comment comment)
	{
		this.comment = comment;
	}
	
	public Comment getOriginal()
	{
		return comment;
	}
	
	protected void addComment(WrappedComment comment)
	{
		childComments.add(comment);
	}
	
	protected void sortComments()
	{
		childComments.sort((o1, o2) -> (int) (o2.getOriginal().getCreationTime() - o1.getOriginal().getCreationTime()));
	}
	
	public List<WrappedComment> getChildComments()
	{
		return childComments;
	}
}
