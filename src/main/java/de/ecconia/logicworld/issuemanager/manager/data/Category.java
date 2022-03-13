package de.ecconia.logicworld.issuemanager.manager.data;

import java.util.ArrayList;
import java.util.Collection;

public class Category
{
	private final CategoryGroup group;
	private final String name;
	private final ArrayList<WrappedTicket> tickets = new ArrayList<>();
	
	public Category(CategoryGroup group, String name)
	{
		this.group = group;
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void add(WrappedTicket ticket, int index)
	{
		group.ensureRemoved(ticket);
		tickets.add(index, ticket);
	}
	
	public void removeTicket(WrappedTicket ticket)
	{
		tickets.remove(ticket);
	}
	
	public ArrayList<WrappedTicket> getTickets()
	{
		return tickets;
	}
	
	public void add(WrappedTicket ticket)
	{
		tickets.add(ticket);
	}
	
	public void add(Collection<WrappedTicket> remainingTickets)
	{
		tickets.addAll(remainingTickets);
	}
	
	public void moveTicket(int oldIndex, int index)
	{
		WrappedTicket ticket = tickets.remove(oldIndex);
		tickets.add(index, ticket);
	}
	
	public boolean contains(WrappedTicket ticket)
	{
		return tickets.contains(ticket);
	}
}
