package main;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.Pane;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;


public class AppController{ 
	
	//Variabler for fx:id i FXML
	@FXML
	private TextField apparatNavn, apparatAntallKilo, apparatAntallSett, apparatOvelseNavn, friOvelseNavn;	
	
	@FXML
	private TextArea apparatBeskrivelse, friOvelseBeskrivelse;
	
	@FXML 
	private Button apparatReg, apparatOvelseReg, apparatVelg;
	
	@FXML
	private ComboBox<String> apparatDropdown;
	
	@FXML
	private Label apparatFeedback, apparatOvelseFeedback, friOvelseFeedback, valgtApparatLabel;
		
	@FXML
	private ListView<String> ovelseListView;
	
	Connection conn;
	
	public void initialize() throws Exception {
		DBConn dbconn = new DBConn();
		conn = dbconn.getConn();
		regApparatOvelse();
		regTreningsOkt();
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
				else if(apparatAntallKilo.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Antall kilo kan ikke være tomt!");
				}
				else if(apparatAntallSett.getText().isEmpty()) {
					apparatOvelseFeedback.setText("Antall sett kan ikke være tomt!");
				}
				else {
					try {
					//Splitter strengen som hentes ut fra databasen med "apparatNr.apparatNavn"
					//apparatNR hentes kun ut for at det skal bli riktig nr i tabellen apparatTilApparatØvelse
					dbhandler.registrerApparatOvelse(conn, apparatNr, apparatOvelseNavn.getText(), Integer.valueOf(apparatAntallKilo.getText()), Integer.valueOf(apparatAntallSett.getText()));
					apparatOvelseFeedback.setText(apparatOvelseNavn.getText() + " er lagt til i databasen.");
					
					//----------------------------------------//
					//Gjør diverse endringer som å sette panel, knapper og label til usynlig.
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
	
	public void regFriOvelse() {
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
	}

	public void regTreningsOkt() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		List<String> ovelseList = dbhandler.getOvelser(conn);
		
	}
}
