package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


//Handler for sende/skrive data til/fra databasen
public class DBHandler {
	
	//Lokal privat variabel for å lagre connection, som blir hentet fra DBConn.
	private static Connection conn;
	
	public static void main(String[] args) throws Exception {
		DBConn dbconn = new DBConn();
		DBHandler test = new DBHandler();
		conn = dbconn.getConn();
	}
	
	//Funksjon for å registrere apparat til databasen
	public void registrerApparat(Connection conn, String navn, String beskrivelse) throws SQLException {
		//Oppretter en statement hvor sql-spørringen ligger.
		PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO apparat(navn, beskrivelse)" +
                "VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, navn);
        stmt.setString(2, beskrivelse);
        stmt.executeUpdate();
        
        //Dette brukes for å hente ut nøklene i tabellen, og som igjen sørger for at 
        //AUTO_INCREMENT i databasen fungerer.
        ResultSet tableKeys = stmt.getGeneratedKeys();
		tableKeys.next();
		int apparatNr = tableKeys.getInt(1);
        
        System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for å registerer apparatøvelse
	public void registrerApparatOvelse(Connection conn, int apparatnr, String navn, int kilo, int sett) throws SQLException {
		//Statement for å skrive til øvelsetabellen
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO øvelse(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		//Henter også her ut nøklene. 
		//Nøkkelen her blir brukt videre til spørringene under, som fremmednøkkel.
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int øvelseNr = tableKeys.getInt(1);
		
		//Statement for å skrive til apparatTilApparatøvelse
		PreparedStatement stmt2 = conn.prepareStatement(
				"INSERT INTO apparatTilApparatøvelse(øvelsenr, apparatnr)" + 
				"VALUES(?,?)");
		stmt2.setInt(1, øvelseNr);
		stmt2.setInt(2, apparatnr);
		stmt2.executeUpdate();
		
		//Statement for å skrive til apparatøvelse
		PreparedStatement stmt3 = conn.prepareStatement(
				"INSERT INTO apparatøvelse(øvelsenr, antallkilo, antallsett)" +
				"VALUES(?,?,?)");
		stmt3.setInt(1, øvelseNr);
		stmt3.setInt(2, kilo);
		stmt3.setInt(3, sett);
		stmt3.executeUpdate();
				
		System.out.println("Successfully inserted to the database");
	}
	
	public void registrerFriOvelse(Connection conn, String navn, String friOvBeskr) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO øvelse(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		//Henter også her ut nøklene. 
		//Nøkkelen her blir brukt videre til spørringene under, som fremmednøkkel.
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int øvelseNr = tableKeys.getInt(1);
		
		PreparedStatement stmt2 = conn.prepareStatement(
				"INSERT INTO friøvelse(øvelsenr, beskrivelse)" + 
				"VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt2.setInt(1, øvelseNr);
		stmt2.setString(2, friOvBeskr);
		stmt2.executeUpdate();
		
		System.out.println("Successfully inserted to the database");
	}
	
	public void registrerOkt(Connection conn) {
		
	}
	
	//Henter ut en liste med apparater som ligger i databasen
	//Denne listen brukes i AppController for å få opp en liste over apparat når apparatøvelse skal registeres
	public List<String> getApparat(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT apparatnr, navn FROM apparat";
		ResultSet rs = st.executeQuery(sql);
		List<String> apparatList = new ArrayList<>();
		while(rs.next()) {
			int apparatnr = rs.getInt("apparatnr");
			String apparatnavn = rs.getString("navn");
			String listStr = String.valueOf(apparatnr) + "," + apparatnavn;
			apparatList.add(listStr);
		}
		System.out.println(apparatList);
		return apparatList;
	}

	
 
}
