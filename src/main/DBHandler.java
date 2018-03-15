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
	
	//Lokal privat variabel for � lagre connection, som blir hentet fra DBConn.
	private static Connection conn;
	
	public static void main(String[] args) throws Exception {
		DBConn dbconn = new DBConn();
		DBHandler test = new DBHandler();
		conn = dbconn.getConn();
	}
	
	//Funksjon for � registrere apparat til databasen
	public void registrerApparat(Connection conn, String navn, String beskrivelse) throws SQLException {
		//Oppretter en statement hvor sql-sp�rringen ligger.
		PreparedStatement stmt = conn.prepareStatement(
				"INSERT INTO apparat(navn, beskrivelse)" +
                "VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, navn);
        stmt.setString(2, beskrivelse);
        stmt.executeUpdate();
        
        //Dette brukes for � hente ut n�klene i tabellen, og som igjen s�rger for at 
        //AUTO_INCREMENT i databasen fungerer.
        ResultSet tableKeys = stmt.getGeneratedKeys();
		tableKeys.next();
		int apparatNr = tableKeys.getInt(1);
        
        System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for � registerer apparat�velse
	public void registrerApparatOvelse(Connection conn, int apparatnr, String navn, int kilo, int sett) throws SQLException {
		//Statement for � skrive til �velsetabellen
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO �velse(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		//Henter ogs� her ut n�klene. 
		//N�kkelen her blir brukt videre til sp�rringene under, som fremmedn�kkel.
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int �velseNr = tableKeys.getInt(1);
		
		//Statement for � skrive til apparatTilApparat�velse
		PreparedStatement stmt2 = conn.prepareStatement(
				"INSERT INTO apparatTilApparat�velse(�velsenr, apparatnr)" + 
				"VALUES(?,?)");
		stmt2.setInt(1, �velseNr);
		stmt2.setInt(2, apparatnr);
		stmt2.executeUpdate();
		
		//Statement for � skrive til apparat�velse
		PreparedStatement stmt3 = conn.prepareStatement(
				"INSERT INTO apparat�velse(�velsenr, antallkilo, antallsett)" +
				"VALUES(?,?,?)");
		stmt3.setInt(1, �velseNr);
		stmt3.setInt(2, kilo);
		stmt3.setInt(3, sett);
		stmt3.executeUpdate();
				
		System.out.println("Successfully inserted to the database");
	}
	
	public void registrerFriOvelse(Connection conn, String navn, String friOvBeskr) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO �velse(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		//Henter ogs� her ut n�klene. 
		//N�kkelen her blir brukt videre til sp�rringene under, som fremmedn�kkel.
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int �velseNr = tableKeys.getInt(1);
		
		PreparedStatement stmt2 = conn.prepareStatement(
				"INSERT INTO fri�velse(�velsenr, beskrivelse)" + 
				"VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt2.setInt(1, �velseNr);
		stmt2.setString(2, friOvBeskr);
		stmt2.executeUpdate();
		
		System.out.println("Successfully inserted to the database");
	}
	
	public void registrerOkt(Connection conn) {
		
	}
	
	//Henter ut en liste med apparater som ligger i databasen
	//Denne listen brukes i AppController for � f� opp en liste over apparat n�r apparat�velse skal registeres
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
