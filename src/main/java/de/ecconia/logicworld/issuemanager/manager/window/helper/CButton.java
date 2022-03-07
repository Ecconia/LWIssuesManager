package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class CButton extends JButton
{
	public CButton(String name)
	{
		super(name);
		
		setFocusPainted(false);
		setBorder(new EmptyBorder(4, 6, 4, 6));
		setBackground(Color.darkGray);
		setForeground(Color.white);
		
		addMouseListener(new MouseAdapter()
		{
			public void mouseEntered(MouseEvent evt)
			{
				setBackground(Color.darkGray.brighter());
			}
			
			public void mouseExited(MouseEvent evt)
			{
				setBackground(Color.darkGray);
			}
		});
	}
}
