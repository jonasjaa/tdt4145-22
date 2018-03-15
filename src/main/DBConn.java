package main;

import java.sql.DriverManager;
import java.sql.Connection;

public class DBConn { 
	
	//Privat variabel for å lagre connection
	private static Connection conn;
	
	public static void main(String[] args) throws Exception {
		dbConn();
	}
	
	//MYSql Database tilkobling
	//Bruker Maven 'dependecy' for mysql-connector.
	private static Connection dbConn() throws Exception{
		String url = "jdbc:mysql://mysql.stud.ntnu.no:3306/jonasjaa_prosjektdb";
		String username = "jonasjaa";
		String password = "apache123";
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, username, password);
			System.out.println("Connected to the database");
			return conn;
			
		} catch (Exception e) {
			System.out.println(conn);
			System.err.println("Failed to connect to the database: " + e);
		}
		return null;
	}
	
	//En getter for at andre klasser skal kunne hente tilkoblingsobjektet.
	public Connection getConn() throws Exception {
		return dbConn();
	} 


}
