package business.emu;

import net.sf.yad2xx.FTDIInterface;

import net.sf.yad2xx.*;

public class EmuCheckConnection extends Thread {

	private Device device = null;
	// Attribut zur Regelung des Threads, siehe unten
	private boolean connected = true;

	private String ergebnis;
	private boolean ergSchreiben = false;

	public double gibErgebnisAus() {
		System.out.println("ergebnis: " + ergebnis);
		if (ergebnis.contains("*")) {
			int a = ergebnis.indexOf("(");
			int m = ergebnis.indexOf("*");
			ergebnis = ergebnis.substring(a + 1, m);
		}
		return Double.parseDouble(ergebnis);
	}

	public EmuCheckConnection() throws FTDIException {
		Device[] devices = FTDIInterface.getDevices();
		System.out.println("Anzahl gefundener FTD2 Devices: " + devices.length);
		for (Device device : devices) {
			if (device.getDescription().startsWith("NZR")) {
				this.device = device;
			}
		}
		if (this.device == null) 
			throw new RuntimeException("NZR-Device nicht gefunden: Überprüfen Sie, ob das Gerät angeschlossen ist");
		this.device.open();
		this.device.setDataCharacteristics((byte) 7, (byte) 1, (byte) 2);
		this.device.setBaudRate(300);
		System.out.println("Verbunden mit Device: " + device.getDescription());
		this.start();
	}

	public void connect() throws FTDIException {
		byte[] start = new byte[] { 0x2F, 0x3F, 0x21, 0x0D, 0x0A };
		this.device.write(start);
		System.out.println("Einleitung Kommunikation USBCheck");
	}

	public void disconnect() throws FTDIException {
		this.connected = false;
		byte[] ende = new byte[] { 0x01, 0x42, 0x30, 0x03 };
		this.device.write(ende);
		this.device.close();
		System.out.println("Ende Kommunikation USBCheck");
	}

	public void sendProgrammingMode() throws FTDIException {
		byte[] nachricht = new byte[] { 0x06, 0x30, 0x30, 0x31, 0x0D, 0x0A };
		this.device.write(nachricht);
		System.out.println("Programming Mode");
	}

	public void sendRequest(MESSWERT m) throws FTDIException {
		this.device.write(m.getRequest());
		ergSchreiben = true;
		System.out.println("Request " + m.getObis() + " " + m.toString());
	}

	@Override
	public void run() {
		int intZahl;
		byte[] byteArray = new byte[1];
		while (connected) {
			try {
				intZahl = device.getQueueStatus();
				if (intZahl != 0) {
					intZahl = device.read(byteArray);
					System.out.print((char) byteArray[0]);
					if (ergSchreiben) {
						ergebnis = ergebnis + (char) byteArray[0];
					}
				}
			} catch (FTDIException e) {
				e.printStackTrace();
			}
			try {
				sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}