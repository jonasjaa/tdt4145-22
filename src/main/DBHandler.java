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
	
	//Funksjon for å registere apparatøvelse
	public void registrerApparatOvelse(Connection conn, int apparatnr, String navn) throws SQLException {
		//Statement for å skrive til øvelsetabellen
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO apparatøvelse(apparatnr, navn)" +
				"VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setInt(1, apparatnr);
		stmt1.setString(2, navn);
		stmt1.executeUpdate();
		
		//Henter også her ut nøklene. 
		//Nøkkelen her blir brukt videre til spørringene under, som fremmednøkkel.
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int øvelseNr = tableKeys.getInt(1);
						
		System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for å registrere friøvelse
	public void registrerFriOvelse(Connection conn, String navn, String friOvBeskr) throws SQLException {
		//Henter også her ut nøklene. 
		//Nøkkelen her blir brukt videre til spørringene under, som fremmednøkkel.		
		PreparedStatement stmt2 = conn.prepareStatement(
				"INSERT INTO friøvelse(navn, beskrivelse)" + 
				"VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt2.setString(1, navn);
		stmt2.setString(2, friOvBeskr);
		stmt2.executeUpdate();
		
		System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for å registrere treningsøkt
	public void registrerOkt(Connection conn, Date dato, Time tidspunkt, int varighet, int form, int prestasjon, List<Integer> øvelseNrList) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO treningsøkt(dato, tidspunkt, varighet, form, prestasjon)" +
				"VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setDate(1, dato);
		stmt1.setTime(2, tidspunkt);
		stmt1.setInt(3, varighet);
		stmt1.setInt(4, form);
		stmt1.setInt(5, prestasjon);
		stmt1.executeUpdate();
		
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int øktNr = tableKeys.getInt(1);
		
		//Går igjennom øvelseNr-nøklene som er hentet fra Listen og legger til i øvelseIØkt.
		for(Integer øvelseNr : øvelseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO øvelseIØkt(øktnr, øvelsenr)" + 
					"VALUES(?,?)");
			stmt2.setInt(1, øktNr);
			stmt2.setInt(2, øvelseNr);
			stmt2.executeUpdate();
		}
		System.out.println("Successfully inserted to the database");
	}
	
	public void registrerOvelsesGruppe(Connection conn, String navn, List<Integer> øvelseNrList) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO øvelsegruppe(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int gruppenr = tableKeys.getInt(1);
		
		//Går igjennom øvelseNr-nøklene som er hentet fra Listen og legger til i øvelseIØkt.
		for(Integer øvelseNr : øvelseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO øvelseIØvelsegruppe(gruppenr, øvelsenr)" + 
					"VALUES(?,?)");
			stmt2.setInt(1, gruppenr);
			stmt2.setInt(2, øvelseNr);
			stmt2.executeUpdate();
		}
		System.out.println("Successfully inserted to the database");
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
		return apparatList;
	}
	
	//Henter ut en liste med alle øvelser som brukes i listview for å legge til treningsøkt med øvelser
	public List<String> getFriovelser(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT friøvelsenr, navn FROM friøvelse";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int øvelsenr = rs.getInt("friøvelsenr");
			String øvelsenavn = rs.getString("navn");
			String listStr = String.valueOf(øvelsenr) + "," + øvelsenavn;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}
	
	public List<String> getApparatovelser(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT apparatøvelsenr, navn FROM apparatøvelse";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int øvelsenr = rs.getInt("apparatøvelsenr");
			String øvelsenavn = rs.getString("navn");
			String listStr = String.valueOf(øvelsenr) + "," + øvelsenavn;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}
	
	//Henter ut en liste med alle apparatøvelser sånn at brukeren for opp en highscore.
	public List<String> getApparatOvelseIOkt(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT navn, antallKilo, antallSett FROM apparatØvelseIØkt INNER JOIN apparatøvelse ON (apparatøvelse.apparatøvelsenr = apparatØvelseIØkt.apparatøvelsenr) ORDER BY antallKilo DESC";
		ResultSet rs = st.executeQuery(sql);
		List<String> apparatOvelseList = new ArrayList<String>();
		while(rs.next()) {
			String øvelsenavn = rs.getString("navn");
			int antallkilo = rs.getInt("antallKilo");
			int antallsett = rs.getInt("antallSett");
			String listStr = øvelsenavn + ": Antall kilo: " + String.valueOf(antallkilo) + " Antall sett: " + String.valueOf(antallsett);
			apparatOvelseList.add(listStr);
		}
		return apparatOvelseList;
	} 
	
	//Henter ut en liste med alle terningsøkter registrert i databasen
		public  List<String> getØkter(Connection conn) throws SQLException {
			Statement st = conn.createStatement();
			String sql = "SELECT * FROM treningsøkt ORDER BY øktnr DESC";
			ResultSet rs = st.executeQuery(sql);
			List<String> øktList = new ArrayList<>();
			while(rs.next()) {
				int øktnr = rs.getInt("øktnr");
				Date dato = rs.getDate("dato");
				Time tidspunkt = rs.getTime("tidspunkt");
				int varighet = rs.getInt("varighet");
				int form = rs.getInt("form");
				int prestasjon = rs.getInt("prestasjon");
				String listString = "Øktnummer: " + String.valueOf(øktnr) + " Dato: " + String.valueOf(dato) + " Tidspunkt: " + String.valueOf(tidspunkt)
				+ " Varighet: " + String.valueOf(varighet) + " Form: " + String.valueOf(form) + " Prestasjon: " + String.valueOf(prestasjon);
				øktList.add(listString);	
			}
			return øktList;
		}
		
		public List<String> getOvelsesGrupper(Connection conn) throws SQLException{
			Statement st = conn.createStatement();
			String sql = "SELECT * FROM øvelsegruppe";
			ResultSet rs = st.executeQuery(sql);
			List<String> øvelsegruppeList = new ArrayList<>();
			while(rs.next()) {
				int gruppenr = rs.getInt("gruppenr");
				String navn = rs.getString("navn");
				String listString = String.valueOf(gruppenr) + "," + navn;
				øvelsegruppeList.add(listString);
			}
			return øvelsegruppeList;
		}
		
		public List<String> getOvelserIGruppe(Connection conn, int gruppeNr) throws SQLException{
			Statement st = conn.createStatement();
			String sql = "SELECT * FROM øvelsegruppe øg INNER JOIN øvelseIØvelsegruppe øiøg ON (øiøg.gruppenr = øg.gruppenr) INNER JOIN øvelse ø ON (øiøg.øvelsenr = ø.øvelsenr)"
					+ " WHERE øiøg.gruppenr = " + gruppeNr;
			ResultSet rs = st.executeQuery(sql);
			List<String> øvelseList = new ArrayList<>();
			while(rs.next()) {
				int øktnr = rs.getInt("ø.øvelsenr");
				String navn = rs.getString("ø.navn");
				String listString = "Øvelsenr: " + String.valueOf(øktnr) + " Øvelsenavn: " + navn;
				øvelseList.add(listString);
			}
			return øvelseList;
		}
}
