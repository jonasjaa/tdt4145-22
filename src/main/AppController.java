package main;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.swing.ButtonGroup;

import javafx.fxml.FXML;

public class AppController{ 
	
	//Variabler for fx:id i FXML
	@FXML
	private TextField apparatNavn, apparatAntallKilo, apparatAntallSett, ovelseNavn;	
	
	@FXML
	private TextArea apparatBeskrivelse, friOvelseBeskrivelse;
	
	@FXML 
	private Button apparatReg, apparatOvelseReg, friOvelseReg, apparatVelg;
	
	@FXML
	private RadioButton apparatOvelseRB, friOvelseRB;
	
	@FXML
	private Pane apparatOvelsePane, friOvelsePane;
	
	@FXML
	private ComboBox<String> apparatDropdown;
	
	@FXML
	private Label apparatFeedback, ovelseFeedback, labelantkilo, labelantsett;
	
	Connection conn;
	
	public void initialize() throws Exception {
		DBConn dbconn = new DBConn();
		conn = dbconn.getConn();
		regOvelse();
	}
	
	//Funksjon for å registerer apparat
	//Denne snakker med registrerApparat i DBHandler
	public void regApparat() throws SQLException {
		DBHandler dbhandler = new DBHandler();
		String navn = apparatNavn.getText();
		String beskrivelse = apparatBeskrivelse.getText();
		dbhandler.registrerApparat(conn, navn, beskrivelse);
		apparatFeedback.setText(navn + " er lagt til i databasen.");
		apparatNavn.setText("");
		apparatBeskrivelse.setText("");
	}
	
	//Funksjon for å registerer øvelse
	//Denne snakker med registrerApparat i DBHandler
	public void regOvelse() throws SQLException {		
		DBHandler dbhandler = new DBHandler();
		ToggleGroup radiobuttons = new ToggleGroup();
		apparatOvelseRB.setToggleGroup(radiobuttons);	
		friOvelseRB.setToggleGroup(radiobuttons);
		//Sjekker hvilken radiobutton som er trykket på.
		//Registrere apparatøvelse
		if(apparatOvelseRB.isSelected()) {
			friOvelseRB.setDisable(true);
			apparatOvelsePane.setVisible(true);
			//Henter ut listen fra getapparat fra DBHandler, slik at brukeren skal kunne velge apparat
			//Henter ut, legger til i comboboxen, og viser første element i listen.
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
				apparatAntallKilo.setVisible(true);
				apparatAntallSett.setVisible(true);
				labelantkilo.setVisible(true);
				labelantsett.setVisible(true);
				apparatOvelseReg.setVisible(true);
				//Registrerer apparatøvelse
				apparatOvelseReg.setOnAction((event1) -> {
					//Sjekker om noen av feltene er tomme	
					if(ovelseNavn.getText().isEmpty()) {
						ovelseFeedback.setText("Navnet kan ikke være tomt!");
					}
					else if(apparatAntallKilo.getText().isEmpty()) {
						ovelseFeedback.setText("Antall kilo kan ikke være tomt!");
					}
					else if(apparatAntallSett.getText().isEmpty()) {
						ovelseFeedback.setText("Antall sett kan ikke være tomt!");
					}
					else {
						try {
							//Splitter strengen som hentes ut fra databasen med "apparatNr.apparatNavn"
							//apparatNR hentes kun ut for at det skal bli riktig nr i tabellen apparatTilApparatØvelse
							dbhandler.registrerApparatOvelse(conn, apparatNr, ovelseNavn.getText(), Integer.valueOf(apparatAntallKilo.getText()), Integer.valueOf(apparatAntallSett.getText()));
							ovelseFeedback.setText(ovelseNavn.getText() + " er lagt til i databasen.");
							
							//----------------------------------------//
							//Gjør diverse endringer som å sette panel, knapper og label til usynlig.
							ovelseNavn.setText("");
							apparatAntallKilo.setText("");
							apparatAntallSett.setText("");
							apparatAntallKilo.setVisible(false);
							apparatAntallSett.setVisible(false);
							labelantkilo.setVisible(false);
							labelantsett.setVisible(false);
							apparatOvelseReg.setVisible(false);
				 			apparatOvelsePane.setVisible(false);
							friOvelseRB.setDisable(false);
							apparatOvelseRB.setSelected(false);
							//--------------------------------------//
							
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}	
				});	
			});
		}
		else if (friOvelseRB.isSelected()) {
			apparatOvelseRB.setDisable(true);
			friOvelsePane.setVisible(true);
			
			//registrer friøvelse
			friOvelseReg.setOnAction((event) -> {
				//Sjekker om noen av feltene er tomme
				if(ovelseNavn.getText().isEmpty()) {
					ovelseFeedback.setText("Navnet kan ikke være tomt!");
				}
				else if (friOvelseBeskrivelse.getText().isEmpty()) {
					ovelseFeedback.setText("Beskrivelsen kan ikke være tom!");
				}
				else {
					try {
						dbhandler.registrerFriOvelse(conn, ovelseNavn.getText(), friOvelseBeskrivelse.getText());
						ovelseFeedback.setText(ovelseNavn.getText() + " er lagt til i databasen.");
						
						//----------------------------------------//
						//Gjør diverse endringer som å sette panel, knapper og label til usynlig.
						ovelseNavn.setText("");
						friOvelseBeskrivelse.setText("");
						friOvelsePane.setVisible(false);
						apparatOvelseRB.setDisable(false);
						friOvelseRB.setSelected(false);
						//--------------------------------------//
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}	
			});	
		}	
	}
}
