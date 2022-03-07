package de.ecconia.logicworld.issuemanager.manager.window.dnd;

import de.ecconia.logicworld.issuemanager.manager.window.TicketBox;
import java.awt.Point;

public interface Dropable
{
	void stopHighlight();
	
	void updateHighlight(Point position);
	
	void drop(Point position, TicketBox ticket);
}
