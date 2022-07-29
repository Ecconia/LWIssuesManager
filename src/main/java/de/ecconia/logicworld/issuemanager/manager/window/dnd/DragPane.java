package de.ecconia.logicworld.issuemanager.manager.window.dnd;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import de.ecconia.logicworld.issuemanager.manager.window.TicketBox;

public class DragPane
{
	private final Container content;
	private final JDialog overlay;
	
	private Dropable lastDropable;
	private Point pos;
	private Point offset;
	private TicketBox ticket;
	
	public DragPane(JFrame parent, Container content)
	{
		this.content = content;
		
		overlay = new JDialog(parent);
		overlay.setUndecorated(true);
		overlay.setSize(100, 100);
		overlay.setBackground(Color.orange);
		overlay.setResizable(false);
		overlay.setAlwaysOnTop(true);
		
		//Dirty, but sadly Java-Swing is very hostile when it comes to event distribution, so lets be hostile ourself.
		long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
			if(overlay.isVisible())
			{
				MouseEvent event = (MouseEvent) e;
				if(event.getID() == MouseEvent.MOUSE_RELEASED)
				{
					if(lastDropable != null)
					{
						lastDropable.stopHighlight();
						Point childPoint = SwingUtilities.convertPoint(content, pos, (Component) lastDropable);
						lastDropable.drop(childPoint, ticket);
						lastDropable = null;
					}
					ticket = null;
					pos = null;
					
					overlay.setVisible(false);
					overlay.getContentPane().removeAll();
				}
				else if(event.getID() == MouseEvent.MOUSE_DRAGGED)
				{
					Point newPos = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), content);
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
								Point childPoint = SwingUtilities.convertPoint(content, newPos, (Component) dropable);
								dropable.updateHighlight(childPoint);
							}
						}
						pos = newPos;
						overlay.setLocation(event.getXOnScreen() - offset.x, event.getYOnScreen() - offset.y);
					}
				}
				event.consume();
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
			comp = comp.getParent();
		}
		while(true);
	}
	
	public void setImage(BufferedImage image, Point offset, TicketBox ticket)
	{
		this.offset = offset;
		this.ticket = ticket;
		
		overlay.getContentPane().add(new JLabel(new ImageIcon(image)));
		overlay.pack();
		overlay.setVisible(true);
	}
}
