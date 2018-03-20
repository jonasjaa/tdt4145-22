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
	
	//Funksjon for ø registerer apparat
	public void regApparat() throws SQLException {	
		if(apparatNavn.getText().isEmpty()) {
			apparatFeedback.setText("Navnet kan ikke være tomt!");
		}
		else if(apparatBeskrivelse.getText().isEmpty()) {
			apparatFeedback.setText("Beskrivelsen kan ikke være tom!");
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
	
	//Funksjon for å registrere apparatøvelse
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
			//Registrerer apparatøvelse
			//Sjekker om noen av feltene er tomme
			apparatOvelseReg.setOnAction((event1) -> {
				if(apparatOvelseNavn.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Navnet kan ikke være tomt!");
				}
				else {
					try {
					//Splitter strengen som hentes ut fra databasen med "apparatNr.apparatNavn"
					//apparatNR hentes kun ut for at det skal bli riktig nr i tabellen apparatTilApparatøvelse
					dbhandler.registrerApparatOvelse(conn, apparatNr, apparatOvelseNavn.getText());
					apparatOvelseFeedback.setText(apparatOvelseNavn.getText() + " er lagt til i databasen.");
					regTreningsokt();
					regOvelsegruppe();
					
					//----------------------------------------//
					//Gjør diverse endringer som ø sette panel, knapper og label til usynlig.
					apparatOvelseNavn.setText("");
					//--------------------------------------//
					
					} catch (SQLException e) {
					e.printStackTrace();
					}
				}
			});
		});
	}
	
	//Funksjon for å registrere friøvelse
	public void regFriOvelse() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		//registrer friøvelse
		//Sjekker om noen av feltene er tomme
		if(friOvelseNavn.getText().isEmpty()) {
			friOvelseFeedback.setText("Navnet kan ikke være tomt!");
		}
		else if (friOvelseBeskrivelse.getText().isEmpty()) {
			friOvelseFeedback.setText("Beskrivelsen kan ikke være tom!");
		}
		else {
			try {
				dbhandler.registrerFriOvelse(conn, friOvelseNavn.getText(), friOvelseBeskrivelse.getText());
				friOvelseFeedback.setText(friOvelseNavn.getText() + " er lagt til i databasen.");
				regOvelsegruppe();
				
				//----------------------------------------//
				//Gjør diverse endringer som ø sette panel, knapper og label til usynlig.
				friOvelseNavn.setText("");
				friOvelseBeskrivelse.setText("");
				//--------------------------------------//
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		regTreningsokt();
	}

	//Funksjon for å registrere treningsøkt
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
		
		//Dette skjer nør knappen trykkes
		regTreningsokt.setOnAction((event) -> {
			//Sjekker om noen felt er tomme, og at tidspunktfeltet matcher formatet hh:mm:ss			
			if(!oktTidspunkt.getText().matches("^([0-9]{2}):([0-9]{2}):([0-9]{2})")) {
			treningsoktFeedback.setText("Tidspunktet må være på formatet hh:mm:ss!");
			}
			else if(oktTidspunkt.getText().isEmpty()) {
				treningsoktFeedback.setText("Tidspunkt kan ikke være tomt!");
			}
			else if(oktVarighet.getText().isEmpty()) {
				treningsoktFeedback.setText("Varighet kan ikke være tomt!");
			}
			else if(oktForm.getText().isEmpty()) {
				treningsoktFeedback.setText("Form kan ikke være tomt!");
			}
			else if(oktPrestasjon.getText().isEmpty()) {
				treningsoktFeedback.setText("Prestasjon kan ikke være tomt!");
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
				treningsoktFeedback.setText("Treningsøkten på dagen: " + Date.valueOf(oktDato.getValue()) + " med " + (valgteApparatovelser.size() + valgteFriovelser.size()) + " øvelser har blitt lagt til i databasen");
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

	//Henter apparatøvelsene som er gruppert med synkende verdi pø antall kilo
	//Legger dette i et listview.
	public void updateHighscore() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatOvelseList = dbhandler.getApparatOvelserMedInfo(conn);
		
	
		
		//Legger til elementene fra getOvelser i listview
		highscoreListview.setItems(FXCollections.observableArrayList(apparatOvelseList));
		highscoreListview.getSelectionModel().select(0);
		
	}
	
	//Funksjon for å vise n-siste økter
	public void viseSisteOkter () throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> oktList = dbhandler.getØkter(conn);
		List<String> outList = new ArrayList<>();
		if (nSiste.getText().isEmpty()) {
			nSisteOutput.setText("Kan ikke være tom");
		}
		else if(Integer.valueOf(nSiste.getText()) > oktList.size()) {
			nSisteOutput.setText("Det finnes ikke sø mange treningsøkter i listen");
		}
		
		else {
		
		for (int i=0; i < Integer.valueOf(nSiste.getText()); i++) {
			outList.add(oktList.get(i));
		}
		oktListview.setItems(FXCollections.observableArrayList(outList));
		nSisteOutput.setText("");
		}
		}

	//Funksjon for å vise øvelser mellom to datoer
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
	
	//Funksjon for ø registrere øvelsesgruppe
	public void regOvelsegruppe() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatOvelseList = dbhandler.getApparatovelser(conn);
		List<String> friOvelseList = dbhandler.getFriovelser(conn);

		//Legger til elemente fra getOvelser i list view
		ovGruppeList1.setItems(FXCollections.observableArrayList(apparatOvelseList));
		ovGruppeList1.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		ovGruppeList2.setItems(FXCollections.observableArrayList(friOvelseList));
		ovGruppeList2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		//Oppretter en liste med øvelseNr for de øvelsene som er valgt.
		List<String> valgteapparatøvelser = ovGruppeList1.getSelectionModel().getSelectedItems();
		List<String> valgtefriøvelser = ovGruppeList2.getSelectionModel().getSelectedItems();
		List<Integer> apparatøvelseNrList = new ArrayList<>();
		List<Integer> friøvelseNrList = new ArrayList<>();
		
		ovGruppeReg.setOnAction((event) -> {
			//Oppretter en liste med øvelsesnr fra de øvelse som er valgt
			for(String øvelse : valgteapparatøvelser) {
				String[] øvelseSplit = øvelse.split(",");
				apparatøvelseNrList.add(Integer.valueOf(øvelseSplit[0]));
			}

			for(String øvelse : valgtefriøvelser) {
				String[] øvelseSplit = øvelse.split(",");
				friøvelseNrList.add(Integer.valueOf(øvelseSplit[0]));
			}
			
			//Sjekker om noen av feltene er tomme
			if(ovGruppeNavn.getText().isEmpty()) {
				ovGruppeFeedback.setText("Navnet kan ikke være tomt!");
			}
			//Legger til i databasen
			else {
				try {
					int str = apparatøvelseNrList.size() + friøvelseNrList.size();
					dbhandler.registrerOvelsesGruppe(conn, ovGruppeNavn.getText(), apparatøvelseNrList, friøvelseNrList);
					ovGruppeFeedback.setText("øvelsegruppe: " + ovGruppeNavn.getText() + " har blitt lagt til i databasen med " + str + " øvelser");
					ovGruppeNavn.setText("");
					visOvelsegrupper();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

	//Funksjon for ø vise øvelsesgrupper
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