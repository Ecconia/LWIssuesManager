package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.manager.data.Category;
import de.ecconia.logicworld.issuemanager.manager.data.CategoryGroup;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ColumnContainer extends JPanel
{
	private final CategoryGroup group;
	private final Map<Category, TicketColumn> categoryMap = new HashMap<>();
	
	private final JPanel content;
	
	public ColumnContainer(ManagerGUI window, CategoryGroup group)
	{
		this.group = group;
		
		setLayout(new BorderLayout());
		setBackground(Color.red);
		
		content = new JPanel();
		{
			JPanel header = new JPanel();
			header.setLayout(new FlowLayout(FlowLayout.LEFT));
			header.setBackground(Color.gray);
			
			header.add(new JLabel("Group: " + group.getName() + "     "));
			CButton addCategory = new CButton("Add category");
			addCategory.addActionListener(e -> {
				String input = JOptionPane.showInputDialog(null, "Name for the new category:");
				if(input == null)
				{
					return;
				}
				if(group.isCategoryExisting(input))
				{
					JOptionPane.showMessageDialog(null, "Category '" + input + "' does already exist.");
					return;
				}
				Category category = group.addNewCategory(input);
				content.add(new TicketColumn(this, category));
				content.invalidate();
				content.revalidate();
				content.repaint();
			});
			header.add(addCategory);
			
			CButton deleteCategoryGroup = new CButton("Delete group");
			deleteCategoryGroup.addActionListener(e -> {
				int choice = JOptionPane.showConfirmDialog(null, "Do you really want to delete '" + group.getName() + "'?","Delete group?", JOptionPane.YES_NO_OPTION);
				if(choice != JOptionPane.YES_OPTION)
				{
					//Abort.
					return;
				}
				//Okay deleting the group on request now!
				window.deleteGroup(this);
			});
			header.add(deleteCategoryGroup);
			
			add(header, BorderLayout.NORTH);
		}
		
		{
			content.setLayout(new ColumnLayout());
			content.setBackground(Color.GREEN);
			
			for(Category cat : group.getCategories())
			{
				TicketColumn ticketColumn = new TicketColumn(this, cat);
				categoryMap.put(cat, ticketColumn);
				content.add(ticketColumn);
				ticketColumn.addTickets(cat.getTickets(), false); //false = silent, no gui update
			}
			
			add(content);
		}
	}
	
	public CategoryGroup getGroup()
	{
		return group;
	}
	
	public void delete(TicketColumn column)
	{
		group.delete(column.getCategory()); //This also modifies the unsorted column.
		((TicketColumn) content.getComponent(0)).addTickets(column.getCategory().getTickets(), false);
		
		content.remove(column);
		content.invalidate();
		content.revalidate();
		content.repaint();
	}
	
	public static class ColumnLayout implements LayoutManager
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
			//This is the jokester which gets called.
			// However we only consider the minimum size here.
			return minimumLayoutSize(parent);
		}
		
		@Override
		public Dimension minimumLayoutSize(Container parent)
		{
			int widestOne = 0;
			int tallestOne = 0;
			for(Component child : parent.getComponents())
			{
				Dimension minimum = child.getMinimumSize();
				if(minimum.width > widestOne)
				{
					widestOne = minimum.width;
				}
				if(minimum.height > tallestOne)
				{
					tallestOne = minimum.height;
				}
			}
			return new Dimension(parent.getComponentCount() * widestOne, tallestOne);
		}
		
		@Override
		public void layoutContainer(Container parent)
		{
			if(parent.getComponentCount() == 0)
			{
				return;
			}
			int widestOne = 0;
			int tallestOne = 0;
			for(Component child : parent.getComponents())
			{
				Dimension minimum = child.getMinimumSize();
				if(minimum.width > widestOne)
				{
					widestOne = minimum.width;
				}
				if(minimum.height > tallestOne)
				{
					tallestOne = minimum.height;
				}
			}
			
			Dimension parentDim = parent.getSize();
			if(parentDim.height > tallestOne)
			{
				tallestOne = parentDim.height;
			}
			int totalWidth = parent.getComponentCount() * widestOne;
			int specialExtra = 0;
			if(parentDim.width > totalWidth)
			{
				widestOne = parentDim.width / parent.getComponentCount();
				specialExtra = parentDim.width % parent.getComponentCount();
			}
			
			int x = 0;
			for(int i = 0; i < parent.getComponentCount(); i++)
			{
				Component child = parent.getComponent(i);
				int thisWidth = widestOne;
				if(specialExtra > 0)
				{
					//Equally distribute pixels not dividable by component count to the first N components.
					thisWidth += 1;
					specialExtra--;
				}
				child.setBounds(x, 0, thisWidth, tallestOne);
				x += thisWidth;
			}
		}
	}
}
