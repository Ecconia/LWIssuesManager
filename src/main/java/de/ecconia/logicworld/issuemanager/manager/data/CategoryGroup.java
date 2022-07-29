package de.ecconia.logicworld.issuemanager.manager.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ecconia.logicworld.issuemanager.manager.Manager;
import de.ecconia.logicworld.issuemanager.util.OrderedMap;

public class CategoryGroup
{
	public static final String defaultCategoryName = "Unsorted";
	
	private final Manager manager;
	private final String name;
	
	private final Category unsorted;
	private final OrderedMap<String, Category> categories = new OrderedMap<>();
	
	public CategoryGroup(String name, Manager manager)
	{
		this.name = name;
		this.manager = manager;
		
		unsorted = new Category(this, defaultCategoryName);
		categories.put(unsorted.getName(), unsorted);
	}
	
	public String getName()
	{
		return name;
	}
	
	public List<Category> getCategories()
	{
		return categories.getValues();
	}
	
	public boolean isCategoryExisting(String name)
	{
		return categories.containsKey(name);
	}
	
	public Category addNewCategory(String name)
	{
		Category category = new Category(this, name);
		categories.put(name, category);
		return category;
	}
	
	public Category getUnsorted()
	{
		return unsorted;
	}
	
	public void delete(Category category)
	{
		unsorted.getTickets().addAll(category.getTickets());
		categories.remove(category.getName());
	}
	
	public void ensureRemoved(WrappedTicket ticket)
	{
		for(Category cat : categories.getValues())
		{
			cat.removeTicket(ticket);
		}
	}
	
	public void injectTickets(Map<Integer, WrappedTicket> ticketMapCopy, HashMap<Category, int[]> idsToInjectLater)
	{
		List<Category> categories = this.categories.getValues();
		for(int i = 1; i < categories.size(); i++)
		{
			Category category = categories.get(i);
			int[] ids = idsToInjectLater.remove(category);
			for(int id : ids)
			{
				WrappedTicket ticket = ticketMapCopy.remove(id);
				if(ticket == null)
				{
					//Ticket must have been deleted?
					System.out.println("Could not inject ticket '" + id + "' into '" + name + "':'" + category.getName() + "', it was not registered. Deleted on LW?");
					//TODO: Collect this data, and refuse start with popup if issue.
				}
				category.add(ticket);
			}
		}
		Collection<WrappedTicket> remainingTickets = ticketMapCopy.values();
		unsorted.add(remainingTickets);
	}
	
	public void injectTickets(Map<Integer, WrappedTicket> ticketMap)
	{
		unsorted.add(ticketMap.values());
	}
}
