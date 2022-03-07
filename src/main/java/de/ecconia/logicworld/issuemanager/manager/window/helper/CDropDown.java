package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class CDropDown<T> extends JComboBox<T>
{
	public CDropDown(T[] items)
	{
		super(items);
		
		setUI(new BasicComboBoxUI()
		{
			@Override
			public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus)
			{
				//Nothing to do here.
			}
		});
		setRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if(c instanceof JLabel label)
				{
					label.setText(" " + label.getText());
					
					//We do not want to highlight the selection focus:
					list.setSelectionForeground(Color.white);
					list.setSelectionBackground(Color.darkGray);
					
					label.setForeground(Color.white);
					label.setBackground(Color.darkGray);
					if(isSelected)
					{
						label.setBackground(Color.darkGray.brighter());
					}
					setBorder(new EmptyBorder(0, 0, 0, 0));
					return label;
				}
				return c;
			}
		});
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBackground(Color.darkGray);
		setForeground(Color.white);
	}
}
