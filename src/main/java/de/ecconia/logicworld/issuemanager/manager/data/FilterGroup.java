package de.ecconia.logicworld.issuemanager.manager.data;

import java.util.ArrayList;
import java.util.List;

public class FilterGroup
{
	private final Filter filter;
	private final CategoryGroup group;
	
	private final List<CategoryFilterEntry> filterEntries;
	
	public FilterGroup(Filter filter, CategoryGroup group)
	{
		this.filter = filter;
		this.group = group;
		
		//TODO: React to changes!
		filterEntries = new ArrayList<>(group.getCategories().size());
		for(Category category : group.getCategories())
		{
			filterEntries.add(new CategoryFilterEntry(filter, category));
		}
	}
	
	public CategoryGroup getGroup()
	{
		return group;
	}
	
	public List<CategoryFilterEntry> getFilterEntries()
	{
		return filterEntries;
	}
	
	public void remove()
	{
		filter.removeFilterGroup(group);
	}
	
	public boolean isWhitelisted()
	{
		for(CategoryFilterEntry entry : filterEntries)
		{
			if(entry.getMode() == FilterEntry.Mode.Whitelist)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isBlacklisted(WrappedTicket ticket)
	{
		for(CategoryFilterEntry entry : filterEntries)
		{
			if(entry.getMode() == FilterEntry.Mode.Blacklist)
			{
				if(entry.getCategory().contains(ticket))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isWhitelisted(WrappedTicket ticket)
	{
		for(CategoryFilterEntry entry : filterEntries)
		{
			if(entry.getMode() == FilterEntry.Mode.Whitelist)
			{
				if(entry.getCategory().contains(ticket))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	private static class CategoryFilterEntry extends FilterEntry
	{
		private final Category category;
		
		public CategoryFilterEntry(Filter filter, Category category)
		{
			super(filter);
			
			this.category = category;
		}
		
		@Override
		public String getName()
		{
			return category.getName();
		}
		
		public Category getCategory()
		{
			return category;
		}
	}
}
