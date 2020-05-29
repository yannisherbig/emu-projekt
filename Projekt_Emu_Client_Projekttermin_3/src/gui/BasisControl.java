package gui;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;

import business.BasisModel;
import business.Messreihe;
import business.Messung;
import business.emu.EmuCheckConnection;
import business.emu.MESSWERT;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import net.sf.yad2xx.FTDIException;
 
public class BasisControl implements Initializable {

	private BasisModel basisModel;
	private boolean messungGestopped;
	
	@FXML private Button btnMessreiheStarten;
	@FXML private Button btnMessreiheStoppen;

	@FXML 
	private TableView<Messreihe> tblMessreihen;
	@FXML private TableColumn<Messreihe, Long> clmnMessreihenId;
	@FXML private TableColumn<Messreihe, Integer> clmnZeitintervall;
	@FXML private TableColumn<Messreihe, String> clmnVerbraucher;
	@FXML private TableColumn<Messreihe, String> clmnMessgroesse;
	@FXML private TableColumn<Messreihe, String> clmnMessungen;

	@FXML private TextField txtMessreihenId;
	@FXML private TextField txtZeitintervall;
	@FXML private TextField txtVerbraucher;
	@FXML private TextField txtMessgroesse;

	
	public BasisControl() {
		this.basisModel = BasisModel.getInstance();
	}
	
	public void setStageAndSetupListeners(Stage primaryStage, Parent root) {
		Scene scene = new Scene(root);
	    primaryStage.setTitle("EMU-Anwendung");
		primaryStage.setScene(scene);
		primaryStage.show();  // Anzeigen der Application
	}
 	  
	public Messung holeMessungVonEMU(String messreihenId, String laufendeNummer) 
			throws FTDIException, InterruptedException {
 		Messung ergebnis = null;
 		int messId = -1;
		messId = Integer.parseInt(messreihenId);
		int lfdNr = Integer.parseInt(laufendeNummer);
		EmuCheckConnection conn = new EmuCheckConnection();
 		conn.connect();
 		Thread.sleep(2000);
 		conn.sendProgrammingMode();
 		Thread.sleep(2000);
 		conn.sendRequest(MESSWERT.Leistung);
 		Thread.sleep(2000);
 		ergebnis = new Messung(lfdNr, conn.gibErgebnisAus());
 		conn.disconnect();
 		this.speichereMessungInDB(messId, ergebnis);
 		return ergebnis;
 	}
	
  	private void speichereMessungInDB(int messreihenId, Messung messung){
  		try {
			this.basisModel.speichereMessungInDB(messreihenId, messung);
		} catch (ClassNotFoundException | JsonProcessingException | SQLException e) {
			zeigeFehlermeldungAn(e.getMessage());
		}
	}

	public void starteMessdurchlauf(final int messreihenId, MESSWERT messwert, int zeitlicheAbstandinMS) 
			throws InterruptedException, FTDIException, NumberFormatException, RuntimeException {
		this.messungGestopped = false;
		System.out.println("zeitlicherAbstandInMS: " + zeitlicheAbstandinMS);
		AtomicInteger lfdNr = new AtomicInteger(0);
		EmuCheckConnection conn = new EmuCheckConnection();
 		conn.connect();
 		Thread.sleep(2000);
 		conn.sendProgrammingMode();
 		Thread.sleep(2000);
		new Thread(() -> {
			while(!messungGestopped) {
				try {
			 		conn.sendRequest(messwert);
			 		Thread.sleep(zeitlicheAbstandinMS);
			 		double messungErgebnis = conn.gibErgebnisAus();
			 		System.out.println("messungErgebnis: " + messungErgebnis);
			 		Messung ergebnis = new Messung(lfdNr.get(), messungErgebnis);
			 		this.speichereMessungInDB(messreihenId, ergebnis);
			 		lfdNr.getAndIncrement();
				} 
				catch (InterruptedException | FTDIException e) {
					stoppeMessdurchlauf();
					zeigeFehlermeldungAn(e.getMessage());
					System.out.println(e.getMessage());
				}
			}
		}).start();
	}

    public void speichereMessreiheInDB() throws ClassNotFoundException, SQLException {
    	try {
    		long messreihenID = Long.parseLong(txtMessreihenId.getText());
			int zeitintervall = Integer.parseInt(txtZeitintervall.getText());
			String messgroesse = txtMessgroesse.getText();
			String verbraucher = txtVerbraucher.getText();
			MESSWERT m = getMesswertEnumValIfExists(messgroesse);
			
			if (m == null) 
				throw new IllegalArgumentException("Ungültige Messgröße: " + messgroesse);
			
			if (this.basisModel.existiertMessreihe((int) messreihenID)) 
				throw new IllegalArgumentException("Die angegebene Messreihen-ID ist bereits belegt");
			
			if (messreihenID < 0 || messgroesse.equals("") || verbraucher.equals("") 
					|| zeitintervall < 0 || zeitintervall > 36000)
				throw new IllegalArgumentException("Ungültige Eingaben");
			
			Messreihe neueMessreihe = new Messreihe((int) messreihenID, zeitintervall, 
					verbraucher, messgroesse);
			this.basisModel.speichereMessreiheInDB(neueMessreihe);
		} 
    	catch (Exception e) {
			Alert alert = new Alert(AlertType.WARNING, 
                    "Mieter-Erstellung fehlgeschlagen: " + e.getMessage(), 
                    ButtonType.OK);
			alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
			alert.show();
		}
	} 
	
    public void stoppeMessreihe() {
    	stoppeMessdurchlauf();
    }
    
    public void starteMessreihe() {
    	btnMessreiheStoppen.setDisable(false);
		btnMessreiheStarten.setDisable(true);
		
		Messreihe messreihe = (Messreihe) this.tblMessreihen.getSelectionModel().getSelectedItem();
		if (messreihe != null) {
			if (messreihe.getMessungen().length == 0){
				try {
					MESSWERT m = getMesswertEnumValIfExists(messreihe.getMessgroesse());
					if (m == null) throw new IllegalArgumentException("Ungültige Messgröße: " + messreihe.getMessgroesse());
					starteMessdurchlauf(messreihe.getMessreihenId(), m, 
							messreihe.getZeitintervall() * 1000);
				} catch (InterruptedException | FTDIException | IllegalArgumentException e) {
					stoppeMessdurchlauf();
					zeigeFehlermeldungAn(e.getMessage());
					System.out.println(e.getMessage());
				}
				catch (RuntimeException e) {
					stoppeMessdurchlauf();
					zeigeFehlermeldungAn(e.getMessage());
					System.out.println(e.getMessage());
				}
			}
			else {
				zeigeFehlermeldungAn("Zur ausgewählten Messreihe existieren bereits Messungen");
				stoppeMessdurchlauf();
			}
		}
		else {
			zeigeFehlermeldungAn("Es wurde keine Messreihe ausgewählt");
			stoppeMessdurchlauf();
		}	
    }
    
    public void leseMessreihenInklusiveMessungenAusDB() {
		try {
			this.basisModel.leseMessreihenInklusiveMessungenAusDB();
			ObservableList<Messreihe> ausgeleseneMessreihen = this.basisModel.getMessreihen();
	    	tblMessreihen.setItems(ausgeleseneMessreihen);
		} catch (ClassNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			zeigeFehlermeldungAn(e.getMessage());
		}
    }
    
	public void stoppeMessdurchlauf() {
		this.messungGestopped = true;
		btnMessreiheStoppen.setDisable(true);
		btnMessreiheStarten.setDisable(false);
	} 
	
	void zeigeFehlermeldungAn(String meldung) {
	   	Alert alert = new Alert(AlertType.ERROR);
	    alert.setTitle("Fehlermeldung");
	    alert.setContentText(meldung);
	    alert.showAndWait();
	}

	private void initTable() {	
		clmnMessreihenId.setCellValueFactory(new PropertyValueFactory<Messreihe, Long>("messreihenId"));
		clmnZeitintervall.setCellValueFactory(new PropertyValueFactory<Messreihe, Integer>("zeitintervall"));
		clmnVerbraucher.setCellValueFactory(new PropertyValueFactory<Messreihe, String>("verbraucher"));
		clmnMessgroesse.setCellValueFactory(new PropertyValueFactory<Messreihe, String>("messgroesse"));
		clmnMessungen.setCellValueFactory(new PropertyValueFactory<Messreihe, String>("messungenString"));
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initTable();
	}

	private MESSWERT getMesswertEnumValIfExists(String messgroesse) {	
		MESSWERT messwert = null;
		switch (messgroesse) {
			case "Leistung":			
				messwert = MESSWERT.Leistung;
				break;			
			case "Arbeit":
				messwert = MESSWERT.Arbeit;
				break;				
			case "Strom":
				messwert = MESSWERT.Strom;
				break;		
			case "Spannung":
				messwert = MESSWERT.Spannung;
				break;
			default:
				messwert = null;
				break;
		}
		
		return messwert;
	}
	
}
