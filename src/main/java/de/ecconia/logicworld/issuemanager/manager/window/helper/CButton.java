package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

public class CButton extends JButton
{
	private boolean active;
	private boolean hover;
	
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
				hover = true;
				updateBackground();
			}
			
			public void mouseExited(MouseEvent evt)
			{
				hover = false;
				updateBackground();
			}
		});
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
		updateBackground();
	}
	
	private void updateBackground()
	{
		if(active)
		{
			setBackground(hover ? getForeground().darker() : getForeground().darker().darker());
		}
		else
		{
			setBackground(hover ? Color.darkGray.brighter() : Color.darkGray);
		}
		repaint();
	}
}
