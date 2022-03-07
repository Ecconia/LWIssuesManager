package de.ecconia.logicworld.issuemanager;

import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.logicworld.issuemanager.data.Ticket;
import de.ecconia.logicworld.issuemanager.downloader.IssueDownloader;
import de.ecconia.logicworld.issuemanager.manager.Manager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IssueManager
{
	public static final Path rootFolder = Paths.get("LWIssueManager");
	public static final Path dataFile = rootFolder.resolve("data.json");
	public static final Path issueFolder = rootFolder.resolve("issues");
	
	public static void main(String[] args) throws IOException
	{
		//Download issues, if not already done:
		Path gameIssueFolder = IssueManager.issueFolder.resolve("game");
		if(!Files.exists(gameIssueFolder))
		{
			System.out.println("Original issues are not downloaded yet! Downloading...");
			IssueDownloader.downloadGameIssues();
			System.out.println();
		}
		//Load the issues from file:
		List<Ticket> tickets = new ArrayList<>();
		Files.list(gameIssueFolder).forEach(path -> {
			try
			{
				if(Files.isDirectory(path))
				{
					return;
				}
				String content = Files.readString(path);
				JSONObject ticketJSON = (JSONObject) JSONParser.parse(content);
				tickets.add(new Ticket(ticketJSON));
			}
			catch(Exception e)
			{
				throw new RuntimeException("Could not parse ticket file at: " + path, e);
			}
		});
		tickets.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o2.getId(), o1.getId()));
		
		//Print things:
		// for(Ticket ticket : tickets)
		// {
		// 	ticket.debug();
		// 	System.out.println();
		// }
		System.out.println("Ticket count: " + tickets.size());
		
		//Start GUI:
		Manager manager = new Manager(tickets);
		//Shutdown hook for saving:
		Runtime.getRuntime().addShutdownHook(new Thread(manager::save)); //TODO: Enable saving again.
	}
}
