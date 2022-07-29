package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;

import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import de.ecconia.logicworld.issuemanager.manager.window.ManagerGUI;

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
		setFont(getFont().deriveFont((float) ManagerGUI.fontSize));
		
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
