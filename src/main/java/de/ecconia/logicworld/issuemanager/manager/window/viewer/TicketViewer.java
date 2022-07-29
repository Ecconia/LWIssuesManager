package de.ecconia.logicworld.issuemanager.manager.window.viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.ecconia.logicworld.issuemanager.data.Type;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedComment;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedTicket;
import de.ecconia.logicworld.issuemanager.manager.window.ManagerGUI;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CDropDown;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CLabel;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CTextArea;
import de.ecconia.logicworld.issuemanager.manager.window.layout.RowsWidthFillLayout;

public class TicketViewer
{
	public TicketViewer(WrappedTicket ticket, Refreshable refreshable)
	{
		JFrame window = new JFrame("Ticket: " + ticket.getOriginal().getNumber() + ": " + ticket.getOriginal().getTitle());
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setMinimumSize(new Dimension(200, 150));
		window.setPreferredSize(new Dimension(500, 500));
		
		JPanel content = new JPanel();
		JScrollPane scroller = new JScrollPane(content);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.getVerticalScrollBar().setUnitIncrement(10);
		scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
		window.add(scroller);
		
		final JPanel commentSection;
		
		{
			content.setLayout(new RowsWidthFillLayout(scroller.getViewport()));
			content.setBackground(Color.gray);
			
			//Author:
			{
				content.add(new CLabel("Author:"));
				content.add(new CTextArea(" " + ticket.getOriginal().getAuthor().getName(), false));
			}
			
			//Type:
			{
				content.add(new CLabel("Type:"));
				CDropDown<Type> dropdown = new CDropDown<>(Type.values());
				dropdown.setSelectedItem(ticket.getType());
				dropdown.addActionListener(e -> {
					ticket.overwriteType(dropdown.getSelectedItem());
					refreshable.refreshContent();
				});
				dropdown.setBorder(new LineBorder(Color.darkGray, 3));
				content.add(dropdown);
			}
			
			//Title:
			{
				content.add(new CLabel("Title:"));
				CTextArea field = new CTextArea(ticket.getTitle(), false);
				content.add(field);
			}
			
			//Short Comment
			{
				content.add(new CLabel("Short Comment:"));
				CTextArea area = new CTextArea(ticket.getShortComment(), true);
				area.getDocument().addDocumentListener(new DocumentListener()
				{
					@Override
					public void insertUpdate(DocumentEvent e)
					{
						changed();
					}
					
					@Override
					public void removeUpdate(DocumentEvent e)
					{
						changed();
					}
					
					@Override
					public void changedUpdate(DocumentEvent e)
					{
						changed();
					}
					
					private void changed()
					{
						String text = area.getText();
						ticket.setShortComment(text);
						refreshable.refreshContent();
					}
				});
				content.add(area);
			}
			
			//Body:
			{
				content.add(new CLabel("Body:"));
				CTextArea area = new CTextArea(ticket.getBody(), false);
				content.add(area);
			}
			
			//Comments:
			{
				content.add(new CLabel("Comments:"));
				commentSection = new JPanel();
				commentSection.setBackground(Color.darkGray);
				commentSection.setLayout(new RowsWidthFillLayout(scroller.getViewport()));//new BoxLayout(commentSection, BoxLayout.Y_AXIS));
				content.add(commentSection);
				commentSection.add(new CLabel("- Loading (coming soon) -"));
			}
		}
		
		window.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				content.invalidate();
				content.revalidate();
				content.repaint();
			}
		});
		
		//Make the window close on ESCAPE:
		KeyEventDispatcher ked = e -> {
			if(e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				if(window.isActive())
				{
					window.dispose();
				}
				return true;
			}
			return false;
		};
		window.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(ked);
			}
		});
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ked);
		
		//Make the window visible:
		window.pack();
		window.setLocationRelativeTo(ManagerGUI.instance.getWindow());
		window.setVisible(true);
		
		//Load the comments (async):
		Thread commentLoader = new Thread(() -> {
			try
			{
				ticket.loadComments();
			}
			catch(Exception e)
			{
				commentSection.removeAll();
				commentSection.add(new CLabel("Was not able to load comments, see console."));
				System.out.println("Was not able to load comments:");
				e.printStackTrace(System.out);
				return;
			}
			//Loading done, now adding:
			List<WrappedComment> comments = ticket.getComments();
			commentSection.removeAll();
			if(comments.isEmpty())
			{
				JLabel label = new CLabel("   No comments.   ");
				label.setForeground(Color.white);
				commentSection.add(label);
			}
			else
			{
				for(WrappedComment comment : comments)
				{
					commentSection.add(new CommentComponent(comment, commentSection));
				}
			}
			commentSection.invalidate();
			commentSection.revalidate();
			commentSection.repaint();
		}, "CommentLoader");
		commentLoader.start();
	}
}
