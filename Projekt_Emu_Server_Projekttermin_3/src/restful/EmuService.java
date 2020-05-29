package restful;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import restful.db.DbAktionen;

@Path("/messungenService")
public class EmuService {

	@GET
	@Path("/messungen/{messreihenID}")
	@Produces(MediaType.APPLICATION_JSON)
	public String leseMessungen(@PathParam("messreihenID") String messreihenID) {
		Messung[] messungen;
		String result = "";
		try (DbAktionen dbAktionen = new DbAktionen()) {
			ObjectMapper obm = new ObjectMapper();
			messungen = dbAktionen.leseMessungen(Integer.parseInt(messreihenID));
			if (messungen.length != 0)
				result = obm.writeValueAsString(messungen);
		} 
		catch (NumberFormatException | SQLException | JsonProcessingException | ClassNotFoundException e) {
			result = e.getClass().getSimpleName() + ": " + e.getMessage();
		} 
		return result;
	}

	@GET
	@Path("/messreihen")
	@Produces(MediaType.APPLICATION_JSON)
	public String leseAlleMessreihen() {
		Messreihe[] messreihen;
		String result = null;
		try (DbAktionen dbAktionen = new DbAktionen()) {
			ObjectMapper obm = new ObjectMapper();
			messreihen = dbAktionen.leseMessreihenInklusiveMessungen();
			if (messreihen.length != 0)
				result = obm.writeValueAsString(messreihen);
		} 
		catch (NumberFormatException | SQLException | JsonProcessingException | ClassNotFoundException e) {
			result = e.getClass().getSimpleName() + ": " + e.getMessage();
		} 

		return result;
	}

	@HEAD
	@Path("/messreihe/{messreihenID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessreihe(@PathParam("messreihenID") String messreihenID) {
		Messreihe messreihe;
		String result = null;
		try (DbAktionen dbAktionen = new DbAktionen()) {
			messreihe = dbAktionen.leseMessreihe(Integer.parseInt(messreihenID));
			if (messreihe != null)
				return Response.status(200).entity(result).build();
		} 
		catch (NumberFormatException | SQLException | ClassNotFoundException e) {
			result = e.getClass().getSimpleName() + ": " + e.getMessage();
		} 
		
		return Response.status(404).entity(result).build();
	}
	
	@POST
	@Path("/messreihe")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response speichereMessreihe(String messreihenString) {
		Messreihe messreihe;
		String fehlermeldung = "";
		try (DbAktionen dbAktionen = new DbAktionen()) {
			System.out.println("im service");
			System.out.println(messreihenString);
			messreihe = new ObjectMapper().readValue(messreihenString, Messreihe.class);
			System.out.println(messreihe.gibAttributeAus());
			dbAktionen.fuegeMessreiheEin(messreihe);
		} 
		catch (IOException | SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			fehlermeldung = e.getClass().getSimpleName() + ":" + e.getMessage();
		}

		return Response.status(200).entity(fehlermeldung).build();
	}

	@POST
	@Path("/messung/{messreihenID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response speichereMessung(String messungString, @PathParam("messreihenID") int messreihenID) {
		Messung messung;
		String fehlermeldung = "";
		try (DbAktionen dbAktionen = new DbAktionen()){
			messung = new ObjectMapper().readValue(messungString, Messung.class);
			dbAktionen.fuegeMessungEin(messreihenID, messung);
		} 
		catch (IOException | SQLException | ClassNotFoundException e) {
			fehlermeldung = e.getClass().getSimpleName() + ":" + e.getMessage();
		}

		return Response.status(200).entity(fehlermeldung).build();
	}
	
}
