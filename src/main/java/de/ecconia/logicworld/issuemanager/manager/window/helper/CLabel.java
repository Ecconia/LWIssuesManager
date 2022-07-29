package de.ecconia.logicworld.issuemanager.manager.window.helper;

import javax.swing.JLabel;

import de.ecconia.logicworld.issuemanager.manager.window.ManagerGUI;

public class CLabel extends JLabel
{
	public CLabel()
	{
		setFont(getFont().deriveFont((float) ManagerGUI.fontSize));
	}
	
	public CLabel(String text)
	{
		super(text);
		
		setFont(getFont().deriveFont((float) ManagerGUI.fontSize));
	}
}
