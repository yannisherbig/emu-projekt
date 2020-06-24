package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import business.Messreihe;


/*
 * Wertebereiche:
 * messreihenId: {...,-3,-2,-1,0} {1,2,3,4...}
 * zeitintervall: {...,12,13,14} {15,16,17,...}
 * verbraucher: {Menge aller Strings \ {��, null}} {��, null}
 * messgroesse: {�Arbeit�, �Leistung�} {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 
 * 
 * vollst�ndige Partionierung (Anzahl der �quivalenzklassen; 2^4):
 * 		partielle Partionierung (erzielt g�ltiges Ergebnis - Messreihe-Konstruktor wird erfolgreich initialisiert):
 * 			{1,2,3,4...} x {15,16,17,...} x {Menge aller Strings \ {��, null}} x {�Arbeit�, �Leistung�}
 * 
 * 	{1,2,3,4...} x {15,16,17,...} x {��, null} x {�Arbeit�, �Leistung�}
 * 	{1,2,3,4...} x {15,16,17,...} x {Menge aller Strings \ {��, null}} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 	{1,2,3,4...} x {15,16,17,...} x {��, null} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 
 *  {1,2,3,4...} x {...,12,13,14} x {Menge aller Strings \ {��, null}} x {�Arbeit�, �Leistung�}
 * 	{1,2,3,4...} x {...,12,13,14} x {��, null} x {�Arbeit�, �Leistung�}
 * 	{1,2,3,4...} x {...,12,13,14} x {Menge aller Strings \ {��, null}} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 	{1,2,3,4...} x {...,12,13,14} x {��, null} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 
 *  {...,-3,-2,-1,0} x {15,16,17,...} x {��, null} x {�Arbeit�, �Leistung�}
 * 	{...,-3,-2,-1,0} x {15,16,17,...} x Menge aller Strings \ {��, null} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 	{...,-3,-2,-1,0} x {15,16,17,...} x {��, null} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 *  {...,-3,-2,-1,0} x {15,16,17,...} x {Menge aller Strings \ {��, null}} x {�Arbeit�, �Leistung�}
 *  
 *  {...,-3,-2,-1,0} x {...,12,13,14} x {��, null} x {�Arbeit�, �Leistung�}
 * 	{...,-3,-2,-1,0} x {...,12,13,14} x Menge aller Strings \ {��, null} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 	{...,-3,-2,-1,0} x {...,12,13,14} x {��, null} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 *  {...,-3,-2,-1,0} x {...,12,13,14} x {Menge aller Strings \ {��, null}} x {�Arbeit�, �Leistung�}
 *  
 *  
 *  Verschmelzen:
 *  
 *  {1,2,3,4...} x {15,16,17,...} x {Menge aller Strings \ {��, null}} x {�Arbeit�, �Leistung�}
 * 		--> ein repr�sentativer Testfall (g�ltig): (2,16,"LCD","Arbeit")
 * 
 * 	{...,-3,-2,-1,0,1,2,3,4...} x {...,12,13,14,15,16,17,...} x {{{Menge aller Strings \ {��, null}}, {��, null}} x {Menge aller Strings \ {�Arbeit�, �Leistung�}}
 * 		--> ein repr�sentativer Testfall (ung�ltig): (2,16,"LCD","Luftdruck")
 * 
 *  {...,-3,-2,-1,0,1,2,3,4...} x {...,12,13,14,15,16,17,...} x {��, null} x {{Menge aller Strings \ {�Arbeit�, �Leistung�}}, {�Arbeit�, �Leistung�}}
 *  	--> ein repr�sentativer Testfall (ung�ltig): (2,16,"LCD","")
 *  
 *  {...,-3,-2,-1,0,1,2,3,4...} x {...,12,13,14} x {{{Menge aller Strings \ {��, null}}, {��, null}} x {{�Arbeit�, �Leistung�}, {Menge aller Strings \ {�Arbeit�, �Leistung�}}}
 *  	--> ein repr�sentativer Testfall (ung�ltig): (2,13,"LCD","Arbeit")
 *  
 *  {...,-3,-2,-1,0} x {...,12,13,14,15,16,17,...} x {{{Menge aller Strings \ {��, null}}, {��, null}} x {{�Arbeit�, �Leistung�}, {Menge aller Strings \ {�Arbeit�, �Leistung�}}}
 *  	--> ein repr�sentativer Testfall (ung�ltig): (-2,16,"LCD","Arbeit")
 *  
 *  
 */	
public class MessreiheTest {

	@ParameterizedTest
	@ValueSource(strings = { "Leistung", "Arbeit" })
	void constructorTest(String messgroesse) {
		Messreihe messreihe = new Messreihe(1, 20, "LED", messgroesse);
		assertTrue(messreihe.getMessreihenId() == 1);
		assertEquals(20, messreihe.getZeitintervall());
		assertEquals("LED", messreihe.getVerbraucher());
		assertEquals(messgroesse, messreihe.getMessgroesse());
	}
	
	@Test
	void constructorExceptionTest() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> 
			new Messreihe(-2, 16, "LCD", "Arbeit"));
		assertEquals("Only positive ID numbers are allowed!", exception.getMessage());
	}
	
}
