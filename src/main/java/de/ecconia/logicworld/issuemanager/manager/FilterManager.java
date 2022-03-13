package de.ecconia.logicworld.issuemanager.manager;

import de.ecconia.logicworld.issuemanager.manager.data.Filter;
import de.ecconia.logicworld.issuemanager.manager.data.FilterGroup;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedTicket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FilterManager
{
	private final Manager manager;
	
	private Filter currentFilter;
	
	public FilterManager(Manager manager)
	{
		this.manager = manager;
		
		currentFilter = new Filter(this);
	}
	
	public Filter getCurrentFilter()
	{
		return currentFilter;
	}
	
	public void setCurrentFilter(Filter currentFilter)
	{
		this.currentFilter = currentFilter;
		applyFilter();
	}
	
	//Group change listener
	// => Updates when a group gets added or removed from the current filter
	
	private final LinkedList<GroupChangeListener> groupChangeListeners = new LinkedList<>();
	
	public interface GroupChangeListener
	{
		void groupsChanged();
	}
	
	public void addGroupChangeListener(GroupChangeListener groupChangeListener)
	{
		groupChangeListeners.add(groupChangeListener);
	}
	
	public void triggerGroupChanged()
	{
		for(GroupChangeListener listener : groupChangeListeners)
		{
			listener.groupsChanged();
		}
	}
	
	//Filter applied listener
	// => Updates when the filter updated
	
	private final LinkedList<FilterAppliedListener> filterAppliedListeners = new LinkedList<>();
	
	public interface FilterAppliedListener
	{
		void filterApplied();
	}
	
	public void addFilterAppliedListener(FilterAppliedListener filterAppliedListener)
	{
		filterAppliedListeners.add(filterAppliedListener);
	}
	
	public void triggerFilterApplied()
	{
		for(FilterAppliedListener listener : filterAppliedListeners)
		{
			listener.filterApplied();
		}
	}
	
	//Filter applying:
	
	public void applyFilter()
	{
		long timestamp = System.currentTimeMillis();
		
		//Actually applies the filter:
		boolean isWhitelisted = false;
		for(FilterGroup filterGroup : currentFilter.getGroups().values())
		{
			if(filterGroup.isWhitelisted())
			{
				isWhitelisted = true;
				break;
			}
		}
		
		List<WrappedTicket> whitelisted = new LinkedList<>(manager.getTickets());
		List<WrappedTicket> blacklisted = new LinkedList<>();
		for(FilterGroup filterGroup : currentFilter.getGroups().values())
		{
			Iterator<WrappedTicket> iterator = whitelisted.iterator();
			while(iterator.hasNext())
			{
				WrappedTicket next = iterator.next();
				if(filterGroup.isBlacklisted(next))
				{
					iterator.remove();
					blacklisted.add(next);
				}
			}
		}
		
		if(isWhitelisted)
		{
			Iterator<WrappedTicket> iterator = whitelisted.iterator();
			ticketLoopLabel:
			while(iterator.hasNext())
			{
				WrappedTicket next = iterator.next();
				for(FilterGroup filterGroup : currentFilter.getGroups().values())
				{
					if(filterGroup.isWhitelisted(next))
					{
						continue ticketLoopLabel;
					}
				}
				//Not whitelisted:
				iterator.remove();
				blacklisted.add(next);
			}
		}
		
		for(WrappedTicket ticket : whitelisted)
		{
			ticket.getComponent().setVisible(true);
		}
		for(WrappedTicket ticket : blacklisted)
		{
			ticket.getComponent().setVisible(false);
		}
		
		System.out.println("Hey your inefficient filtering took: " + ((System.currentTimeMillis() - timestamp) / 1000f) + "s");
		
		triggerFilterApplied();
	}
}
