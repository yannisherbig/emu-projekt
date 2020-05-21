package business;

import javafx.collections.*;
import business.db.DbAktionen;
import java.io.*;
import java.sql.*;
import java.util.concurrent.TimeUnit;

public final class BasisModel {
	
	private static BasisModel basisModel;
	
	public static BasisModel getInstance(){
		if (basisModel == null){
			basisModel = new BasisModel();
		}
		return basisModel;
	}
	
	private BasisModel(){		
	}
	
	private DbAktionen dbAktionen = new DbAktionen();
	
	// wird zukuenftig noch instanziiert
	private ObservableList<Messreihe> messreihen = FXCollections.observableArrayList(); 
	
	public ObservableList<Messreihe> getMessreihen() {
		return messreihen;
	}
	
	public void speichereMessungInDB(int messreihenId, Messung messung)
		throws ClassNotFoundException, SQLException{
		this.dbAktionen.connectDB();
		this.dbAktionen.fuegeMessungEin(messreihenId, messung);
		this.dbAktionen.closeDb();
	} 
	
	public void leseMessreihenInklusiveMessungenAusDB()
		throws ClassNotFoundException, SQLException{
		this.dbAktionen.connectDB();
		Messreihe[] messreihenAusDb 
		    = this.dbAktionen.leseMessreihenInklusiveMessungen(); 
		this.dbAktionen.closeDb();
		int anzahl = this.messreihen.size();
		for(int i = 0; i < anzahl; i++){
		    this.messreihen.remove(0);
		}
		for(int i = 0; i < messreihenAusDb.length; i++){
			this.messreihen.add(messreihenAusDb[i]);
		} 
	}
		  
	public void speichereMessreiheInDB(Messreihe messreihe)
	  	throws ClassNotFoundException, SQLException{
	  	this.dbAktionen.connectDB();
	  	this.dbAktionen.fuegeMessreiheEin(messreihe);
	  	this.dbAktionen.closeDb();
	  	this.messreihen.add(messreihe);
	} 
	
  	public String getDaten(){
    	return "in getDaten";
	} 
 }
