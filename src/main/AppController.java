package main;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.Pane;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.ButtonGroup;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;


public class AppController{ 
	
	//Variabler for fx:id i FXML
	@FXML
	private TextField apparatNavn, apparatAntallKilo, apparatAntallSett, apparatOvelseNavn, friOvelseNavn, oktVarighet, oktForm, oktPrestasjon, oktTidspunkt, nSiste, ovGruppeNavn;	
	
	@FXML
	private TextArea apparatBeskrivelse, friOvelseBeskrivelse;
	
	@FXML 
	private Button apparatReg, apparatOvelseReg, apparatVelg, regTreningsokt, ovelseVelg, ovGruppeReg;
	
	@FXML
	private ComboBox<String> apparatDropdown, resultatOvelseDropdown, ovGruppeDropdown;
	
	@FXML
	private DatePicker oktDato, resultatFraDato, resultatTildato;
	
	@FXML
	private Label apparatFeedback, apparatOvelseFeedback, friOvelseFeedback, valgtApparatLabel, treningsoktFeedback, nSisteOutput, resultatloggOutput, ovGruppeFeedback;
		
	@FXML
	private ListView<String> ovelseListView, highscoreListview, oktListview, ovGruppeList, ovGruppeList1 , resultatListview;	
	Connection conn;
	
	public void initialize() throws Exception {
		DBConn dbconn = new DBConn();
		conn = dbconn.getConn();
		regApparatOvelse();
		regTreningsokt();
		regOvelsegruppe();
		visOvelssgrupper();
	}
	
	//Funksjon for å registerer apparat
	//Denne snakker med registrerApparat i DBHandler
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
	
	public void regApparatOvelse() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatList = dbhandler.getApparat(conn);
		apparatDropdown.getItems().clear();
		apparatDropdown.getItems().addAll(apparatList);
		apparatDropdown.setValue(apparatDropdown.getItems().get(0));
		
		//Velger apparat slik at valgApparatSplit-variabelen oppdateres om man endrer apparat i listen.
		apparatVelg.setOnAction((event) -> {
			String valgtApparat = apparatDropdown.getSelectionModel().getSelectedItem().toString();
			System.out.println(valgtApparat);
			String[] valgtApparatSplit = valgtApparat.split(",");
			int apparatNr = Integer.valueOf(valgtApparatSplit[0]);
			valgtApparatLabel.setText(valgtApparatSplit[1]);
			apparatOvelseReg.setVisible(true);
			//Registrerer apparatøvelse
			//Sjekker om noen av feltene er tomme
			apparatOvelseReg.setOnAction((event1) -> {
				if(apparatOvelseNavn.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Navnet kan ikke være tomt!");
				}
				else {
					try {
					//Splitter strengen som hentes ut fra databasen med "apparatNr.apparatNavn"
					//apparatNR hentes kun ut for at det skal bli riktig nr i tabellen apparatTilApparatØvelse
					dbhandler.registrerApparatOvelse(conn, apparatNr, apparatOvelseNavn.getText());
					apparatOvelseFeedback.setText(apparatOvelseNavn.getText() + " er lagt til i databasen.");
					regTreningsokt();
					
					//----------------------------------------//
					//Gjør diverse endringer som å sette panel, knapper og label til usynlig.
					apparatOvelseNavn.setText("");
					apparatOvelseReg.setVisible(false);
					//--------------------------------------//
					
					} catch (SQLException e) {
					e.printStackTrace();
					}
				}
			});
		});
	}
	
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
				
				//----------------------------------------//
				//Gjør diverse endringer som å sette panel, knapper og label til usynlig.
				friOvelseNavn.setText("");
				friOvelseBeskrivelse.setText("");
				//--------------------------------------//
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		regTreningsokt();
	}

	public void regTreningsokt() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		//List<String> ovelseList = dbhandler.getApparatOvelseIOkt(conn);
		
		//Legger til elementene fra getOvelser i listview
		//ovelseListView.setItems(FXCollections.observableArrayList(ovelseList));
		ovelseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ovelseListView.getSelectionModel().select(0);
		
		//Dette skjer når knappen trykkes
		regTreningsokt.setOnAction((event) -> {
			
			//Oppretter en liste med øvelseNr for de øvelsene som er valgt.
			List<String> valgteØvelser = ovelseListView.getSelectionModel().getSelectedItems();
			List<Integer> øvelseNrList = new ArrayList<>();
			
			for(String øvelse : valgteØvelser) {
				String[] øvelseSplit = øvelse.split(",");
				øvelseNrList.add(Integer.valueOf(øvelseSplit[0]));
			}
			
			
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
			else if(ovelseListView.getSelectionModel().getSelectedItem().isEmpty()) {
				treningsoktFeedback.setText("Treningsøkten må inneholde minst en treningsøkt!");
			}
			else {
				try {
					//Setter inn i databasen
					dbhandler.registrerOkt(conn, Date.valueOf(oktDato.getValue()), Time.valueOf(oktTidspunkt.getText()), Integer.valueOf(oktVarighet.getText()), 
					Integer.valueOf(oktForm.getText()), Integer.valueOf(oktPrestasjon.getText()), øvelseNrList);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Gir feedback til brukeren og setter feltene til tomme igjen
				treningsoktFeedback.setText("Treningsøkten på dagen: " + Date.valueOf(oktDato.getValue()) + " med " + øvelseNrList.size() + " øvelser har blitt lagt til i databasen");
				oktTidspunkt.setText("00:00:00");
				oktVarighet.setText("");
				oktForm.setText("");
				oktPrestasjon.setText("");
			}
		});	
	}

	//Henter apparatøvelsene som er gruppert med synkende verdi på antall kilo
	//Legger dette i et listview.
	public void updateHighscore() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> apparatOvelseList = dbhandler.getApparatOvelseIOkt(conn);
		
		//Legger til elementene fra getOvelser i listview
		highscoreListview.setItems(FXCollections.observableArrayList(apparatOvelseList));
		highscoreListview.getSelectionModel().select(0);
	}
	
	public void viseSisteOkter () throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> oktList = dbhandler.getØkter(conn);
		List<String> outList = new ArrayList<>();
		if (nSiste.getText().isEmpty()) {
			nSisteOutput.setText("Kan ikke være tom");
		}
		else if(Integer.valueOf(nSiste.getText()) > oktList.size()) {
			nSisteOutput.setText("Det finnes ikke så mange treningsøkter i listen");
		}
		
		else {
		
		for (int i=0; i < Integer.valueOf(nSiste.getText()); i++) {
			outList.add(oktList.get(i));
		}
		oktListview.setItems(FXCollections.observableArrayList(outList));
		nSisteOutput.setText("");
		}
		}
	
	public void resultatloggDato() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> ovelser = dbhandler.getApparatOvelseIOkt(conn);
		resultatOvelseDropdown.getItems().clear();
		resultatOvelseDropdown.getItems().addAll(ovelser);
		
		ovelseVelg.setOnAction((event) -> {
			String valgtOvelse = resultatOvelseDropdown.getSelectionModel().getSelectedItem().toString();
			System.out.println(valgtOvelse);
			String[] valgsOvelseSplit = valgtOvelse.split(",");
			int ovelseNr = Integer.valueOf(valgsOvelseSplit[0]);
			String ovelseNavn = valgsOvelseSplit[1];
			
			
		});
		
		LocalDate datoFra = resultatFraDato.getValue();
		LocalDate datoTil = resultatTildato.getValue();
		
		//System.out.println("" + datoFra + datoTil);
	}
	
	//Funksjon for å registrere øvelsesgruppe
	public void regOvelsegruppe() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> ovelseList = dbhandler.getOvelser(conn);
		
		//Legger til elemente fra getOvelser i list view
		ovGruppeList.setItems(FXCollections.observableArrayList(ovelseList));
		ovGruppeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		//Oppretter en liste med øvelseNr for de øvelsene som er valgt.
		List<String> valgteØvelser = ovGruppeList.getSelectionModel().getSelectedItems();
		List<Integer> øvelseNrList = new ArrayList<>();
		
		
		ovGruppeReg.setOnAction((event) -> {
			//Oppretter en liste med øvelsesnr fra de øvelse som er valgt
			for(String øvelse : valgteØvelser) {
				String[] øvelseSplit = øvelse.split(",");
				øvelseNrList.add(Integer.valueOf(øvelseSplit[0]));
			}
			
			//Sjekker om noen av feltene er tomme
			if(ovGruppeNavn.getText().isEmpty()) {
				ovGruppeFeedback.setText("Navnet kan ikke være tomt!");
			}
			else if(ovelseListView.getSelectionModel().getSelectedItem().isEmpty()) {
				ovGruppeFeedback.setText("Øvelsesgruppen må inneholde minst en øvelse!");
			}
			//Legger til i databasen
			else {
				try {
					dbhandler.registrerOvelsesGruppe(conn, ovGruppeNavn.getText(), øvelseNrList);
					ovGruppeFeedback.setText("Øvelsegruppe: " + ovGruppeNavn.getText() + " har blitt lagt til i databasen med " + øvelseNrList.size() + " øvelser");
					ovGruppeNavn.setText("");
					visOvelssgrupper();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
			}
		});	
	}

	//Funksjon for å vise øvelsesgrupper
	public void visOvelssgrupper() throws SQLException{
		DBHandler dbhandler = new DBHandler();
		List<String> ovelsegruppeList = dbhandler.getOvelsesGrupper(conn);
		
		ovGruppeDropdown.getItems().clear();
		ovGruppeDropdown.getItems().addAll(ovelsegruppeList);
		
		ovGruppeDropdown.setOnAction((event) -> {
			String gruppevalgt = ovGruppeDropdown.getSelectionModel().getSelectedItem().toString();
			String[] gruppevalgtsplit = gruppevalgt.split(",");
			int gruppenr = Integer.valueOf(gruppevalgtsplit[0]);
			try {
				ovGruppeList1.setItems(FXCollections.observableArrayList(dbhandler.getOvelserIGruppe(conn, gruppenr)));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		});
	}
}