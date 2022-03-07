package de.ecconia.logicworld.issuemanager.manager.window;

import de.ecconia.logicworld.issuemanager.manager.Manager;
import de.ecconia.logicworld.issuemanager.manager.data.CategoryGroup;
import de.ecconia.logicworld.issuemanager.manager.window.helper.CButton;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//TODO: Highlight current group.
public class GroupBar extends JPanel
{
	public GroupBar(Manager manager, ManagerGUI window)
	{
		//General setup:
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(Color.gray);
		
		//Group buttons:
		ActionListener groupSwitchAction = e -> window.setCurrentGroup(manager.getGroup(((JButton) e.getSource()).getText()));
		for(CategoryGroup group : manager.getGroups())
		{
			CButton button = new CButton(group.getName());
			button.addActionListener(groupSwitchAction);
			add(button);
		}
		
		//Add button:
		CButton addNewGroup = new CButton("Add category group");
		addNewGroup.addActionListener((unused) -> {
			String input = JOptionPane.showInputDialog(null, "Name for the new group:");
			if(input == null)
			{
				return;
			}
			if(manager.isGroupExisting(input))
			{
				JOptionPane.showMessageDialog(null, "Group '" + input + "' does already exist.");
				return;
			}
			manager.addNewGroup(input);
			CButton group = new CButton(input);
			group.addActionListener(groupSwitchAction);
			add(group, getComponentCount() - 1);
			invalidate();
			revalidate();
			repaint();
		});
		add(addNewGroup);
	}
	
	public void removeButtonFor(String name)
	{
		for(int i = 0; i < getComponentCount(); i++)
		{
			if(name.equals(((JButton) getComponent(i)).getText()))
			{
				remove(i);
				invalidate();
				revalidate();
				repaint();
				return;
			}
		}
	}
}
