package de.ecconia.logicworld.issuemanager.manager.window.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

public class RowsWidthFillLayout implements LayoutManager
{
	private final Container referenceComponent;
	
	public RowsWidthFillLayout(Container referenceComponent)
	{
		this.referenceComponent = referenceComponent;
	}
	
	@Override
	public void addLayoutComponent(String name, Component comp)
	{
	}
	
	@Override
	public void removeLayoutComponent(Component comp)
	{
	}
	
	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		Insets insets = parent.getInsets();
		int height = 0;
		for(Component child : parent.getComponents())
		{
			if(!child.isVisible())
			{
				continue;
			}
			height += child.getPreferredSize().height;
		}
		return new Dimension(referenceComponent.getWidth() - insets.left - insets.right, height + insets.top + insets.bottom);
	}
	
	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		Insets insets = parent.getInsets();
		int height = 0;
		for(Component child : parent.getComponents())
		{
			if(!child.isVisible())
			{
				continue;
			}
			height += child.getMinimumSize().height;
		}
		return new Dimension(referenceComponent.getWidth() - insets.left - insets.right, height + insets.top + insets.bottom);
	}
	
	@Override
	public void layoutContainer(Container parent)
	{
		Insets insets = parent.getInsets();
		int y = insets.top;
		for(Component child : parent.getComponents())
		{
			if(!child.isVisible())
			{
				child.setBounds(insets.left, y, 0, 0);
				continue;
			}
			int height = child.getMinimumSize().height;
			child.setBounds(insets.left, y, referenceComponent.getWidth() - insets.left - insets.right, height);
			y += height;
		}
	}
}
