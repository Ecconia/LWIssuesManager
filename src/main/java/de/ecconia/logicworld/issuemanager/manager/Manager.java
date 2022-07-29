package de.ecconia.logicworld.issuemanager.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.logicworld.issuemanager.IssueManager;
import de.ecconia.logicworld.issuemanager.data.Ticket;
import de.ecconia.logicworld.issuemanager.data.Type;
import de.ecconia.logicworld.issuemanager.manager.data.Category;
import de.ecconia.logicworld.issuemanager.manager.data.CategoryGroup;
import de.ecconia.logicworld.issuemanager.manager.data.WrappedTicket;
import de.ecconia.logicworld.issuemanager.manager.window.ManagerGUI;
import de.ecconia.logicworld.issuemanager.util.OrderedMap;

public class Manager
{
	private final Map<Integer, WrappedTicket> ticketMap = new HashMap<>();
	private final OrderedMap<String, CategoryGroup> groups = new OrderedMap<>();
	
	private FilterManager filterManager = new FilterManager(this);
	
	public Manager(List<Ticket> originalTickets)
	{
		for(Ticket ticket : originalTickets)
		{
			WrappedTicket wTicket = new WrappedTicket(ticket);
			if(ticketMap.put(wTicket.getOriginal().getNumber(), wTicket) != null)
			{
				throw new RuntimeException("Ticket with number " + wTicket.getOriginal().getNumber() + " exists more than once!");
			}
		}
		
		load();
		
		new ManagerGUI(this);
	}
	
	public void save()
	{
		System.out.println("Saving...");
		JSONObject rootJSON = new JSONObject();
		
		{
			JSONArray groupsJSON = new JSONArray();
			rootJSON.put("groups", groupsJSON);
			for(CategoryGroup group : groups.getValues())
			{
				JSONObject groupJSON = new JSONObject();
				groupsJSON.add(groupJSON);
				
				groupJSON.put("name", group.getName());
				JSONArray categoriesJSON = new JSONArray();
				
				groupJSON.put("categories", categoriesJSON);
				List<Category> categories = group.getCategories();
				for(int i = 1; i < categories.size(); i++)
				{
					Category category = categories.get(i);
					
					JSONObject categoryJSON = new JSONObject();
					categoriesJSON.add(categoryJSON);
					
					categoryJSON.put("name", category.getName());
					
					JSONArray categoryEntriesJSON = new JSONArray();
					categoryJSON.put("entries", categoryEntriesJSON);
					for(WrappedTicket ticket : category.getTickets())
					{
						categoryEntriesJSON.add(ticket.getOriginal().getNumber());
					}
				}
			}
		}
		{
			JSONArray ticketsJSON = new JSONArray();
			rootJSON.put("tickets", ticketsJSON);
			for(WrappedTicket ticket : ticketMap.values())
			{
				if(ticket.isModified())
				{
					JSONObject ticketJSON = new JSONObject();
					ticketsJSON.add(ticketJSON);
					
					ticketJSON.put("id", ticket.getOriginal().getNumber());
					ticketJSON.put("type", Type.asNameOrNull(ticket.getRawType()));
					ticketJSON.put("title", ticket.getRawTitle());
					ticketJSON.put("body", ticket.getRawBody());
					ticketJSON.put("sComment", ticket.getShortComment());
				}
			}
		}
		
		String jsonString = rootJSON.printJSON();
		try
		{
			Files.writeString(IssueManager.dataFile, jsonString);
		}
		catch(IOException e)
		{
			throw new RuntimeException("Could not save state to file!", e);
		}
		System.out.println("Saving done!");
	}
	
	private void load()
	{
		System.out.println("Loading from file...");
		String dataFile;
		try
		{
			dataFile = Files.readString(IssueManager.dataFile);
		}
		catch(IOException e)
		{
			throw new RuntimeException("Could not load data file.", e);
		}
		HashMap<Category, int[]> idsToInjectLater = new HashMap<>();
		JSONObject rootJSON = (JSONObject) JSONParser.parse(dataFile);
		{
			JSONArray groupsJSON = rootJSON.getArray("groups");
			for(Object groupObj : groupsJSON.getEntries())
			{
				JSONObject groupJSON = JSONArray.asObject(groupObj);
				
				String groupName = groupJSON.getString("name");
				CategoryGroup group = new CategoryGroup(groupName, this);
				groups.put(groupName, group);
				
				JSONArray categoriesJSON = groupJSON.getArray("categories");
				for(Object categoryObj : categoriesJSON.getEntries())
				{
					JSONObject categoryJSON = JSONArray.asObject(categoryObj);
					
					String categoryName = categoryJSON.getString("name");
					Category category = group.addNewCategory(categoryName);
					
					JSONArray entriesJSON = categoryJSON.getArray("entries");
					int[] ids = new int[entriesJSON.getEntries().size()];
					int index = 0;
					for(Object idObj : entriesJSON.getEntries())
					{
						int ticketNumber = JSONArray.asInt(idObj);
						ids[index++] = ticketNumber;
					}
					idsToInjectLater.put(category, ids);
				}
			}
			
			JSONArray ticketsJSON = rootJSON.getArray("tickets");
			for(Object ticketObj : ticketsJSON.getEntries())
			{
				JSONObject ticketJSON = JSONArray.asObject(ticketObj);
				int id = ticketJSON.getInt("id");
				String typeString = ticketJSON.getStringOrNull("type");
				Type type = typeString == null ? null : Type.fromString(typeString);
				String title = ticketJSON.getStringOrNull("title");
				String body = ticketJSON.getStringOrNull("body");
				String shortComment = ticketJSON.getString("sComment");
				
				WrappedTicket ticket = ticketMap.get(id);
				if(ticket == null)
				{
					System.out.println("Could not overwrite ticket with ID: " + id + " Because it seems missing!");
					System.out.println(" Overwrite data:");
					System.out.println("  - Type: " + type);
					System.out.println("  - Title: " + title);
					System.out.println("  - Body: " + body);
					System.out.println("  - ShortComment: " + shortComment);
					continue;
				}
				ticket.overwriteType(type);
				ticket.overwriteTitle(title);
				ticket.overwriteBody(body);
				ticket.setShortComment(shortComment);
			}
		}
		
		//Injecting loaded data into groups/categories and tickets:
		System.out.println("Done parsing file, processing tickets...");
		for(CategoryGroup group : groups.getValues())
		{
			HashMap<Integer, WrappedTicket> ticketMapCopy = new HashMap<>(ticketMap);
			group.injectTickets(ticketMapCopy, idsToInjectLater);
		}
	}
	
	public boolean isGroupExisting(String input)
	{
		return groups.containsKey(input);
	}
	
	public void addNewGroup(String name)
	{
		CategoryGroup group = new CategoryGroup(name, this);
		groups.put(name, group);
		group.injectTickets(ticketMap);
		//TBI: Also activate it?
	}
	
	public Collection<CategoryGroup> getGroups()
	{
		return groups.getValues();
	}
	
	public CategoryGroup getGroup(String name)
	{
		CategoryGroup group = groups.get(name);
		if(group == null)
		{
			throw new RuntimeException("Tried to get group (probably from button click), but group does not exist.");
		}
		return group;
	}
	
	public void deleteGroup(CategoryGroup group)
	{
		groups.remove(group.getName());
	}
	
	public Collection<WrappedTicket> getTickets()
	{
		return ticketMap.values();
	}
	
	public FilterManager getFilterManager()
	{
		return filterManager;
	}
}
