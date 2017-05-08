package de.uni_koeln.spinfo.stocknews.articles.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import de.uni_koeln.spinfo.stocknews.articles.data.Article;


public class DbConnector {

	public static Connection connect(String dbFilePath) throws SQLException, ClassNotFoundException {
		Connection connection;
		// register the driver
		Class.forName("org.sqlite.JDBC");
		connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
		System.out.println("Database " + dbFilePath + " successfully opened");
		return connection;
	}

	public static void createNewsFilteredDB(Connection connection) throws SQLException {
		System.out.println("create news_filtered_db");
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement();
		String sql = "DROP TABLE IF EXISTS News";
		stmt.executeUpdate(sql);
		sql = "CREATE TABLE News (ID  INTEGER PRIMARY KEY AUTOINCREMENT, KEYWORD TEXT NOT NULL, story_index TEXT NOT NULL, story_date_time VARCHAR(20) NOT NULL, HEADLINE TEXT, CONTENT TEXT, RICS TEXT, tags Text)";
		stmt.executeUpdate(sql);
		stmt.close();
		connection.commit();
		System.out.println("Initialized new output-database.");
	}
	
	public static Set<Article> readAll (Connection connection) throws SQLException, ParseException{
		Set<Article> articles = new HashSet<Article>();
		
		String query = "SELECT ID, KEYWORD, story_index, story_date_time, HEADLINE, CONTENT, RICS, tags FROM NEWS;";
		PreparedStatement prepStmt = connection.prepareStatement(query);
		prepStmt.setFetchSize(100);
		ResultSet queryResult = prepStmt.executeQuery();
		
		String keyword = null;
		
		String story_index = null;
		LocalDateTime story_date_time = null;
		String headline = null;
		String content = null;
		List<String> rics = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		
		
		while (queryResult.next()){
			keyword = queryResult.getString("KEYWORD");
			story_index = queryResult.getString("story_index");
			story_date_time = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(queryResult.getString("story_date_time"))), ZoneId.systemDefault());
			headline = queryResult.getString("HEADLINE");
			content = queryResult.getString("CONTENT");
			rics.addAll(Arrays.asList(queryResult.getString("RICS").split(";")));
			tags.addAll(Arrays.asList(queryResult.getString("tags").split(";")));
			
			Article art = new Article(story_index,story_date_time,headline,content,rics,tags);
			articles.add(art);	
		}
		return articles;
	}
	
	public static Set<Article> getByKeyword (Connection connection, String key) throws SQLException, ParseException{
		Set<Article> articles = new HashSet<Article>();
		
		String query = "SELECT ID, KEYWORD, story_index, story_date_time, HEADLINE, CONTENT, RICS, tags FROM NEWS WHERE KEYWORD = '" + key + "';";
		PreparedStatement prepStmt = connection.prepareStatement(query);
		prepStmt.setFetchSize(100);
		ResultSet queryResult = prepStmt.executeQuery();
		
		String keyword = null;
		
		String story_index = null;
		LocalDateTime story_date_time = null;
		String headline = null;
		String content = null;
		List<String> rics = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		
		
		while (queryResult.next()){
			keyword = queryResult.getString("KEYWORD");
			story_index = queryResult.getString("story_index");
			story_date_time = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(queryResult.getString("story_date_time"))), ZoneId.systemDefault());
			headline = queryResult.getString("HEADLINE");
			content = queryResult.getString("CONTENT");
			rics.addAll(Arrays.asList(queryResult.getString("RICS").split(";")));
			tags.addAll(Arrays.asList(queryResult.getString("tags").split(";")));
			
			Article art = new Article(story_index,story_date_time,headline,content,rics,tags);
			articles.add(art);	
		}
		return articles;
	}
	
	public static boolean insert(Connection outputConnection, Map<String, List<Article>> filteredNews) throws SQLException {

		try {
			outputConnection.setAutoCommit(false);

			Statement stmt = outputConnection.createStatement();
			PreparedStatement prepNewsTable = outputConnection.prepareStatement(
					"INSERT INTO News (KEYWORD,story_index,story_date_time,HEADLINE,CONTENT, RICS, tags) VALUES(?,?,?,?,?,?,?)");
			
			for(String key : filteredNews.keySet()){
				List<Article> articles = filteredNews.get(key);
				for(Article art : articles){
					
					// Update News
					prepNewsTable.setString(1, key);
					prepNewsTable.setString(2, art.getIndex());
					prepNewsTable.setString(3,art.getDateAsLong().toString());
					prepNewsTable.setString(4, art.getTitle());
					prepNewsTable.setString(5, art.getContent());
					prepNewsTable.setString(6, art.ricsToString());
					prepNewsTable.setString(7, art.tagsToString());
					prepNewsTable.executeUpdate();
				}	
			}
			prepNewsTable.close();
			stmt.close();
			outputConnection.commit();

			return true;

		} catch (SQLException e) {
			outputConnection.rollback();
			e.printStackTrace();
			return false;
		}

	}

}
