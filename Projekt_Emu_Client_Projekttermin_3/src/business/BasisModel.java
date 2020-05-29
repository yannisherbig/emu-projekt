package business;

import javafx.collections.*;
import java.io.*;
import java.sql.*;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


public final class BasisModel {
	
	private static BasisModel basisModel;
	
	public static BasisModel getInstance() {
		if (basisModel == null) {
			basisModel = new BasisModel();
		}
		return basisModel;
	}
	
	private BasisModel(){ }

	private ObservableList<Messreihe> messreihen;
	
	public ObservableList<Messreihe> getMessreihen() {
		return messreihen;
	}
	
	public void speichereMessungInDB(int messreihenId, Messung messung)
		throws ClassNotFoundException, SQLException, JsonProcessingException {
		String messungStringJSON = "";
		ObjectMapper mapper = new ObjectMapper();
		
		messungStringJSON = mapper.writeValueAsString(messung);
		System.out.println(messungStringJSON);
		
        WebResource wrs = Client.create().resource(
        		"http://localhost:8080/Projekt_Emu_Server_Projekttermin_3/rest/messungenService/messung/"
        		+ messreihenId);
        wrs.type(MediaType.APPLICATION_JSON).post(messungStringJSON);
	} 
	
	public void leseMessreihenInklusiveMessungenAusDB()
		throws ClassNotFoundException, SQLException, JsonParseException, JsonMappingException, IOException {
		Messreihe[] alleMessreihen = null;
	 
	   	WebResource wrs = Client.create().resource(
	   			"http://localhost:8080/Projekt_Emu_Server_Projekttermin_3/rest/messungenService/messreihen");
	    String responseJSON = wrs.accept(MediaType.APPLICATION_JSON).get(String.class);
	   
   		ObjectMapper om = new ObjectMapper();
   		alleMessreihen = om.readValue(responseJSON, Messreihe[].class);
	        
   		if (this.messreihen != null)
   			this.messreihen.clear();
   		
   		this.messreihen = FXCollections.observableArrayList(alleMessreihen);  			
	}
		  
	public boolean existiertMessreihe(int messreihenID) throws JsonParseException, JsonMappingException, IOException {	
	   	WebResource wrs = Client.create().resource(
	   			"http://localhost:8080/Projekt_Emu_Server_Projekttermin_3/rest/messungenService/messreihe/" 
	   					+ messreihenID);
   		ClientResponse response = wrs.head();
   		
   	    if (response.getStatus() == 200)
   	    	return true;
   	    
   	    return false;
	}
	
	public void speichereMessreiheInDB(Messreihe messreihe)
	  	throws ClassNotFoundException, SQLException, JsonProcessingException {
		String messungenStringJSON = "";
		ObjectMapper mapper = new ObjectMapper();
		
		messungenStringJSON = mapper.writeValueAsString(messreihe);
		System.out.println(messungenStringJSON);
		
        WebResource wrs = Client.create().resource(
        		"http://localhost:8080/Projekt_Emu_Server_Projekttermin_3/rest/messungenService/messreihe");
        wrs.type(MediaType.APPLICATION_JSON).post(messungenStringJSON);
	} 
	
 }
