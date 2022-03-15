package de.ecconia.logicworld.issuemanager.manager.window.viewer;

import de.ecconia.logicworld.issuemanager.manager.data.WrappedComment;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CLabel;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CTextArea;
import de.ecconia.logicworld.issuemanager.manager.window.helper.OutlineBorder;
import de.ecconia.logicworld.issuemanager.manager.window.layout.RowsWidthFillLayout;
import java.awt.Color;
import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CommentComponent extends JPanel
{
	public CommentComponent(WrappedComment comment, Container reference)
	{
		setLayout(new RowsWidthFillLayout(reference));
		setBorder(new OutlineBorder());
		
		JLabel sender = new CLabel("From: " + comment.getOriginal().getAuthor().getName());
		sender.setOpaque(true);
		sender.setForeground(Color.white);
		sender.setBackground(Color.darkGray);
		add(sender);
		
		add(new CTextArea(comment.getOriginal().getBody(), false));
		
		if(!comment.getChildComments().isEmpty())
		{
			JPanel commentSection = new JPanel();
			commentSection.setLayout(new RowsWidthFillLayout(commentSection));
			for(WrappedComment childComment : comment.getChildComments())
			{
				commentSection.add(new CommentComponent(childComment, commentSection));
			}
			add(commentSection);
		}
	}
}
