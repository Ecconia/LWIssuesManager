package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class CDropDown<T> extends JPanel
{
	private final List<ActionListener> actionListeners = new LinkedList<>();
	
	private final T[] elements;
	private final JLabel currentElementCell;
	JPopupMenu popup;
	
	private int activeIndex = -1;
	
	public CDropDown(T[] elements)
	{
		super(new BorderLayout());
		
		this.elements = elements;
		if(elements.length != 0)
		{
			activeIndex = 0;
		}
		
		popup = new JPopupMenu("DropdownPopup");
		
		currentElementCell = new CLabel(elements.length == 0 ? " - Empty - " : elements[activeIndex].toString());
		currentElementCell.setOpaque(true);
		currentElementCell.setBackground(Color.darkGray);
		currentElementCell.setForeground(Color.white);
		add(currentElementCell);
		
		//Indicator element:
		//TODO: Update empty state.
		add(new DropDownArrowComponent(elements.length == 0), BorderLayout.EAST);
		
		popup.setBackground(Color.darkGray);
		popup.setBorder(new LineBorder(Color.black, 1));
		popup.setLayout(new GridLayout(0, 1));
		for(T t : elements)
		{
			popup.add(new EntryLabel(t));
		}
		
		setupEventListeners();
	}
	
	public static class DropDownArrowComponent extends JComponent
	{
		private boolean isEmpty;
		
		public DropDownArrowComponent(boolean isEmpty)
		{
			this.isEmpty = isEmpty;
		}
		
		public void setEmpty(boolean isEmpty)
		{
			this.isEmpty = isEmpty;
		}
		
		@Override
		public Dimension getPreferredSize()
		{
			int side = getHeight();
			return new Dimension(side, side);
		}
		
		@Override
		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}
		
		@Override
		public void paint(Graphics g)
		{
			g.setColor(Color.darkGray);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.white);
			
			int width = getSize().width;
			int height = getSize().height;
			//Clamp the size of the image to roughly 2px to a third of width/height (whichever is smaller).
			int size = Math.max((Math.min(height, width) - 4) / 3, 2);
			int x = (width - size) / 2;
			int y = (height - size) / 2;
			g.translate(x, y);
			if(isEmpty)
			{
				//X:
				g.drawLine(0, 0, size, size);
				g.drawLine(0, size, size, 0);
			}
			else
			{
				//Triangle:
				int mid = (size / 2);
				for(int column = size - 1, line = 0; column >= 0; column--)
				{
					g.drawLine(mid - column, line, mid + column, line);
					line++;
				}
			}
			g.translate(-x, -y);
		}
	}
	
	private void setupEventListeners()
	{
		MouseAdapter mouseHook = new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			
			@Override
			public void mouseExited(MouseEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			
			@Override
			public void mouseClicked(MouseEvent e)
			{
				//onClick and not onRelease, since the popup closes itself onPress already. -> It would open again.
				if(!popup.isVisible())
				{
					spawnPopup();
				}
			}
		};
		addMouseListener(mouseHook);
		currentElementCell.addMouseListener(mouseHook);
	}
	
	private void spawnPopup()
	{
		if(!popup.isVisible())
		{
			Insets insets = getInsets();
			Dimension size = popup.getMinimumSize();
			popup.setPreferredSize(new Dimension(Math.max(size.width, getWidth() - insets.left - insets.right), size.height));
			popup.invalidate();
			popup.revalidate();
			popup.show(this, insets.left, getHeight() - insets.bottom);
			popup.requestFocus();
		}
	}
	
	private void killPopup()
	{
		if(popup.isVisible())
		{
			popup.setVisible(false);
		}
	}
	
	public T getSelectedItem()
	{
		return elements[activeIndex];
	}
	
	private class EntryLabel extends CLabel
	{
		private boolean mouseOver;
		private final T element;
		
		public EntryLabel(T element)
		{
			super(element.toString());
			this.element = element;
			
			setOpaque(true);
			setForeground(Color.white);
			setBorder(new EmptyBorder(0, 4, 0, 4));
			
			addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseReleased(MouseEvent e)
				{
					//Activate stuffs.
					setSelectedItem(element);
					notifyActionListeners();
					killPopup(); //Done here.
				}
				
				@Override
				public void mouseEntered(MouseEvent e)
				{
					mouseOver = true;
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					repaint();
				}
				
				@Override
				public void mouseExited(MouseEvent e)
				{
					mouseOver = false;
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					repaint();
				}
			});
		}
		
		@Override
		public void paint(Graphics g)
		{
			//Update here, whenever drawn...
			if(mouseOver)
			{
				setBackground(new Color(0, 150, 0));
			}
			else if(element == elements[activeIndex])
			{
				setBackground(new Color(0, 100, 0));
			}
			else
			{
				setBackground(Color.darkGray);
			}
			super.paint(g);
		}
	}
	
	public void setSelectedItem(T element)
	{
		if(element == elements[activeIndex])
		{
			return; //Nothing to do.
		}
		for(int i = 0; i < elements.length; i++)
		{
			if(elements[i] == element)
			{
				activeIndex = i;
				currentElementCell.setText(elements[i].toString());
				currentElementCell.repaint();
				return;
			}
		}
		throw new RuntimeException("Provided argument not element of the data-array.");
	}
	
	private void notifyActionListeners()
	{
		for(ActionListener listener : actionListeners)
		{
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
		}
	}
	
	public void addActionListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener)
	{
		actionListeners.remove(listener);
	}
}
