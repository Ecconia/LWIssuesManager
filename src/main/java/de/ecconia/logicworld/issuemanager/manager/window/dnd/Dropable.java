package de.ecconia.logicworld.issuemanager.manager.window.dnd;

import java.awt.Point;

import de.ecconia.logicworld.issuemanager.manager.window.TicketBox;

public interface Dropable
{
	void stopHighlight();
	
	void updateHighlight(Point position);
	
	void drop(Point position, TicketBox ticket);
}
