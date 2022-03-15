package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.IssueManager;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
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
	private final GroupBar groupBar;
	private final JPanel mainContent;
	
	private ColumnContainer currentGroup;
	
	public static int fontSize = 12;
	
	public ManagerGUI(Manager manager)
	{
		instance = this;
		this.manager = manager;
		
		try
		{
			initializeConfig();
		}
		catch(IOException e)
		{
			System.out.println("Was not able to parse config files, see stacktrace:");
			e.printStackTrace(System.out);
		}
		
		for(WrappedTicket ticket : manager.getTickets())
		{
			ticket.setComponent(new TicketBox(ticket));
		}
		
		//Setup window:
		JFrame window = new JFrame("LogicWorld Ticket-Manager Version: " + loadVersion());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setMinimumSize(new Dimension(250, 200));
		window.setPreferredSize(new Dimension(800, 600));
		window.getContentPane().setLayout(new BorderLayout());
		dragPane = new DragPane(window.getContentPane());
		window.setGlassPane(dragPane);
		
		//Add content:
		JPanel contentPane = new JPanel();
		contentPane.setBackground(Color.darkGray);
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout());
		{
			JPanel topBars = new JPanel(new RowsWidthFillLayout(window));
			topBars.setBackground(Color.gray);
			contentPane.add(topBars, BorderLayout.NORTH);
			
			//TODO: Also register this as field, in case that something gets deleted - or better register a listener to the manager.
			topBars.add(new FilterBar(manager, this));
			
			groupBar = new GroupBar(manager, this);
			topBars.add(groupBar);
			
		}
		mainContent = new JPanel();
		mainContent.setBackground(Color.darkGray);
		mainContent.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainContent.setLayout(new BorderLayout());
		JScrollPane scroller = new JScrollPane(mainContent);
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
		contentPane.add(scroller);
		window.getContentPane().add(contentPane);
		
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
			mainContent.remove(currentGroup);
		}
		currentGroup = new ColumnContainer(this, group);
		mainContent.add(currentGroup);
		
		mainContent.invalidate();
		mainContent.revalidate();
		mainContent.repaint();
	}
	
	public void deleteGroup(ColumnContainer columnContainer)
	{
		if(columnContainer == currentGroup)
		{
			mainContent.remove(columnContainer);
			currentGroup = null;
			mainContent.invalidate();
			mainContent.revalidate();
			mainContent.repaint();
		}
		groupBar.removeButtonFor(columnContainer.getGroup().getName());
		manager.deleteGroup(columnContainer.getGroup());
	}
	
	public Manager getManager()
	{
		return manager;
	}
	
	private void initializeConfig() throws IOException
	{
		//TODO: Register file watcher.
		Path configFolder = IssueManager.configFolder;
		Path columnWidthFile = configFolder.resolve("columnWidth.txt");
		if(!Files.exists(configFolder))
		{
			Files.createDirectories(configFolder);
		}
		
		if(!Files.exists(columnWidthFile))
		{
			Files.writeString(columnWidthFile, String.valueOf(TicketColumn.width));
		}
		else
		{
			String content = Files.readString(columnWidthFile);
			if(content != null && !content.isBlank())
			{
				try
				{
					TicketColumn.width = Integer.parseInt(content.replace('\n', ' ').trim());
				}
				catch(NumberFormatException e)
				{
					System.out.println("Could not parse column width: " + e.getMessage());
				}
			}
		}
		
		Path fileSizeFile = configFolder.resolve("fontSize.txt");
		if(!Files.exists(fileSizeFile))
		{
			Files.writeString(fileSizeFile, String.valueOf(fontSize));
		}
		else
		{
			String content = Files.readString(fileSizeFile);
			if(content != null && !content.isBlank())
			{
				try
				{
					fontSize = Integer.parseInt(content.replace('\n', ' ').trim());
				}
				catch(NumberFormatException e)
				{
					System.out.println("Could not parse column width: " + e.getMessage());
				}
			}
		}
	}
	
	private static String loadVersion()
	{
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		try(InputStream is = classLoader.getResourceAsStream("version.txt"))
		{
			if(is == null)
			{
				System.out.println("Version file is not existing.");
				return "<unknown>";
			}
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
			{
				return reader.readLine(); //There is only one line.
			}
		}
		catch(Exception e)
		{
			System.out.println("Exception while loading version:");
			e.printStackTrace(System.out);
			return "<unknown>";
		}
	}
}
