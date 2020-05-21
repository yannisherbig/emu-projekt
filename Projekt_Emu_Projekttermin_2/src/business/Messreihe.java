package business;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Messreihe {
	
	private final SimpleIntegerProperty messreihenId;
	private final SimpleIntegerProperty zeitintervall;
	private final SimpleStringProperty verbraucher;
	private final SimpleStringProperty messgroesse;
	private Messung[] messungen; 
	private SimpleStringProperty messungenString;
	
	public Messreihe(int messreihenId, int zeitintervall,
		String verbraucher, String messgroesse) {
		super();
		this.messreihenId = new SimpleIntegerProperty(messreihenId);
		this.zeitintervall = new SimpleIntegerProperty(zeitintervall);
		this.verbraucher = new SimpleStringProperty(verbraucher);
		this.messgroesse = new SimpleStringProperty(messgroesse);
		this.messungenString = new SimpleStringProperty();
	}

	public int getMessreihenId() {
		return messreihenId.get();
	}
	
	public int getZeitintervall() {
		return zeitintervall.get();
	}
	
	public String getVerbraucher() {
		return verbraucher.get();
	}
	
	public String getMessgroesse() {
		return messgroesse.get(); 
	}
	
	public Messung[] getMessungen() {
		return messungen;
	}
	
	public void setMessungen(Messung[] messungen) {
		this.messungen = messungen;
		StringBuilder sb = new StringBuilder();
		for (Messung m : messungen) {
			sb.append(m.getWert() + " / ");
		}
		this.messungenString.set(sb.toString());
	}
	
	public String getMessungenString() {
		return this.messungenString.get();
	}
	
	public String gibAttributeAus(){
		return (this.messreihenId + " " 
		    + this.zeitintervall + " " + this.verbraucher + " " 
			+ this.messgroesse);
	}

}
