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
	private TextField apparatNavn, apparatAntallKilo, apparatAntallSett, apparatOvelseNavn, friOvelseNavn, oktVarighet, oktForm, oktPrestasjon, oktTidspunkt;	
	
	@FXML
	private TextArea apparatBeskrivelse, friOvelseBeskrivelse;
	
	@FXML 
	private Button apparatReg, apparatOvelseReg, apparatVelg, regTreningsokt;
	
	@FXML
	private ComboBox<String> apparatDropdown;
	
	@FXML
	private DatePicker oktDato;
	
	@FXML
	private Label apparatFeedback, apparatOvelseFeedback, friOvelseFeedback, valgtApparatLabel, treningsoktFeedback;
	
	@FXML
	private Slider sliderMin, sliderSec;
		
	@FXML
	private ListView<String> ovelseListView;
	
	Connection conn;
	
	public void initialize() throws Exception {
		DBConn dbconn = new DBConn();
		conn = dbconn.getConn();
		regApparatOvelse();
		regTreningsokt();
	}
	
	//Funksjon for � registerer apparat
	//Denne snakker med registrerApparat i DBHandler
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
			//Registrerer apparat�velse
			//Sjekker om noen av feltene er tomme
			apparatOvelseReg.setOnAction((event1) -> {
				if(apparatOvelseNavn.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Navnet kan ikke v�re tomt!");
				}
				else if(apparatAntallKilo.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Antall kilo kan ikke v�re tomt!");
				}
				else if(apparatAntallSett.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Antall sett kan ikke v�re tomt!");
				}
				else {
					try {
					//Splitter strengen som hentes ut fra databasen med "apparatNr.apparatNavn"
					//apparatNR hentes kun ut for at det skal bli riktig nr i tabellen apparatTilApparat�velse
					dbhandler.registrerApparatOvelse(conn, apparatNr, apparatOvelseNavn.getText(), Integer.valueOf(apparatAntallKilo.getText()), Integer.valueOf(apparatAntallSett.getText()));
					apparatOvelseFeedback.setText(apparatOvelseNavn.getText() + " er lagt til i databasen.");
					regTreningsokt();
					
					//----------------------------------------//
					//Gj�r diverse endringer som � sette panel, knapper og label til usynlig.
					apparatOvelseNavn.setText("");
					apparatAntallKilo.setText("");
					apparatAntallSett.setText("");
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

	public void regTreningsokt() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> ovelseList = dbhandler.getOvelser(conn);
		
		//Legger til elementene fra getOvelser i listview
		ovelseListView.setItems(FXCollections.observableArrayList(ovelseList));
		ovelseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ovelseListView.getSelectionModel().select(0);
		
		//Dette skjer n�r knappen trykkes
		regTreningsokt.setOnAction((event) -> {
			
			//Oppretter en liste med �velseNr for de �velsene som er valgt.
			List<String> valgte�velser = ovelseListView.getSelectionModel().getSelectedItems();
			List<Integer> �velseNrList = new ArrayList<>();
			
			for(String �velse : valgte�velser) {
				System.out.println(�velse.toString());
				String[] �velseSplit = �velse.split(",");
				�velseNrList.add(Integer.valueOf(�velseSplit[0]));
			}
			
			
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
			else if(ovelseListView.getSelectionModel().getSelectedItem().isEmpty()) {
				treningsoktFeedback.setText("Trenings�kten m� inneholde minst en trenings�kt!");
			}
			else {
				try {
					//Setter inn i databasen
					dbhandler.registrerOkt(conn, Date.valueOf(oktDato.getValue()), Time.valueOf(oktTidspunkt.getText()), Integer.valueOf(oktVarighet.getText()), 
					Integer.valueOf(oktForm.getText()), Integer.valueOf(oktPrestasjon.getText()), �velseNrList);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//Gir feedback til brukeren og setter feltene til tomme igjen
				treningsoktFeedback.setText("Trenings�kten p� dagen: " + Date.valueOf(oktDato.getValue()) + " med " + �velseNrList.size() + " �velser har blitt lagt til i databasen");
				oktTidspunkt.setText("00:00:00");
				oktVarighet.setText("");
				oktForm.setText("");
				oktPrestasjon.setText("");
			}
		});	
	}
}
