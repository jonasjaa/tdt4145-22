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
	}
	
	//Funksjon for � legge til apparat til databasen
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
	
	//Funksjon for � legge til apparat�velse
	public void registrerApparatOvelse(Connection conn, int apparatnr, String navn) throws SQLException {
		//Statement for � skrive til �velsetabellen
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO apparat�velse(apparatnr, navn)" +
				"VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setInt(1, apparatnr);
		stmt1.setString(2, navn);
		stmt1.executeUpdate();
		
		//Henter ogs� her ut n�klene. 
		//N�kkelen her blir brukt videre til sp�rringene under, som fremmedn�kkel.
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int �velseNr = tableKeys.getInt(1);
						
		System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for � legge til fri�velse
	public void registrerFriOvelse(Connection conn, String navn, String friOvBeskr) throws SQLException {
		//Henter ogs� her ut n�klene. 
		//N�kkelen her blir brukt videre til sp�rringene under, som fremmedn�kkel.		
		PreparedStatement stmt2 = conn.prepareStatement(
				"INSERT INTO fri�velse(navn, beskrivelse)" + 
				"VALUES(?,?)", Statement.RETURN_GENERATED_KEYS);
		stmt2.setString(1, navn);
		stmt2.setString(2, friOvBeskr);
		stmt2.executeUpdate();
		
		System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for � legge til trenings�kt
	public void registrerOkt(Connection conn, Date dato, Time tidspunkt, int varighet, int form, int prestasjon, List<String> apparatOvelserList, List<String> friOvelserList) throws SQLException {
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
		for(String apparatOvelse : apparatOvelserList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO apparat�velseI�kt(�ktnr, apparat�velsenr, antallKilo, antallSett)" + 
					"VALUES(?,?,?,?)");
			String[] split = apparatOvelse.split(",");
			stmt2.setInt(1, �ktNr);
			stmt2.setInt(2, Integer.valueOf(split[0]));
			stmt2.setInt(3, Integer.valueOf(split[2]));
			stmt2.setInt(4, Integer.valueOf(split[3]));
			stmt2.executeUpdate();
		}
		
		for(String friOvelse : friOvelserList) {
			PreparedStatement stmt3 = conn.prepareStatement(
					"INSERT INTO fri�velseI�kt(�ktnr, fri�velsenr, kommentar)" + 
					"VALUES(?,?,?)");
			String[] split = friOvelse.split(",");
			System.out.println(split[2]);
			stmt3.setInt(1, �ktNr);
			stmt3.setInt(2, Integer.valueOf(split[0]));
			stmt3.setString(3, split[2]);
			stmt3.executeUpdate();
		}
		
		System.out.println("Successfully inserted to the database");
	}
	
	//Funksjon for � legge til �velsesgruppe til dataabasen
	public void registrerOvelsesGruppe(Connection conn, String navn, List<Integer> apparat�velseNrList, List<Integer> fri�velseNrList) throws SQLException {
		PreparedStatement stmt1 = conn.prepareStatement(
				"INSERT INTO �velsegruppe(navn)" +
				"VALUES(?)", Statement.RETURN_GENERATED_KEYS);
		stmt1.setString(1, navn);
		stmt1.executeUpdate();
		
		ResultSet tableKeys = stmt1.getGeneratedKeys();
		tableKeys.next();
		int gruppenr = tableKeys.getInt(1);
		
		//G�r igjennom �velseNr-n�klene som er hentet fra Listen og legger til i �velseI�kt.
		for(Integer �velseNr : apparat�velseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO apparat�velseI�velsegruppe(gruppenr, apparat�velsenr)" + 
					"VALUES(?,?)");
			stmt2.setInt(1, gruppenr);
			stmt2.setInt(2, �velseNr);
			stmt2.executeUpdate();
		}
		
		for(Integer �velseNr : fri�velseNrList) {
			PreparedStatement stmt2 = conn.prepareStatement(
					"INSERT INTO fri�velseI�velsegruppe(gruppenr, fri�velsenr)" + 
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
	
	//Henter ut en liste med alle fri�velser
	public List<String> getFriovelser(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT fri�velsenr, navn FROM fri�velse";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int �velsenr = rs.getInt("fri�velsenr");
			String �velsenavn = rs.getString("navn");
			String listStr = String.valueOf(�velsenr) + "," + �velsenavn;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}
	
	//Henter ut en liste med alle apparat�velser
	public List<String> getApparatovelser(Connection conn) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT apparat�velsenr, navn FROM apparat�velse";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int �velsenr = rs.getInt("apparat�velsenr");
			String �velsenavn = rs.getString("navn");
			String listStr = String.valueOf(�velsenr) + "," + �velsenavn;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}
	
	//Henter ut alle �velser, som brukes blant annet for � opprette �velsesgrupper
	public List<String> getAllovelser(Connection conn) throws SQLException{
		Statement st = conn.createStatement();
		String sql = "SELECT * FROM apparat�velse ";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int �velsenr = rs.getInt("apparat�velsenr");
			String �velsenavn = rs.getString("navn");
			String listStr = String.valueOf(�velsenr) + "," + �velsenavn;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}
	
	//Henter ut en liste med alle apparat�velser i �kter.
	public List<String> getApparatOvelseIOkt(Connection conn, int apparat�velseNr) throws SQLException {
		Statement st = conn.createStatement();
		String sql = "SELECT a�.apparat�velsenr, dato, navn, antallKilo, antallSett FROM apparat�velseI�kt a� "
				+ "INNER JOIN apparat�velse a ON (a.apparat�velsenr = a�.apparat�velsenr) "
				+ "INNER JOIN trenings�kt t� ON (t�.�ktnr = a�.�ktnr) "
				+ "WHERE a�.apparat�velsenr = " + apparat�velseNr;
		
		ResultSet rs = st.executeQuery(sql);
		List<String> apparatOvelseList = new ArrayList<String>();
		while(rs.next()) {
			String �velsenavn = rs.getString("navn");
			int antallkilo = rs.getInt("antallKilo");
			int antallsett = rs.getInt("antallSett");
			Date dato = rs.getDate("dato");
			String listStr = �velsenavn + "," + String.valueOf(antallkilo) + "," + String.valueOf(antallsett) + "," + dato;
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
	
	//Henter ut en liste med alle �velsesgrupper
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
		
	//Henter ut en liste med alle �velser i �velsesgrupper
	public List<String> getOvelserIGruppe(Connection conn, int gruppeNr) throws SQLException{
			Statement st = conn.createStatement();
			Statement st2 = conn.createStatement();
			String sql = "SELECT * FROM �velsegruppe �g"
					+ " INNER JOIN apparat�velseI�velsegruppe ag ON (ag.gruppenr = �g.gruppenr)"
					+ " INNER JOIN apparat�velse a� ON (a�.apparat�velsenr = ag.apparat�velsenr)"
					+ " WHERE ag.gruppenr = " + gruppeNr;
			
			String sql2 = "SELECT * FROM �velsegruppe �g"
					+ " INNER JOIN fri�velseI�velsegruppe fg ON (fg.gruppenr = �g.gruppenr)"
					+ " INNER JOIN fri�velse f� ON (f�.fri�velsenr = fg.fri�velsenr)"
					+ " WHERE fg.gruppenr = " + gruppeNr;
			
			ResultSet rs = st.executeQuery(sql);
			ResultSet rs2 = st2.executeQuery(sql2);
			List<String> apparat�velseList = new ArrayList<>();
			List<String> fri�velseList = new ArrayList<>();
			while(rs.next()) {
				int apparat�ktnr = rs.getInt("ag.apparat�velsenr");
				String apparatnavn = rs.getString("a�.navn");
				String apparatlistString = "�velsenr: " + String.valueOf(apparat�ktnr) + " �velsenavn: " + apparatnavn;
				apparat�velseList.add(apparatlistString);
			}
			while(rs2.next()) {
				int fri�ktnr = rs2.getInt("fg.fri�velsenr");
				String frinavn = rs2.getString("f�.navn");
				String frilistString = "�velsenr: " + String.valueOf(fri�ktnr) + " �velsenavn: " + frinavn;
				fri�velseList.add(frilistString);
			}
			apparat�velseList.addAll(fri�velseList);
			return apparat�velseList;
		}

	//Henter ut en liste med alle �velser, i tillegg til ekstra info som antall kilo og antall sett. 
	//Bruker i highscorelisten.
	public List<String> getApparatOvelserMedInfo(Connection conn) throws SQLException{
		Statement st = conn.createStatement();
		String sql = "SELECT a�.apparat�velsenr, navn, antallKilo, antallSett FROM apparat�velse a "
				+ "INNER JOIN apparat�velseI�kt a� ON (a�.apparat�velsenr = a.apparat�velsenr) "
				+ "ORDER BY navn ASC, antallKilo DESC";
		ResultSet rs = st.executeQuery(sql);
		List<String> ovelseList = new ArrayList<>();
		while(rs.next()) {
			int �velsenr = rs.getInt("a�.apparat�velsenr");
			String �velsenavn = rs.getString("navn");
			int antKilo = rs.getInt("antallKilo");
			int antSett = rs.getInt("antallSett");
			String listStr = "Nr: " + String.valueOf(�velsenr) + " // Navn: " + �velsenavn + " // Antall kilo: " + antKilo + " // Antall sett: " + antSett;
			ovelseList.add(listStr);
		}
		return ovelseList;
	}

}
