package gui;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import business.BasisModel;
import business.Messung;
import business.emu.EmuCheckConnection;
import business.emu.MESSWERT;
import javafx.stage.Stage;
import net.sf.yad2xx.FTDIException;

public class BasisControl {

	private BasisModel basisModel;
	private BasisView basisView;
	private boolean messungGestopped;
	
	public BasisControl(Stage primaryStage){
		this.basisModel = BasisModel.getInstance();
		this.basisView = new BasisView(this, primaryStage, this.basisModel);
		primaryStage.show();
	}
		
	public Messung[] leseMessungenAusDb(String messreihenId){
		Messung[] ergebnis = null;
		int idMessreihe = -1;
		try{
 			idMessreihe = Integer.parseInt(messreihenId);
 		}
 		catch(NumberFormatException nfExc){
 			basisView.zeigeFehlermeldungAn( 
 				"Das Format der eingegebenen MessreihenId ist nicht korrekt.");
 		}
 		try{
 			ergebnis = this.basisModel.leseMessungenAusDb(idMessreihe);
 		}
 		catch(ClassNotFoundException cnfExc){
 			basisView.zeigeFehlermeldungAn( 
 				"Fehler bei der Verbindungerstellung zur Datenbank.");
 		}
 		catch(SQLException sqlExc){
 			basisView.zeigeFehlermeldungAn( 
 				"Fehler beim Zugriff auf die Datenbank.");
 			sqlExc.printStackTrace();
 		}
 		return ergebnis;
	} 
 	  
	public Messung holeMessungVonEMU(String messreihenId, String laufendeNummer) throws FTDIException, InterruptedException{
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
 		this.speichereMessungInDb(messId, ergebnis);
 		return ergebnis;
 	}
	
  	private void speichereMessungInDb(int messreihenId, Messung messung){
 		try{
 			this.basisModel.speichereMessungInDb(messreihenId, messung);
 		}
 		catch(ClassNotFoundException cnfExc){
 			basisView.zeigeFehlermeldungAn( 
 				"Fehler bei der Verbindungerstellung zur Datenbank.");
 		}
 		catch(SQLException sqlExc){
 			basisView.zeigeFehlermeldungAn( 
 				"Fehler beim Zugriff auf die Datenbank.");
 		}
	}

	public void starteMessdurchlauf(String messreihenId) throws InterruptedException, FTDIException, NumberFormatException {
		this.messungGestopped = false;
		AtomicInteger lfdNr = new AtomicInteger(0);
 		final int messId = Integer.parseInt(messreihenId);
		EmuCheckConnection conn = new EmuCheckConnection();
 		conn.connect();
 		Thread.sleep(2000);
 		conn.sendProgrammingMode();
 		Thread.sleep(2000);
		new Thread(() -> {
			while(!messungGestopped) {
				try {
			 		conn.sendRequest(MESSWERT.Leistung);
			 		Thread.sleep(2000);
			 		double messungErgebnis = conn.gibErgebnisAus();
			 		System.out.println("messungErgebnis: " + messungErgebnis);
			 		Messung ergebnis = new Messung(lfdNr.get(), messungErgebnis);
			 		this.speichereMessungInDb(messId, ergebnis);
			 		lfdNr.getAndIncrement();
				} catch (InterruptedException | FTDIException e) {
					System.out.println(e.getMessage());
				}
			}
		}).start();
	}

	public void stoppeMessdurchlauf() {
		this.messungGestopped = true;
	} 

}
