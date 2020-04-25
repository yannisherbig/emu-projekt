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
		device.write(m.getRequest());
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

//Übersicht über die Klassen aus net.sf.yad2xx.*
//Klasse FTDIInterface
///**
// * A Java Native Interface (JNI) wrapper that adapts the FTDI
// * D2XX library to a more OO based approach. This Singleton type
// * provides a very thin Java layer over the top of the native C
// * language code and is central to the libraries operation.
// */
//Initialisierungsblock
///**
// * Loads the native library on first class usage. Library location
// * is JVM/platform dependent.
// */
//static {
//	System.loadLibrary("FTDIInterface");
//}
//
//Methode
///**
// * Returns an array of Devices which describe attached D2XX devices.
// * Combines calls to FT_CreateDeviceInfoList and  
// * FT_GetDeviceInfoList. <p>
// * Copies values returned from FT_GetDeviceInfoList into individual 
// * Device objects.
// *
// * @return array of Devices describing attached D2XX devices
// *
// * @throws	FTDIException	FT_CreateDeviceInfoList returned a non-
// * zero
// */
// public static native Device[] getDevices() throws FTDIException;
//
//Klasse Device
//Methoden
///**
// * Return the device description.
// *
// * @return	device description 
// */
//public String getDescription() {
//	return description;
//}
//
///**
// * Begin a session with the device.
// *  
// * @throws	IllegalStateException	if device already open
// * @throws	FTDIException D2XX API call failed
// */
//public void open() throws FTDIException {
//	if (isOpen())
//		throw new IllegalStateException("Device in use");
//		FTDIInterface.open(this);
//}
//
///**
// * Close the device.
// * 
// * @throws	FTDIException	D2XX API call failed, 
// */
//public void close() throws FTDIException {
//	if (ftHandle != 0) {
//		FTDIInterface.close(this);
//	}
//}
///**
// * Sets the data characteristics for the device.
// * 
// * @param	wordLength number of bits per word, must be 8 or 7
// * @param	stopBits number of stop bits, must be 1 or 2 
// * @param	parity must be one of the parity values e.g. 0 or 1 or 2
// * 					
// * @throws	FTDIException	D2XX API call failed, 
// */
//public void setDataCharacteristics(byte wordLength, byte stopBits,
// 	byte parity) throws FTDIException {
//	FTDIInterface.setDataCharacteristics(ftHandle, wordLength, 
// 	stopBits, parity);
//	}
///**
// * Sets the baud rate for the device.
// * 
// * @param	baudRate		baud rate
// * @throws	FTDIException	D2XX API call failed
// */
//public void setBaudRate(int baudRate) throws FTDIException {
//	FTDIInterface.setBaudRate(ftHandle, baudRate);
//}
///**
// * Write data to the device.
// * 
// * @param	buffer bytes to write to device
// *
// * @return	number of bytes actually written
// *
// * @throws	FTDIException	D2XX API call failed
// */
//public int write(byte[] buffer) throws FTDIException {
//	return write(buffer, buffer.length);
//}
//
///**
// * Returns number of bytes in receive queue.
// * 
// * @return number of bytes in receive queue
// *
// * @throws	FTDIException 	D2XX API call failed
// */
//public int getQueueStatus() throws FTDIException {
//	return FTDIInterface.getQueueStatus(ftHandle);
//}
//
///**
// * Reads data from device up to the size of the buffer. Note that 
// * this call will block if the requested number of bytes is not
// * immediately available. Call getQueueStatus to get the number of
// * bytes actually available to avoid blocking. 
// * 
// * @param	buffer bytes read from device. Buffer
// * length determines maximum number of bytes read
// *
// * @return number of bytes actually read
// * @throws	FTDIException	D2XX API call failed
// */
//public int read(byte[] buffer) throws FTDIException {
//	return FTDIInterface.read(ftHandle, buffer, buffer.length);
//}
