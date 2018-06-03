package athernet.mac;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import athernet.nat.NatPacket;
import java.io.*;
import java.util.Scanner;

class MacInterface{
	public static final Scanner in = new Scanner(System.in);
	static public int getUnsignedByte() throws Exception{
		while (!in.hasNextInt()) { Thread.sleep(1); }
		return in.nextInt();
	}
	static public void toAthernet(MacLayer mac_layer) throws Exception{
		int len = getUnsignedByte();
		byte[] data = new byte[len];
		for (int i=0; i<len; i++){
			data[i] = (byte) getUnsignedByte();
		}
		mac_layer.requestSend(data);
	}

	static public void toInternet(byte[] data) throws Exception{
		// Send the length first.
		int len = data.length;
		System.out.print(((len & 0xff00) >> 8) + " ");
		System.out.print((len & 0x00ff) + " ");

		for (int i=0; i<data.length; i++){
			System.out.print((int) data[i] + " ");
		}
	}
	static public void main(String[] args) throws Exception{
		if (args.length == 0) {
			System.err.println("Please provide enough args.");
		}
		MacLayer mac_layer = new MacLayer((byte) 0x2, (byte) 0x1);
		mac_layer.startMacLayer();

		if (args[0].equals("toInternet")) {
			for (int i=0; i<50; i++){
				byte[] data = mac_layer.getOnePack().getData();
				toInternet(data);
			}
		} else if (args[0].equals("toAthernet")){
			for (int i=0; i<50; i++){
				toAthernet(mac_layer);
			}
		} else {
			System.err.println("Unrecognized argument.");
		}
		mac_layer.stopMacLayer();
	}
}