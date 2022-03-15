package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.data.Tag;
import de.ecconia.logicworld.issuemanager.data.Type;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedTicket;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CLabel;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CTextArea;
import de.ecconia.logicworld.issuemanager.manager.window.layout.FillWidthFlowLayout;
import de.ecconia.logicworld.issuemanager.manager.window.layout.RowsWidthFillLayout;
import de.ecconia.logicworld.issuemanager.manager.window.viewer.Refreshable;
import de.ecconia.logicworld.issuemanager.manager.window.viewer.TicketViewer;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class TicketBox extends JPanel implements Refreshable
{
	private final WrappedTicket ticket;
	
	private TicketColumn currentColumn;
	
	//Refreshables:
	private final JLabel type;
	private final CTextArea title;
	private final CTextArea shortComment;
	
	public TicketBox(WrappedTicket ticket)
	{
		this.ticket = ticket;
		
		setBackground(Color.gray);
		setBorder(new LineBorder(Color.darkGray, 5));
		setLayout(new RowsWidthFillLayout(this));
		
		//Top section:
		{
			JPanel headerBox = new JPanel();
			headerBox.setBorder(new EmptyBorder(0, 0, 0, 0));
			headerBox.setBackground(Color.gray);
			headerBox.setLayout(new FillWidthFlowLayout(this, 3));
			add(headerBox);
			
			//ID:
			JLabel number = new CLabel("#" + ticket.getOriginal().getNumber());
			number.setBorder(new EmptyBorder(0, 0, 0, 0));
			number.setForeground(Color.darkGray);
			headerBox.add(number);
			
			//Type:
			type = new CLabel();
			type.setHorizontalAlignment(JLabel.LEFT);
			Type actualType = ticket.getType();
			type.setText(actualType.name());
			type.setForeground(actualType.getColor());
			type.setBorder(new EmptyBorder(0, 0, 0, 0));
			headerBox.add(type);
			
			//Closed:
			if(!ticket.getOriginal().isOpen())
			{
				String message = ticket.getOriginal().getClosedVersion();
				if(message == null || message.isEmpty())
				{
					message = "CLOSED";
				}
				JLabel closedTag = new CLabel(message);
				closedTag.setForeground(Color.red);
				closedTag.setBorder(new EmptyBorder(0, 0, 0, 0));
				headerBox.add(closedTag);
			}
		}
		
		title = new CTextArea(ticket.getTitle(), false);
		title.setFont(title.getFont().deriveFont(Font.BOLD)); //Make the field bold.
		title.setBackground(Color.gray); //Default here is dark gray, hence overwrite.
		title.setBorder(new EmptyBorder(0, 2, 0, 2)); //Add some padding at the sides.
		title.makeUnfocusable();
		add(title);
		
		shortComment = new CTextArea(ticket.getShortComment(), false);
		shortComment.setBackground(Color.gray); //Default here is dark gray, hence overwrite.
		shortComment.setBorder(new EmptyBorder(0, 2, 0, 2)); //Add some padding at the sides.
		shortComment.makeUnfocusable();
		shortComment.setVisible(ticket.hasShortComment());
		add(shortComment);
		
		//Bottom section:
		{
			JPanel tagBox = new JPanel();
			tagBox.setBorder(new EmptyBorder(0, 0, 0, 0));
			tagBox.setBackground(Color.gray);
			tagBox.setLayout(new FillWidthFlowLayout(this, 3));
			add(tagBox);
			for(Tag tag : ticket.getOriginal().getTags())
			{
				JLabel tagText = new CLabel(tag.getName());
				tagText.setForeground(tag.getColor());
				tagBox.add(tagText);
			}
		}
		
		DragGestureListener dragListener = new DragGestureListener()
		{
			@Override
			public void dragGestureRecognized(DragGestureEvent e)
			{
				BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_3BYTE_BGR);
				Graphics2D g = image.createGraphics();
				paint(g);
				g.dispose();
				//Cannot remove ticket already, because then a lot of things related to mouse events break.
				// Thank you Swing.
				ManagerGUI.instance.startDragging(TicketBox.this, image, e.getDragOrigin());
			}
		};
		MouseAdapter clickHandler = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0)
				{
					try
					{
						if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
						{
							Desktop.getDesktop().browse(new URI("https://logicworld.net/tracker/" + ticket.getOriginal().getNumber()));
						}
						else
						{
							System.out.println("Cannot open browser, insufficient capabilities.");
						}
					}
					catch(Exception ex)
					{
						System.out.println("Could not open browser, because an exception occurred:");
						ex.printStackTrace();
					}
				}
				else
				{
					new TicketViewer(ticket, TicketBox.this);
				}
			}
		};
		
		//Choosing Actions randomly "I buy all".
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK, dragListener);
		addMouseListener(clickHandler);
		//Text areas need this special registrations, to overwrite their DnD & MouseHandling stuff:
		new DragSource().createDefaultDragGestureRecognizer(title, DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK, dragListener);
		title.addMouseListener(clickHandler);
		new DragSource().createDefaultDragGestureRecognizer(shortComment, DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK, dragListener);
		shortComment.addMouseListener(clickHandler);
	}
	
	@Override
	public void refreshContent()
	{
		type.setText(ticket.getType().name());
		type.setForeground(ticket.getType().getColor());
		
		title.setText(ticket.getTitle());
		
		shortComment.setText(ticket.getShortComment());
		shortComment.setVisible(ticket.hasShortComment());
		
		invalidate();
		revalidate();
		repaint();
	}
	
	public WrappedTicket getTicket()
	{
		return ticket;
	}
	
	public TicketColumn getCurrentColumn()
	{
		return currentColumn;
	}
	
	public void setSelected(boolean selected)
	{
		setBorder(new LineBorder(selected ? Color.red : Color.darkGray, 5));
	}
	
	public void setCurrentColumn(TicketColumn ticketColumn)
	{
		currentColumn = ticketColumn;
	}
}
