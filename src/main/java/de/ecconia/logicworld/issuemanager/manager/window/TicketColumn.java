package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.manager.data.Category;
import de.ecconia.logicworld.issuemanager.manager.data.CategoryGroup;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedTicket;
import de.ecconia.logicworld.issuemanager.manager.window.dnd.Dropable;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CButton;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CTextArea;
import de.ecconia.logicworld.issuemanager.manager.window.helper.LightScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collection;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class TicketColumn extends JPanel
{
	private final Category category;
	private final TicketList list;
	private final LightScrollPane scroller;
	private final int width = 150;
	private final CTextArea title; //To update the count. //TBI: Should filtered tickets be counted? I do not think so.
	
	public TicketColumn(ColumnContainer groupComponent, Category category)
	{
		this.category = category;
		setBorder(new LineBorder(Color.gray, 2));
		setLayout(new BorderLayout());
		list = new TicketList();
		
		JPanel topBar = new JPanel(new BorderLayout());
		{
			title = new CTextArea(category.getName() + " (" + category.getTickets().size() + ")", false);
			title.setFont(title.getFont().deriveFont(Font.BOLD)); //Make bold, for the good thickness.
			title.setBorder(new EmptyBorder(4, 6, 4, 6)); //Adjust to be the same nice outline as buttons have.
			topBar.add(title);
		}
		if(!category.getName().equals(CategoryGroup.defaultCategoryName))
		{
			CButton deleteCategory = new CButton("X");
			topBar.add(deleteCategory, BorderLayout.EAST);
			deleteCategory.addActionListener(e -> {
				int choice = JOptionPane.showConfirmDialog(null, "Do you really want to delete '" + category.getName() + "'?", "Delete category?", JOptionPane.YES_NO_OPTION);
				if(choice != JOptionPane.YES_OPTION)
				{
					//Abort.
					return;
				}
				//Okay deleting the category on request now!
				groupComponent.delete(this);
			});
		}
		add(topBar, BorderLayout.NORTH);
		
		setMinimumSize(new Dimension(width, 100));
		
		scroller = new LightScrollPane(list);
		scroller.setBorder(new EmptyBorder(0, 0, 0, 0));
		add(scroller);
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				list.invalidate();
				list.revalidate();
				list.repaint();
			}
		});
	}
	
	protected void updateCount()
	{
		title.setText(category.getName() + " (" + category.getTickets().size() + ")");
	}
	
	public Category getCategory()
	{
		return category;
	}
	
	public void addTickets(Collection<WrappedTicket> tickets, boolean update)
	{
		for(WrappedTicket ticket : tickets)
		{
			TicketBox ticketComponent = (TicketBox) ticket.getComponent();
			list.add(ticket.getComponent());
			ticketComponent.setCurrentColumn(this);
		}
		if(update)
		{
			list.invalidate();
			list.revalidate();
			list.repaint();
		}
	}
	
	private class TicketList extends JPanel implements Dropable
	{
		private Integer highlight = null;
		
		public TicketList()
		{
			setBackground(Color.gray.darker());
			setLayout(new ListLayout());
		}
		
		@Override
		public void updateHighlight(Point position)
		{
			int index = getIndexAt(position);
			if(index == 0)
			{
				//It is the first item, so the drop Y value is 0.
				highlight = -5;
			}
			else if(index == getComponentCount())
			{
				//We are appending to the list, so take the last entry and its height as Y value.
				Component comp = getComponent(index - 1);
				highlight = comp.getY() + comp.getHeight() - 5;
			}
			else
			{
				//We are somewhere in the middle. So just take the entry which place gonna be used as drop position.
				highlight = getComponent(index).getY() - 5;
			}
			repaint();
		}
		
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			if(highlight != null)
			{
				g.setColor(Color.red);
				g.fillRect(0, highlight, getWidth(), 10);
			}
		}
		
		private int getIndexAt(Point position)
		{
			if(getComponentCount() == 0)
			{
				return 0;
			}
			int index = 0; //The index to be returned.
			Component onComponent = null;
			if(getComponentCount() == 1)
			{
				onComponent = getComponent(0);
				if(!onComponent.isVisible())
				{
					//Only one component, and this component is invisible, add last:
					return 1;
				}
			}
			else
			{
				Component firstChild = getComponent(0);
				Component lastVisibleChild = firstChild.isVisible() ? firstChild : null; //Remember the last visible child.
				int lastVisibleIndex = 0;
				for(int i = 1; i < getComponentCount(); i++)
				{
					Component nextChild = getComponent(i);
					if(position.y < nextChild.getY())
					{
						if(lastVisibleChild == null)
						{
							//We have not yet encountered a single visible ticket, yet managed to drop before the first visible one, this means we add to first position.
							return 0;
						}
						onComponent = lastVisibleChild;
						index = lastVisibleIndex;
						break;
					}
					if(nextChild.isVisible())
					{
						lastVisibleChild = nextChild;
						lastVisibleIndex = i;
					}
				}
				if(onComponent == null)
				{
					if(lastVisibleChild == null)
					{
						//Not a single ticket in this list is visible, append last (since we dropped behind them all):
						return getComponentCount();
					}
					//We went over all components but had not been before any component. Now we can be on the last or behind the last, check:
					if(position.y > lastVisibleChild.getY() + lastVisibleChild.getHeight())
					{
						//We are behind the last component, append!
						return getComponentCount();
					}
					else
					{
						//We are on the last child:
						onComponent = lastVisibleChild;
						index = lastVisibleIndex;
					}
				}
			}
			int halfHeight = onComponent.getHeight() / 2;
			int pos = position.y - onComponent.getY();
			return pos > halfHeight ? index + 1 : index;
		}
		
		@Override
		public void stopHighlight()
		{
			highlight = null;
			repaint();
		}
		
		@Override
		public void drop(Point position, TicketBox ticket)
		{
			int index = getIndexAt(position);
			if(ticket.getCurrentColumn() == TicketColumn.this)
			{
				if(getComponentCount() > index && getComponent(index) == ticket || index != 0 && getComponentCount() > (index - 1) && getComponent(index - 1) == ticket)
				{
					// System.out.println("Trying to move component to its old position. Lets not.");
					return;
				}
				int oldIndex = getIndexOf(ticket);
				if(oldIndex < index)
				{
					index--;
				}
				//Move in data:
				category.moveTicket(oldIndex, index);
				//Move in GUI:
				remove(oldIndex);
				add(ticket, index);
			}
			else
			{
				//Move in data:
				category.add(ticket.getTicket(), index);
				//Move in GUI:
				TicketColumn oldColumn = ticket.getCurrentColumn();
				oldColumn.remove(ticket);
				oldColumn.updateCount();
				oldColumn.invalidate();
				oldColumn.revalidate();
				oldColumn.repaint();
				add(ticket, index);
				ticket.setCurrentColumn(TicketColumn.this);
			}
			updateCount();
			invalidate();
			revalidate();
			repaint();
		}
		
		private int getIndexOf(Component component)
		{
			for(int i = 0; i < getComponentCount(); i++)
			{
				if(component == getComponent(i))
				{
					return i;
				}
			}
			throw new RuntimeException("Expected component to be inside this list, but it was not.");
		}
		
		private class ListLayout implements LayoutManager
		{
			@Override
			public void addLayoutComponent(String name, Component comp)
			{
				//Idc.
			}
			
			@Override
			public void removeLayoutComponent(Component comp)
			{
				//Idc.
			}
			
			@Override
			public Dimension preferredLayoutSize(Container parent)
			{
				int totalHeight = 0;
				for(Component child : parent.getComponents())
				{
					if(!child.isVisible())
					{
						continue; //Ignore the ones which can't be seen anyway.
					}
					Dimension minimum = child.getMinimumSize();
					totalHeight += minimum.height;
				}
				return new Dimension(parent.getParent().getWidth(), totalHeight);
			}
			
			@Override
			public Dimension minimumLayoutSize(Container parent)
			{
				int totalHeight = 0;
				for(Component child : parent.getComponents())
				{
					if(!child.isVisible())
					{
						continue; //Ignore the ones which can't be seen anyway.
					}
					Dimension minimum = child.getMinimumSize();
					totalHeight += minimum.height;
				}
				return new Dimension(width, totalHeight);
			}
			
			@Override
			public void layoutContainer(Container parent)
			{
				int y = 0;
				for(Component child : parent.getComponents())
				{
					if(!child.isVisible())
					{
						child.setBounds(0, y, 0, 0); //Make super small, but preserve the position. So that the drop code can figure out the index.
						continue;
					}
					Dimension min = child.getPreferredSize();
					child.setBounds(0, y, parent.getParent().getSize().width, min.height);
					y += min.height;
				}
			}
		}
	}
}
