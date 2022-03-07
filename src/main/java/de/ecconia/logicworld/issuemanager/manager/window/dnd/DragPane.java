package de.ecconia.logicworld.issuemanager.manager.window.dnd;

import de.ecconia.logicworld.issuemanager.manager.window.TicketBox;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class DragPane extends JComponent
{
	private final Container content;
	
	private Dropable lastDropable;
	private BufferedImage image;
	private Point pos;
	private Point offset;
	private TicketBox ticket;
	
	public DragPane(Container content)
	{
		this.content = content;
		setLayout(null);
		
		//Dirty, but sadly Java-Swing is very hostile when it comes to event distribution, so lets be hostile ourself.
		long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener()
		{
			public void eventDispatched(AWTEvent e)
			{
				if(image != null)
				{
					MouseEvent event = (MouseEvent) e;
					if(event.getID() == MouseEvent.MOUSE_RELEASED)
					{
						//Must have been released!
						setVisible(false);
						image = null;
						if(lastDropable != null)
						{
							lastDropable.stopHighlight();
							Point childPoint = SwingUtilities.convertPoint(DragPane.this, pos, (Component) lastDropable);
							lastDropable.drop(childPoint, ticket);
							lastDropable = null;
						}
						ticket = null;
						pos = null;
					}
					else if(event.getID() == MouseEvent.MOUSE_DRAGGED)
					{
						Point newPos = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), DragPane.this);
						if(!newPos.equals(pos))
						{
							{
								//Find the target position!
								Dropable dropable = getDropableAt(newPos);
								if(dropable != lastDropable)
								{
									if(lastDropable != null)
									{
										lastDropable.stopHighlight();
									}
									lastDropable = dropable;
								}
								if(dropable != null)
								{
									Point childPoint = SwingUtilities.convertPoint(DragPane.this, newPos, (Component) dropable);
									dropable.updateHighlight(childPoint);
								}
							}
							pos = newPos;
							repaint();
						}
					}
					event.consume();
				}
			}
		}, eventMask);
	}
	
	private Dropable getDropableAt(Point position)
	{
		Component comp = SwingUtilities.getDeepestComponentAt(content, position.x, position.y);
		do
		{
			if(comp == null || comp == content)
			{
				return null;
			}
			if(comp instanceof Dropable)
			{
				return (Dropable) comp;
			}
			comp =  comp.getParent();
		}
		while(true);
	}
	
	public void setImage(BufferedImage image, Point offset, TicketBox ticket)
	{
		setVisible(true);
		this.offset = offset;
		this.image = image;
		this.ticket = ticket;
		repaint();
	}
	
	@Override
	public void paint(Graphics g)
	{
		if(image != null && pos != null)
		{
			g.drawImage(image, pos.x - offset.x, pos.y - offset.y, image.getWidth(), image.getHeight(), null);
		}
	}
}
