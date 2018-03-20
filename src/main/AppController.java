package main;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;


public class AppController{ 
	
	//Variabler for fx:id i FXML
	@FXML
	private TextField apparatNavn, apparatAntKilo, apparatAntSett, apparatOvelseNavn, friOvelseNavn, oktVarighet, oktForm, oktPrestasjon, oktTidspunkt, nSiste, ovGruppeNavn;	
	
	@FXML
	private TextArea apparatBeskrivelse, friOvelseBeskrivelse, friovelseKommentar;
	
	@FXML 
	private Button apparatReg, apparatOvelseReg, apparatVelg, regTreningsokt, ovelseVelg, ovGruppeReg, addApparatovelse, addFriovelse;
	
	@FXML
	private ComboBox<String> apparatDropdown, resultatOvelseDropdown, ovGruppeDropdown, velgApparatovelse, velgFriovelse;
	
	@FXML
	private DatePicker oktDato, resultatFraDato, resultatTilDato;
	
	@FXML
	private Label apparatFeedback, apparatOvelseFeedback, friOvelseFeedback, treningsoktFeedback, nSisteOutput, resultatloggOutput, ovGruppeFeedback;
		
	@FXML
	private ListView<String> ovelseListView, highscoreListview, oktListview, ovGruppeList1, ovGruppeList2, ovGruppeList3, resultatListview, oktApparatovelser, oktFriovelser;	
	Connection conn;
	
	public void initialize() throws Exception {
		DBConn dbconn = new DBConn();
		conn = dbconn.getConn();
		regApparatOvelse();
		regTreningsokt();
		regOvelsegruppe();
		visOvelsegrupper();
		resultatloggDato();
	}
	
	//Funksjon for � registerer apparat
	public void regApparat() throws SQLException {	
		if(apparatNavn.getText().isEmpty()) {
			apparatFeedback.setText("Navnet kan ikke v�re tomt!");
		}
		else if(apparatBeskrivelse.getText().isEmpty()) {
			apparatFeedback.setText("Beskrivelsen kan ikke v�re tom!");
		}
		else {
			DBHandler dbhandler = new DBHandler();
			String navn = apparatNavn.getText();
			String beskrivelse = apparatBeskrivelse.getText();
			dbhandler.registrerApparat(conn, navn, beskrivelse);
			apparatFeedback.setText(navn + " er lagt til i databasen.");
			apparatNavn.setText("");
			apparatBeskrivelse.setText("");
			regApparatOvelse();
		}
	}
	
	//Funksjon for � registrere apparat�velse
	public void regApparatOvelse() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatList = dbhandler.getApparat(conn);
		
		if(!apparatDropdown.getItems().isEmpty()) {
			apparatDropdown.getItems().clear();
		}
		apparatDropdown.getItems().addAll(apparatList);
		
		//Velger apparat slik at valgApparatSplit-variabelen oppdateres om man endrer apparat i listen.
		apparatDropdown.setOnAction((event) -> {
			String valgtApparat = apparatDropdown.getSelectionModel().getSelectedItem().toString();
			System.out.println(valgtApparat);
			String[] valgtApparatSplit = valgtApparat.split(",");
			int apparatNr = Integer.valueOf(valgtApparatSplit[0]);
			//Registrerer apparat�velse
			//Sjekker om noen av feltene er tomme
			apparatOvelseReg.setOnAction((event1) -> {
				if(apparatOvelseNavn.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Navnet kan ikke v�re tomt!");
				}
				else {
					try {
					//Splitter strengen som hentes ut fra databasen med "apparatNr.apparatNavn"
					//apparatNR hentes kun ut for at det skal bli riktig nr i tabellen apparatTilApparat�velse
					dbhandler.registrerApparatOvelse(conn, apparatNr, apparatOvelseNavn.getText());
					apparatOvelseFeedback.setText(apparatOvelseNavn.getText() + " er lagt til i databasen.");
					regTreningsokt();
					regOvelsegruppe();
					
					//----------------------------------------//
					//Gj�r diverse endringer som � sette panel, knapper og label til usynlig.
					apparatOvelseNavn.setText("");
					//--------------------------------------//
					
					} catch (SQLException e) {
					e.printStackTrace();
					}
				}
			});
		});
	}
	
	//Funksjon for � registrere fri�velse
	public void regFriOvelse() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		//registrer fri�velse
		//Sjekker om noen av feltene er tomme
		if(friOvelseNavn.getText().isEmpty()) {
			friOvelseFeedback.setText("Navnet kan ikke v�re tomt!");
		}
		else if (friOvelseBeskrivelse.getText().isEmpty()) {
			friOvelseFeedback.setText("Beskrivelsen kan ikke v�re tom!");
		}
		else {
			try {
				dbhandler.registrerFriOvelse(conn, friOvelseNavn.getText(), friOvelseBeskrivelse.getText());
				friOvelseFeedback.setText(friOvelseNavn.getText() + " er lagt til i databasen.");
				regOvelsegruppe();
				
				//----------------------------------------//
				//Gj�r diverse endringer som � sette panel, knapper og label til usynlig.
				friOvelseNavn.setText("");
				friOvelseBeskrivelse.setText("");
				//--------------------------------------//
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		regTreningsokt();
	}

	//Funksjon for � registrere trenings�kt
	public void regTreningsokt() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		
		//Legger til elementene fra getOvelser i listview
		//ovelseListView.setItems(FXCollections.observableArrayList(ovelseList));
		
		velgApparatovelse.getItems().clear();
		velgApparatovelse.getItems().addAll(dbhandler.getApparatovelser(conn));
		
		velgFriovelse.getItems().clear();
		velgFriovelse.getItems().addAll(dbhandler.getFriovelser(conn));
		
		List<String> valgteApparatovelser = new ArrayList<String>();
		List<String> valgteFriovelser = new ArrayList<String>();

		
		addApparatovelse.setOnAction((e1) -> {
			String temp = velgApparatovelse.getSelectionModel().getSelectedItem().toString() + "," + apparatAntKilo.getText() + "," + apparatAntSett.getText();
			valgteApparatovelser.add(temp);
			oktApparatovelser.getItems().clear();
			oktApparatovelser.setItems(FXCollections.observableArrayList(valgteApparatovelser));
			apparatAntKilo.setText("");
			apparatAntSett.setText("");
		});
		
		addFriovelse.setOnAction((e1) -> {
			String temp = velgFriovelse.getSelectionModel().getSelectedItem().toString() + "," + friovelseKommentar.getText();
			valgteFriovelser.add(temp);
			oktFriovelser.getItems().clear();
			oktFriovelser.setItems(FXCollections.observableArrayList(valgteFriovelser));
			friovelseKommentar.setText("");
		});
		
		//Dette skjer n�r knappen trykkes
		regTreningsokt.setOnAction((event) -> {
			//Sjekker om noen felt er tomme, og at tidspunktfeltet matcher formatet hh:mm:ss			
			if(!oktTidspunkt.getText().matches("^([0-9]{2}):([0-9]{2}):([0-9]{2})")) {
			treningsoktFeedback.setText("Tidspunktet m� v�re p� formatet hh:mm:ss!");
			}
			else if(oktTidspunkt.getText().isEmpty()) {
				treningsoktFeedback.setText("Tidspunkt kan ikke v�re tomt!");
			}
			else if(oktVarighet.getText().isEmpty()) {
				treningsoktFeedback.setText("Varighet kan ikke v�re tomt!");
			}
			else if(oktForm.getText().isEmpty()) {
				treningsoktFeedback.setText("Form kan ikke v�re tomt!");
			}
			else if(oktPrestasjon.getText().isEmpty()) {
				treningsoktFeedback.setText("Prestasjon kan ikke v�re tomt!");
			}
			else {
				try {
					//Setter inn i databasen
					dbhandler.registrerOkt(conn, Date.valueOf(oktDato.getValue()), Time.valueOf(oktTidspunkt.getText()), Integer.valueOf(oktVarighet.getText()), 
					Integer.valueOf(oktForm.getText()), Integer.valueOf(oktPrestasjon.getText()), valgteApparatovelser, valgteFriovelser);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Gir feedback til brukeren og setter feltene til tomme igjen
				treningsoktFeedback.setText("Trenings�kten p� dagen: " + Date.valueOf(oktDato.getValue()) + " med " + (valgteApparatovelser.size() + valgteFriovelser.size()) + " �velser har blitt lagt til i databasen");
				oktTidspunkt.setText("00:00:00");
				oktVarighet.setText("");
				oktForm.setText("");
				oktPrestasjon.setText("");
				oktApparatovelser.getItems().clear();
				oktFriovelser.getItems().clear();
				valgteApparatovelser.clear();
				valgteFriovelser.clear();
			}
		});	
	}

	//Henter apparat�velsene som er gruppert med synkende verdi p� antall kilo
	//Legger dette i et listview.
	public void updateHighscore() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatOvelseList = dbhandler.getApparatOvelserMedInfo(conn);
		
	
		
		//Legger til elementene fra getOvelser i listview
		highscoreListview.setItems(FXCollections.observableArrayList(apparatOvelseList));
		highscoreListview.getSelectionModel().select(0);
		
	}
	
	//Funksjon for � vise n-siste �kter
	public void viseSisteOkter () throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> oktList = dbhandler.get�kter(conn);
		List<String> outList = new ArrayList<>();
		if (nSiste.getText().isEmpty()) {
			nSisteOutput.setText("Kan ikke v�re tom");
		}
		else if(Integer.valueOf(nSiste.getText()) > oktList.size()) {
			nSisteOutput.setText("Det finnes ikke s� mange trenings�kter i listen");
		}
		
		else {
		
		for (int i=0; i < Integer.valueOf(nSiste.getText()); i++) {
			outList.add(oktList.get(i));
		}
		oktListview.setItems(FXCollections.observableArrayList(outList));
		nSisteOutput.setText("");
		}
		}

	//Funksjon for � vise �velser mellom to datoer
	public void resultatloggDato() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		resultatOvelseDropdown.getItems().clear();
		resultatOvelseDropdown.getItems().addAll(dbhandler.getApparatovelser(conn));
	
		ovelseVelg.setOnAction((event) -> {
			Date datoFra = Date.valueOf(resultatFraDato.getValue());
			Date datoTil = Date.valueOf(resultatTilDato.getValue());
			String valgtOvelse = resultatOvelseDropdown.getSelectionModel().getSelectedItem().toString();
			String[] valgtOvelseSplit = valgtOvelse.split(",");
			String ovelseNr = valgtOvelseSplit[0];
			List<String> ovelser = new ArrayList<>();
			
			try {
				ovelser = dbhandler.getApparatOvelseIOkt(conn, Integer.valueOf(ovelseNr));
			} catch (NumberFormatException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			List<String> outList = new ArrayList<>();
			
			for (String ovelse : ovelser) {
				String[] split = ovelse.split(",");
				Date dato = Date.valueOf(split[3]);
				if (dato.getTime() <= datoTil.getTime() && dato.getTime() >= datoFra.getTime()) {
					outList.add(ovelse);
				}
			}	
			resultatListview.setItems(FXCollections.observableArrayList(outList));
		});
	}
	
	//Funksjon for � registrere �velsesgruppe
	public void regOvelsegruppe() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatOvelseList = dbhandler.getApparatovelser(conn);
		List<String> friOvelseList = dbhandler.getFriovelser(conn);

		//Legger til elemente fra getOvelser i list view
		ovGruppeList1.setItems(FXCollections.observableArrayList(apparatOvelseList));
		ovGruppeList1.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		ovGruppeList2.setItems(FXCollections.observableArrayList(friOvelseList));
		ovGruppeList2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		//Oppretter en liste med �velseNr for de �velsene som er valgt.
		List<String> valgteapparat�velser = ovGruppeList1.getSelectionModel().getSelectedItems();
		List<String> valgtefri�velser = ovGruppeList2.getSelectionModel().getSelectedItems();
		List<Integer> apparat�velseNrList = new ArrayList<>();
		List<Integer> fri�velseNrList = new ArrayList<>();
		
		ovGruppeReg.setOnAction((event) -> {
			//Oppretter en liste med �velsesnr fra de �velse som er valgt
			for(String �velse : valgteapparat�velser) {
				String[] �velseSplit = �velse.split(",");
				apparat�velseNrList.add(Integer.valueOf(�velseSplit[0]));
			}

			for(String �velse : valgtefri�velser) {
				String[] �velseSplit = �velse.split(",");
				fri�velseNrList.add(Integer.valueOf(�velseSplit[0]));
			}
			
			//Sjekker om noen av feltene er tomme
			if(ovGruppeNavn.getText().isEmpty()) {
				ovGruppeFeedback.setText("Navnet kan ikke v�re tomt!");
			}
			//Legger til i databasen
			else {
				try {
					int str = apparat�velseNrList.size() + fri�velseNrList.size();
					dbhandler.registrerOvelsesGruppe(conn, ovGruppeNavn.getText(), apparat�velseNrList, fri�velseNrList);
					ovGruppeFeedback.setText("�velsegruppe: " + ovGruppeNavn.getText() + " har blitt lagt til i databasen med " + str + " �velser");
					ovGruppeNavn.setText("");
					visOvelsegrupper();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

	//Funksjon for � vise �velsesgrupper
	public void visOvelsegrupper() throws SQLException{
		DBHandler dbhandler = new DBHandler();
		List<String> ovelsegruppeList = dbhandler.getOvelsesGrupper(conn);
		
		ovGruppeDropdown.getItems().clear();
		ovGruppeDropdown.getItems().addAll(ovelsegruppeList);
		
		ovGruppeDropdown.setOnAction((event) -> {
			String gruppevalgt = ovGruppeDropdown.getSelectionModel().getSelectedItem().toString();
			String[] gruppevalgtsplit = gruppevalgt.split(",");
			int gruppenr = Integer.valueOf(gruppevalgtsplit[0]);
			try {
				ovGruppeList3.setItems(FXCollections.observableArrayList(dbhandler.getOvelserIGruppe(conn, gruppenr)));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
	}
}