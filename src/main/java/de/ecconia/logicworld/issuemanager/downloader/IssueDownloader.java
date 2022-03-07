package de.ecconia.logicworld.issuemanager.downloader;

import de.ecconia.java.json.JSONArray;
import de.ecconia.java.json.JSONNode;
import de.ecconia.java.json.JSONObject;
import de.ecconia.java.json.JSONParser;
import de.ecconia.logicworld.issuemanager.IssueManager;
import de.ecconia.logicworld.issuemanager.data.Comment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public class IssueDownloader
{
	private static final String gameType = "game";
	
	public static final String LogicWorldWebAPI = "https://logicworld.net/graphql";
	
	//TODO: Actually move to own class, because this should not generate the data objects.
	public static List<Comment> loadCommentsFor(String id)
	{
		Path folderLocation = IssueManager.issueFolder.resolve(gameType).resolve("comments");
		Path fileLocation = folderLocation.resolve("c-" + id + ".json");
		JSONObject data;
		if(Files.exists(fileLocation))
		{
			try
			{
				String content = Files.readString(fileLocation);
				data = (JSONObject) JSONParser.parse(content);
			}
			catch(Exception e)
			{
				throw new RuntimeException("Not able to read comments from files for ticket: " + id, e);
			}
		}
		else
		{
			try
			{
				Files.createDirectories(folderLocation);
			}
			catch(Exception e)
			{
				throw new RuntimeException("Not able to create comments folder.", e);
			}
			System.out.println("Downloading comments for " + id);
			data = IssueDownloader.downloadComments(id, fileLocation);
		}
		
		//Parse data:
		{
			JSONArray commentsJSON = data.getArray("comments");
			List<Comment> comments = new LinkedList<>();
			for(Object obj : commentsJSON.getEntries())
			{
				JSONObject commentJSON = JSONArray.asObject(obj);
				Comment comment = new Comment(commentJSON);
				comments.add(comment);
			}
			return comments;
		}
	}
	
	public static JSONObject downloadComments(String id, Path outputFile)
	{
		//Removed 'objectid', since already provided.
		//Removed 'authorid', since in author object.
		String query = """
				query GetComments($objid: String!) {
				  comments(objid: $objid) {
				    id
				    parentid
				    createdat
				    editedat
				    renderedbody
				    body
				    author {
				      id
				      username
				      picture
				      flair
				    }
				    rating {
				      score
				      likedBy
				    }
				  }
				}
				""";
		JSONObject variables = new JSONObject();
		variables.put("objid", id);
		JSONObject response = makeGraphQLRequest(query, variables);
		JSONObject data = response.getObject("data");
		try
		{
			Files.writeString(outputFile, data.printJSON());
		}
		catch(IOException e)
		{
			throw new RuntimeException("Could not write ticket to file '" + outputFile + "'.", e);
		}
		return data;
	}
	
	public static void downloadGameIssues()
	{
		Path path = IssueManager.issueFolder.resolve(gameType);
		try
		{
			Files.createDirectories(path);
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
		downloadIssuesOfType(gameType, path);
	}
	
	private static void downloadIssuesOfType(String type, Path outputDirectory)
	{
		int newlyCollectedTickets;
		int page = 0;
		do
		{
			newlyCollectedTickets = downloadIssuesOfType(type, outputDirectory, page);
			System.out.println("Collected " + newlyCollectedTickets + " tickets from page " + page + ", continuing.");
			page++;
		}
		while(newlyCollectedTickets != 0);
		System.out.println("Done.");
	}
	
	private static int downloadIssuesOfType(String type, Path outputDirectory, int pageNumber)
	{
		String query = """
				query GetTicketsListing($page: PageOptions, $product: String!) {
				  tickets(page: $page, product: $product) {
				    id
				    number
				    kind
				    title
				    createdAt
				    isOpen
				    closedVersion
				    tags {
				      name
				      color
				    }
				    author {
				      id
				      username
				      picture
				      flair
				    }
				    updatedAt
				    body
				    renderedbody
				    rating {
				      score
				      likedBy
				    }
				    product {
				      id
				      prettyName
				      needAnyRole
				    }
				  }
				}
				"""; //There are more fields. However they are redundant or pointless for usage.
		JSONObject variables = new JSONObject();
		JSONObject page = new JSONObject();
		variables.put("page", page);
		JSONArray sort = new JSONArray();
		page.put("sort", sort);
		
		variables.put("product", type);
		page.put("page", pageNumber); //First page.
		page.put("maxCount", 10); //Lets only get 10 for now. MAX is 50. Default is unknown.
		sort.add("-created");
		
		JSONObject response = makeGraphQLRequest(query, variables);
		JSONObject data = response.getObject("data");
		JSONArray tickets = data.getArray("tickets");
		int amount = tickets.getEntries().size();
		for(Object obj : tickets.getEntries())
		{
			JSONObject ticket = JSONArray.asObject(obj);
			// ticket.debugTree("");
			String id = ticket.getString("id");
			Path outputFile = outputDirectory.resolve(id + ".json");
			try
			{
				Files.writeString(outputFile, ticket.printJSON());
			}
			catch(IOException e)
			{
				throw new RuntimeException("Could not write ticket to file '" + outputFile + "'.", e);
			}
		}
		return amount;
	}
	
	private static void getIssueTypes()
	{
		String query = """
				query GetProducts {
				  products {
				    id
				    prettyName
				    needAnyRole
				  }
				}
				""";
		JSONObject response = makeGraphQLRequest(query, null);
		//TODO: Parse, once needed.
		System.out.println("Issue types:");
		response.debugTree("");
	}
	
	public static JSONObject makeGraphQLRequest(String query, JSONObject variables)
	{
		JSONObject requestBodyJSON = new JSONObject();
		requestBodyJSON.put("query", query);
		if(variables != null)
		{
			requestBodyJSON.put("variables", variables);
		}
		String rawResponse = makeRequest(requestBodyJSON);
		JSONNode response;
		try
		{
			response = JSONParser.parse(rawResponse);
		}
		catch(Exception e)
		{
			throw new RuntimeException("Could not parse JSON response: " + rawResponse, e);
		}
		if(!(response instanceof JSONObject))
		{
			throw new RuntimeException("Expected response to be a JSON array, but got: " + rawResponse);
		}
		return (JSONObject) response;
	}
	
	private static String makeRequest(JSONObject body)
	{
		try
		{
			String requestBody = body.printJSON();
			URL uri = new URL(LogicWorldWebAPI);
			HttpsURLConnection connection = (HttpsURLConnection) uri.openConnection();
			connection.setRequestMethod("POST");
			
			connection.setRequestProperty("user-agent", "ecconia/issue-crawler-9000");
			connection.setRequestProperty("content-type", "application/json");
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("content-length", String.valueOf(requestBody.length()));
			
			connection.setDoInput(true);
			connection.setDoOutput(true);
			
			connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
			connection.getOutputStream().close();
			
			if(connection.getResponseCode() != 200)
			{
				String response = readStringFromStream(connection.getErrorStream());
				throw new RuntimeException("Remote server responded with non-OK response code: " + connection.getResponseCode() + " and Message: '" + response + "'");
			}
			
			return readStringFromStream(connection.getInputStream());
		}
		catch(Exception e)
		{
			throw new RuntimeException("Exception while executing GraphQL request.", e);
		}
	}
	
	private static String readStringFromStream(InputStream is) throws IOException
	{
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		for(int length; (length = is.read(buffer)) != -1; )
		{
			result.write(buffer, 0, length);
		}
		is.close();
		return result.toString(StandardCharsets.UTF_8);
	}
}
