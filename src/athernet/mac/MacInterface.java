package athernet.mac;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import athernet.nat.NatPacket;
import java.io.*;
import java.util.Scanner;

class MacInterface{
	static public int getUnsignedByte(){
		Scanner in = new Scanner(System.in);
		return in.nextInt();
	}
	static public void receive(MacLayer mac_layer) throws Exception{
		int len = (getUnsignedByte() << 8) + getUnsignedByte();
		byte[] data = new byte[len];
		for (int i=0; i<len; i++){
			data[i] = (byte) getUnsignedByte();
		}
		mac_layer.requestSend(data);
	}

	static public void send(byte[] data) throws Exception{
		int len = data.length;

		System.out.print(((len & 0xff00) >> 8) + " ");
		System.out.print((len & 0x00ff) + " ");

		System.err.println();
		for (int i=0; i<data.length; i++){
			System.out.print((int) data[i] + " ");
		}
	}
	static public void main(String[] args) throws Exception{
		if (args.length < 1) {
			System.err.println("Please provide enough args.");
		}
		MacLayer mac_layer = new MacLayer((byte) 0x2, (byte) 0x1);
		mac_layer.startMacLayer();

		if (args[0].equals("toInternet")) {
			while (true){
				byte[] data = mac_layer.getOnePack().getData();
				send(data);
			}
		} else if (args[0].equals("toAthernet")){
			while (true){
				receive(mac_layer);
			}
		} else {
			System.err.println("Unrecognized argument.")
		}
		mac_layer.stopMacLayer();
	}
}