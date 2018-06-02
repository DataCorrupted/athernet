package athernet.mac;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import athernet.nat.NatPacket;
import java.io.*;

class MacInterface{
	public void receive(MacLayer mac_layer) throws Exception{
		byte c = (byte) System.in.read();
		int len = c << 8 + (int) (System.in.read() & 0xff);
		byte[] data = new byte[len];
		for (int i=0; i<len; i++){
			data[i] = (byte) (System.in.read() & 0xff);
		}
		mac_layer.requestSend(data);
	}

	static public void send(byte[] data) throws Exception{
		int len = data.length;

		System.out.print((char) ((len & 0xff00) >> 8));
		System.out.print((char) (len & 0x00ff));

		for (int i=0; i<data.length; i++){
			System.out.print((char) data[i]);
		}
	}
	static public void main(String[] args) throws Exception{
		MacLayer mac_layer = new MacLayer((byte) 0x2, (byte) 0x1);
		mac_layer.startMacLayer();

		for (int i=0; i<50; i++){
			byte[] data = mac_layer.getOnePack().getData();
			send(data);
		}

		mac_layer.stopMacLayer();
	}
}