package de.ecconia.logicworld.issuemanager.manager.window.helper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.border.AbstractBorder;

public class OutlineBorder extends AbstractBorder
{
	
	public OutlineBorder()
	{
	}
	
	public void paintBorder(Component c, Graphics g1, int x, int y, int width, int height)
	{
		if(g1 instanceof Graphics2D)
		{
			Graphics2D g = (Graphics2D) g1;
			Color oldColor = g.getColor();
			
			g.setColor(Color.darkGray);
			
			Shape outer;
			Shape inner;
			
			outer = new Rectangle2D.Float(x, y, width, height);
			inner = new Rectangle2D.Float(x + 6, y + 6, width - 12, height - 12);
			Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
			path.append(outer, false);
			path.append(inner, false);
			g.fill(path);
			
			g.setColor(Color.gray);
			g.drawRect(2, 2, width - 4, height - 4);
			g.drawRect(3, 3, width - 6, height - 6);
			
			g.setColor(oldColor);
		}
	}
	
	public Insets getBorderInsets(Component c, Insets insets)
	{
		insets.set(6, 6, 6, 6);
		return insets;
	}
}
