package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.manager.Manager;
import de.ecconia.logicworld.issuemanager.manager.data.CategoryGroup;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedTicket;
import de.ecconia.logicworld.issuemanager.manager.window.dnd.DragPane;
import de.ecconia.logicworld.issuemanager.manager.window.layout.RowsWidthFillLayout;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class ManagerGUI
{
	public static ManagerGUI instance;
	
	private final Manager manager;
	private final DragPane dragPane;
	private final JPanel contentPane;
	private final GroupBar groupBar;
	
	private ColumnContainer currentGroup;
	
	public ManagerGUI(Manager manager)
	{
		instance = this;
		this.manager = manager;
		
		for(WrappedTicket ticket : manager.getTickets())
		{
			ticket.setComponent(new TicketBox(ticket));
		}
		
		//Setup window:
		JFrame window = new JFrame("LogicWorld Ticket-Manager");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setMinimumSize(new Dimension(250, 200));
		window.setPreferredSize(new Dimension(800, 600));
		window.getContentPane().setLayout(new BorderLayout());
		dragPane = new DragPane(window.getContentPane());
		window.setGlassPane(dragPane);
		
		//Add content:
		contentPane = new JPanel();
		contentPane.setBackground(Color.darkGray);
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout());
		{
			JPanel topBars = new JPanel(new RowsWidthFillLayout(window));
			contentPane.add(topBars, BorderLayout.NORTH);
			
			//TODO: Also register this as field, in case that something gets deleted - or better register a listener to the manager.
			topBars.add(new FilterBar(manager, this));
			
			groupBar = new GroupBar(manager, this);
			topBars.add(groupBar);
		}
		JScrollPane scroller = new JScrollPane(contentPane);
		scroller.getHorizontalScrollBar().setUnitIncrement(10);
		scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
		//Dirty fix for horizontal scrolling:
		{
			Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener()
			{
				@Override
				public void eventDispatched(AWTEvent e)
				{
					if(!window.isActive())
					{
						return;
					}
					if(e instanceof MouseWheelEvent event)
					{
						if((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0)
						{
							event.consume(); //Lets process this somewhere else.
							Point transfer = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), scroller);
							MouseWheelEvent eventCopy = new MouseWheelEvent(scroller, event.getID(), event.getWhen(), event.getModifiersEx(), transfer.x, transfer.y, event.getXOnScreen(), event.getYOnScreen(), event.getClickCount(), false, event.getScrollType(), event.getScrollAmount(), event.getWheelRotation());
							//Dispatching does not work, instead directly forward it to the listeners. Probably the better solution anyway.
							for(MouseWheelListener l : scroller.getMouseWheelListeners())
							{
								l.mouseWheelMoved(eventCopy);
							}
						}
					}
				}
			}, AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		}
		window.getContentPane().add(scroller);
		
		//Listeners:
		{
			manager.getFilterManager().addFilterAppliedListener(() -> {
				contentPane.invalidate();
				contentPane.revalidate();
				contentPane.repaint();
			});
		}
		
		//Make visible:
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	
	public void startDragging(TicketBox ticket, BufferedImage image, Point offset)
	{
		dragPane.setImage(image, offset, ticket);
	}
	
	public void setCurrentGroup(CategoryGroup group)
	{
		if(currentGroup != null && currentGroup.getGroup() == group)
		{
			// System.out.println("Attempted to display group, but it is already displayed.");
			return;
		}
		if(currentGroup != null)
		{
			contentPane.remove(currentGroup);
		}
		currentGroup = new ColumnContainer(this, group);
		contentPane.add(currentGroup);
		
		contentPane.invalidate();
		contentPane.revalidate();
		contentPane.repaint();
	}
	
	public void deleteGroup(ColumnContainer columnContainer)
	{
		if(columnContainer == currentGroup)
		{
			contentPane.remove(columnContainer);
			currentGroup = null;
			contentPane.invalidate();
			contentPane.revalidate();
			contentPane.repaint();
		}
		groupBar.removeButtonFor(columnContainer.getGroup().getName());
		manager.deleteGroup(columnContainer.getGroup());
	}
	
	public Manager getManager()
	{
		return manager;
	}
}
