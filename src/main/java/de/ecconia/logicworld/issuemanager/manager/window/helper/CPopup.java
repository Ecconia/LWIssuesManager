package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

public class CPopup extends JPopupMenu
{
	public CPopup()
	{
		setLayout(new BorderLayout());
		setBorder(new LineBorder(Color.black, 1));
		removeAll();
	}
}
