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
	
	//Funksjon for å legge til apparat til databasen
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
	
	//Funksjon for å legge til apparatøvelse
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
	
	//Funksjon for å legge til friøvelse
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
	
	//Funksjon for å legge til treningsøkt
	public void registrerOkt(Connection conn, Date dato, Time tidspunkt, int varighet, int form, int prestasjon, List<String> apparatOvelserList, List<String> friOvelserList) throws SQLException {
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
		for(String apparatOvelse : apparatOvelserList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO apparatØvelseIØkt(øktnr, apparatøvelsenr, antallKilo, antallSett)" + 
					"VALUES(?,?,?,?)");
			String[] split = apparatOvelse.split(",");
			stmt2.setInt(1, øktNr);
			stmt2.setInt(2, Integer.valueOf(split[0]));
			stmt2.setInt(3, Integer.valueOf(split[2]));
			stmt2.setInt(4, Integer.valueOf(split[3]));
			stmt2.executeUpdate();
		}
		
		for(String friOvelse : friOvelserList) {
			PreparedStatement stmt3 = conn.prepareStatement(
					"INSERT INTO friøvelseIØkt(øktnr, friøvelsenr, kommentar)" + 
					"VALUES(?,?,?)");
			String[] split = friOvelse.split(",");
			System.out.println(split[2]);
			stmt3.setInt(1, øktNr);
			stmt3.setInt(2, Integer.valueOf(split[0]));
			stmt3.setString(3, split[2]);
			stmt3.executeUpdate();
		}
		
		System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for å legge til øvelsesgruppe til dataabasen
	public void registrerOvelsesGruppe(Connection conn, String navn, List<Integer> apparatøvelseNrList, List<Integer> friøvelseNrList) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO øvelsegruppe(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int gruppenr = tableKeys.getInt(1);
		
		//Går igjennom øvelseNr-nøklene som er hentet fra Listen og legger til i øvelseIØkt.
		for(Integer øvelseNr : apparatøvelseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO apparatøvelseIØvelsegruppe(gruppenr, apparatøvelsenr)" + 
					"VALUES(?,?)");
			stmt2.setInt(1, gruppenr);
			stmt2.setInt(2, øvelseNr);
			stmt2.executeUpdate();
		}
		
		for(Integer øvelseNr : friøvelseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO friøvelseIØvelsegruppe(gruppenr, friøvelsenr)" + 
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
	
	//Henter ut en liste med alle friøvelser
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
	
	//Henter ut en liste med alle apparatøvelser
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
	
	//Henter ut alle øvelser, som brukes blant annet for å opprette øvelsesgrupper
	public List<String> getAllovelser(Connection conn) throws SQLException{
		Statement st = conn.createStatement();
		String sql = "SELECT * FROM apparatøvelse ";
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
	
	//Henter ut en liste med alle apparatøvelser i økter.
	public List<String> getApparatOvelseIOkt(Connection conn, int apparatØvelseNr) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT aø.apparatøvelsenr, dato, navn, antallKilo, antallSett FROM apparatØvelseIØkt aø "
				+ "INNER JOIN apparatøvelse a ON (a.apparatøvelsenr = aø.apparatøvelsenr) "
				+ "INNER JOIN treningsøkt tø ON (tø.øktnr = aø.øktnr) "
				+ "WHERE aø.apparatøvelsenr = " + apparatØvelseNr;
		
		ResultSet rs = st.executeQuery(sql);
		List<String> apparatOvelseList = new ArrayList<String>();
		while(rs.next()) {
			String øvelsenavn = rs.getString("navn");
			int antallkilo = rs.getInt("antallKilo");
			int antallsett = rs.getInt("antallSett");
			Date dato = rs.getDate("dato");
			String listStr = øvelsenavn + "," + String.valueOf(antallkilo) + "," + String.valueOf(antallsett) + "," + dato;
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
	
	//Henter ut en liste med alle øvelsesgrupper
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
		
	//Henter ut en liste med alle øvelser i øvelsesgrupper
	public List<String> getOvelserIGruppe(Connection conn, int gruppeNr) throws SQLException{
			Statement st = conn.createStatement();
			Statement st2 = conn.createStatement();
			String sql = "SELECT * FROM øvelsegruppe øg"
					+ " INNER JOIN apparatøvelseIØvelsegruppe ag ON (ag.gruppenr = øg.gruppenr)"
					+ " INNER JOIN apparatøvelse aø ON (aø.apparatøvelsenr = ag.apparatøvelsenr)"
					+ " WHERE ag.gruppenr = " + gruppeNr;
			
			String sql2 = "SELECT * FROM øvelsegruppe øg"
					+ " INNER JOIN friøvelseIØvelsegruppe fg ON (fg.gruppenr = øg.gruppenr)"
					+ " INNER JOIN friøvelse fø ON (fø.friøvelsenr = fg.friøvelsenr)"
					+ " WHERE fg.gruppenr = " + gruppeNr;
			
			ResultSet rs = st.executeQuery(sql);
			ResultSet rs2 = st2.executeQuery(sql2);
			List<String> apparatøvelseList = new ArrayList<>();
			List<String> friøvelseList = new ArrayList<>();
			while(rs.next()) {
				int apparatøktnr = rs.getInt("ag.apparatøvelsenr");
				String apparatnavn = rs.getString("aø.navn");
				String apparatlistString = "Øvelsenr: " + String.valueOf(apparatøktnr) + " Øvelsenavn: " + apparatnavn;
				apparatøvelseList.add(apparatlistString);
			}
			while(rs2.next()) {
				int friøktnr = rs2.getInt("fg.friøvelsenr");
				String frinavn = rs2.getString("fø.navn");
				String frilistString = "Øvelsenr: " + String.valueOf(friøktnr) + " Øvelsenavn: " + frinavn;
				friøvelseList.add(frilistString);
			}
			apparatøvelseList.addAll(friøvelseList);
			return apparatøvelseList;
		}

	//Henter ut en liste med alle øvelser, i tillegg til ekstra info som antall kilo og antall sett. 
	//Bruker i highscorelisten.
	public List<String> getApparatOvelserMedInfo(Connection conn) throws SQLException{
		Statement st = conn.createStatement();
		String sql = "SELECT aø.apparatøvelsenr, navn, antallKilo, antallSett FROM apparatøvelse a "
				+ "INNER JOIN apparatØvelseIØkt aø ON (aø.apparatøvelsenr = a.apparatøvelsenr) "
				+ "ORDER BY navn ASC, antallKilo DESC";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int øvelsenr = rs.getInt("aø.apparatøvelsenr");
			String øvelsenavn = rs.getString("navn");
			int antKilo = rs.getInt("antallKilo");
			int antSett = rs.getInt("antallSett");
			String listStr = "Nr: " + String.valueOf(øvelsenr) + " // Navn: " + øvelsenavn + " // Antall kilo: " + antKilo + " // Antall sett: " + antSett;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}

}
