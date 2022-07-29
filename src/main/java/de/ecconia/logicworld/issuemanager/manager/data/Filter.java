package de.ecconia.logicworld.issuemanager.manager.data;

import java.util.HashMap;
import java.util.Map;

import de.ecconia.logicworld.issuemanager.manager.FilterManager;

public class Filter
{
	private final FilterManager filterManager;
	
	private final HashMap<CategoryGroup, FilterGroup> groups = new HashMap<>();
	
	public Filter(FilterManager filterManager)
	{
		this.filterManager = filterManager;
	}
	
	public boolean apply(WrappedTicket ticket)
	{
		return false;
	}
	
	public FilterGroup createFilterGroup(CategoryGroup group)
	{
		try
		{
			FilterGroup groupFilter = groups.get(group);
			if(groupFilter != null)
			{
				throw new RuntimeException("Group filter was already created!");
			}
			groupFilter = new FilterGroup(this, group);
			groups.put(group, groupFilter);
			return groupFilter;
		}
		finally
		{
			filterManager.triggerGroupChanged();
		}
	}
	
	public boolean hasFilterGroup(CategoryGroup group)
	{
		return groups.containsKey(group);
	}
	
	public void removeFilterGroup(CategoryGroup group)
	{
		groups.remove(group);
		filterManager.triggerGroupChanged();
		filterManager.applyFilter();
	}
	
	public int getGroupFilterAmount()
	{
		return groups.size();
	}
	
	//Filter simulation:
	
	public void filterChanged()
	{
		filterManager.applyFilter();
	}
	
	public Map<CategoryGroup, FilterGroup> getGroups()
	{
		return groups;
	}
}
