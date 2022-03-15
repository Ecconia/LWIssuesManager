package de.ecconia.logicworld.issuemanager.manager.window.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class FillWidthFlowLayout implements LayoutManager
{
	private int paddingX;
	private final Container reference;
	
	public FillWidthFlowLayout(Container reference, int paddingX)
	{
		this.reference = reference;
		this.paddingX = paddingX;
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp)
	{
	}
	
	@Override
	public void removeLayoutComponent(Component comp)
	{
	}
	
	private int getWidth()
	{
		return reference.getWidth() - reference.getInsets().left - reference.getInsets().right;
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		return minimumLayoutSize(parent);
	}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		Insets insets = parent.getInsets();
		int maxWidth = getWidth();
		Component[] children = parent.getComponents();
		int[] widths = new int[children.length];
		for(int i = 0; i < widths.length; i++)
		{
			widths[i] = children[i].getMinimumSize().width;
		}
		
		int totalHeight = insets.top + insets.bottom;
		int lineHeight = 0;
		int lineWidth = 0;
		int lineStartIndex = 0;
		
		for(int i = lineStartIndex; i < children.length; i++)
		{
			int childWidth = widths[i];
			if(childWidth > maxWidth)
			{
				if(lineStartIndex != i)
				{
					//Save last line.
					totalHeight += lineHeight;
				}
				
				//Child won't fit any row... so just squeeze it:
				int childHeight = children[i].getMinimumSize().height;
				totalHeight += childHeight;
				
				//Init next line:
				lineHeight = 0;
				lineWidth = 0;
				lineStartIndex = i + 1; //Cause we already handled i
			}
			else
			{
				int childWidthWithPadding = childWidth;
				if(lineStartIndex != i)
				{
					//This one is a bit tricky, we want to skip the padding, if we are dealing with the very first of the entries.
					childWidthWithPadding += paddingX;
				}
				if((lineWidth + childWidthWithPadding) > maxWidth)
				{
					//Child won't fit current line, save last line and move it to next line.
					
					//Save last line.
					totalHeight += lineHeight;
					
					//Init next line:
					lineHeight = 0;
					lineWidth = 0;
					lineStartIndex = i;
				}
				
				//Child will fit, check next one!
				lineWidth += childWidthWithPadding;
				int childHeight = children[i].getMinimumSize().height;
				if(childHeight > lineHeight)
				{
					lineHeight = childHeight;
				}
			}
		}
		
		if(lineStartIndex < children.length)
		{
			//Save last line.
			totalHeight += lineHeight;
		}
		
		return new Dimension(maxWidth, totalHeight);
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int maxWidth = getWidth();
		Component[] children = parent.getComponents();
		int[] widths = new int[children.length];
		for(int i = 0; i < widths.length; i++)
		{
			widths[i] = children[i].getMinimumSize().width;
		}
		
		int lineHeight = 0;
		int lineWidth = 0;
		int lineStartIndex = 0;
		int y = insets.top;
		
		for(int i = lineStartIndex; i < children.length; i++)
		{
			int childWidth = widths[i];
			if(childWidth > maxWidth)
			{
				if(lineStartIndex != i)
				{
					//Save last line.
					int x = insets.left;
					for(int j = lineStartIndex; j < i; j++)
					{
						children[j].setBounds(x, y, widths[j], lineHeight);
						x += widths[j];
					}
					y += lineHeight;
				}
				
				//Child won't fit any row... so just squeeze it:
				int childHeight = children[i].getMinimumSize().height;
				children[i].setBounds(insets.left, y, widths[i], childHeight);
				y += childHeight;
				
				//Init next line:
				lineHeight = 0;
				lineWidth = 0;
				lineStartIndex = i + 1; //Cause we already handled i
			}
			else
			{
				int childWidthWithPadding = childWidth;
				if(lineStartIndex != i)
				{
					//This one is a bit tricky, we want to skip the padding, if we are dealing with the very first of the entries.
					childWidthWithPadding += paddingX;
				}
				if((lineWidth + childWidthWithPadding) > maxWidth)
				{
					//Child won't fit current line, save last line and move it to next line.
					
					//Save last line.
					int x = insets.left;
					for(int j = lineStartIndex; j < i; j++)
					{
						children[j].setBounds(x, y, widths[j], lineHeight);
						x += widths[j] + paddingX;
					}
					y += lineHeight;
					
					//Init next line:
					lineHeight = 0;
					lineWidth = 0;
					lineStartIndex = i;
				}
				
				//Child will fit, check next one!
				lineWidth += childWidthWithPadding;
				int childHeight = children[i].getMinimumSize().height;
				if(childHeight > lineHeight)
				{
					lineHeight = childHeight;
				}
			}
		}
		
		if(lineStartIndex < children.length)
		{
			//Save last line.
			int x = insets.left;
			for(int j = lineStartIndex; j < children.length; j++)
			{
				children[j].setBounds(x, y, widths[j], lineHeight);
				x += widths[j] + paddingX;
			}
		}
	}
}
