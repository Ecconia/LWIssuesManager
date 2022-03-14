package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
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
	
	public void makeUnfocusable()
	{
		setFocusable(false); //Prevents selection of text.
	}
}
