package main;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

//Handler for sende/skrive data til/fra databasen
public class DBHandler {
	
	//Lokal privat variabel for � lagre connection, som blir hentet fra DBConn.
	private static Connection conn;
	
	public static void main(String[] args) throws Exception {
		DBConn dbconn = new DBConn();
		DBHandler test = new DBHandler();
		conn = dbconn.getConn();
		test.getApparatOvelser(conn);
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
	
	//Funksjon for � registere apparat�velse
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
	
	//Funksjon for � registrere fri�velse
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
	
	//Funksjon for � registrere trenings�kt
	public void registrerOkt(Connection conn, Date dato, Time tidspunkt, int varighet, int form, int prestasjon, List<Integer> �velseNrList) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO trenings�kt(dato, tidspunkt, varighet, form, prestasjon)" +
				"VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setDate(1, dato);
		stmt1.setTime(2, tidspunkt);
		stmt1.setInt(3, varighet);
		stmt1.setInt(4, form);
		stmt1.setInt(5, prestasjon);
		stmt1.executeUpdate();
		
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int �ktNr = tableKeys.getInt(1);
		
		//G�r igjennom �velseNr-n�klene som er hentet fra Listen og legger til i �velseI�kt.
		for(Integer �velseNr : �velseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO �velseI�kt(�ktnr, �velsenr)" + 
					"VALUES(?,?)");
			stmt2.setInt(1, �ktNr);
			stmt2.setInt(2, �velseNr);
			stmt2.executeUpdate();
		}
		System.out.println("Successfully inserted to the database");
	}
	
	public void registrerOvelsesGruppe(Connection conn, String navn, List<Integer> �velseNrList) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO �velsegruppe(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int gruppenr = tableKeys.getInt(1);
		
		//G�r igjennom �velseNr-n�klene som er hentet fra Listen og legger til i �velseI�kt.
		for(Integer �velseNr : �velseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO �velseI�velsegruppe(gruppenr, �velsenr)" + 
					"VALUES(?,?)");
			stmt2.setInt(1, gruppenr);
			stmt2.setInt(2, �velseNr);
			stmt2.executeUpdate();
		}
		System.out.println("Successfully inserted to the database");
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
		return apparatList;
	}
	
	//Henter ut en liste med alle �velser som brukes i listview for � legge til trenings�kt med �velser
	public List<String> getOvelser(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT �velsenr, navn FROM �velse";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int �velsenr = rs.getInt("�velsenr");
			String �velsenavn = rs.getString("navn");
			String listStr = String.valueOf(�velsenr) + "," + �velsenavn;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}
	
	//Henter ut en liste med alle apparat�velser s�nn at brukeren for opp en highscore.
	public List<String> getApparatOvelser(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT navn, antallkilo, antallsett FROM �velse INNER JOIN apparat�velse ON (apparat�velse.�velsenr = �velse.�velsenr) ORDER BY antallkilo DESC";
		ResultSet rs = st.executeQuery(sql);
		List<String> apparatOvelseList = new ArrayList<String>();
		while(rs.next()) {
			String �velsenavn = rs.getString("navn");
			int antallkilo = rs.getInt("antallkilo");
			int antallsett = rs.getInt("antallsett");
			String listStr = �velsenavn + ": Antall kilo: " + String.valueOf(antallkilo) + " Antall sett: " + String.valueOf(antallsett);
			apparatOvelseList.add(listStr);
		}
		return apparatOvelseList;
	} 
	
	//Henter ut en liste med alle ternings�kter registrert i databasen
		public  List<String> get�kter(Connection conn) throws SQLException {
			Statement st = conn.createStatement();
			String sql = "SELECT * FROM trenings�kt ORDER BY �ktnr DESC";
			ResultSet rs = st.executeQuery(sql);
			List<String> �ktList = new ArrayList<>();
			while(rs.next()) {
				int �ktnr = rs.getInt("�ktnr");
				Date dato = rs.getDate("dato");
				Time tidspunkt = rs.getTime("tidspunkt");
				int varighet = rs.getInt("varighet");
				int form = rs.getInt("form");
				int prestasjon = rs.getInt("prestasjon");
				String listString = "�ktnummer: " + String.valueOf(�ktnr) + " Dato: " + String.valueOf(dato) + " Tidspunkt: " + String.valueOf(tidspunkt)
				+ " Varighet: " + String.valueOf(varighet) + " Form: " + String.valueOf(form) + " Prestasjon: " + String.valueOf(prestasjon);
				�ktList.add(listString);	
			}
			return �ktList;
		}
		
		public List<String> getOvelsesGrupper(Connection conn) throws SQLException{
			Statement st = conn.createStatement();
			String sql = "SELECT * FROM �velsegruppe";
			ResultSet rs = st.executeQuery(sql);
			List<String> �velsegruppeList = new ArrayList<>();
			while(rs.next()) {
				int gruppenr = rs.getInt("gruppenr");
				String navn = rs.getString("navn");
				String listString = String.valueOf(gruppenr) + "," + navn;
				�velsegruppeList.add(listString);
			}
			return �velsegruppeList;
		}
		
		public List<String> getOvelserIGruppe(Connection conn, int gruppeNr) throws SQLException{
			Statement st = conn.createStatement();
			String sql = "SELECT * FROM �velsegruppe �g INNER JOIN �velseI�velsegruppe �i�g ON (�i�g.gruppenr = �g.gruppenr) INNER JOIN �velse � ON (�i�g.�velsenr = �.�velsenr)"
					+ " WHERE �i�g.gruppenr = " + gruppeNr;
			ResultSet rs = st.executeQuery(sql);
			List<String> �velseList = new ArrayList<>();
			while(rs.next()) {
				int �ktnr = rs.getInt("�.�velsenr");
				String navn = rs.getString("�.navn");
				String listString = "�velsenr: " + String.valueOf(�ktnr) + " �velsenavn: " + navn;
				�velseList.add(listString);
			}
			return �velseList;
		}
}
