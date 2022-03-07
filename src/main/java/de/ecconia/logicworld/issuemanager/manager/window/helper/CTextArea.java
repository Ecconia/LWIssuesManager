package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

public class CTextArea extends JTextArea
{
	public CTextArea(String text, boolean editable)
	{
		super(text);
		
		//Basic setup:
		setLineWrap(true);
		setWrapStyleWord(true);
		setBackground(Color.darkGray);
		setForeground(Color.white);
		setCaretColor(Color.white);
		setSelectionColor(new Color(0, 100, 0));
		setSelectedTextColor(Color.white);
		
		//Handle editable:
		if(editable)
		{
			setBorder(new LineBorder(Color.darkGray, 3));
		}
		else
		{
			setEditable(false);
		}
	}
	
	//This is like a very filthy hack. But no I had to use JTextArea, cause reasons. (Better formatting, less HTML).
	public void makeUnfocusable()
	{
		setFocusable(false);
		addMouseListener(new MouseListener()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				forwardEvent(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e)
			{
				forwardEvent(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e)
			{
				forwardEvent(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e)
			{
				forwardEvent(e);
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				forwardEvent(e);
			}
		});
		addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
				forwardEvent(e);
			}
			
			@Override
			public void mouseMoved(MouseEvent e)
			{
				forwardEvent(e);
			}
		});
	}
	
	private void forwardEvent(MouseEvent e)
	{
		Point transfer = SwingUtilities.convertPoint(CTextArea.this, e.getPoint(), getParent());
		MouseEvent ee = new MouseEvent(getParent(), e.getID(), e.getWhen(), e.getModifiersEx(), transfer.x, transfer.y, e.getClickCount(), false);
		getParent().dispatchEvent(ee);
	}
}
