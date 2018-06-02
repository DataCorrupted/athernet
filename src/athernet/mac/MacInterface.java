package athernet.mac;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import athernet.nat.NatPacket;
import java.io.*;

class MacInterface{
	private byte src_addr_;
	private byte dst_addr_;
	private MacLayer mac_layer_;

	public MacInterface(byte src, byte dst) throws Exception{
		src_addr_ = src;
		dst_addr_ = dst;
		mac_layer_ = new MacLayer(src_addr_, dst_addr_);
	} 
	public void send() throws Exception{
		byte c = (byte) System.in.read();
		int len = c << 8 + (int) (System.in.read() & 0xff);
		byte[] data = new byte[len];
		for (int i=0; i<len; i++){
			data[i] = (byte) (System.in.read() & 0xff);
		}
		mac_layer_.requestSend(data);
	}

	public void receive() throws Exception{
		MacPacket mac_pack = mac_layer_.getOnePack();
		byte[] data = mac_pack.getData();
		int len = data.length;

		System.out.print((char) ((len & 0xff00) >> 8));
		System.out.print((char) (len & 0x00ff));

		for (int i=0; i<data.length; i++){
			System.out.print((char) data[i]);
		}
	}
	static public void main(String[] args) throws Exception{
		if (args.length == 0){
			System.err.println("No input. You have to give me something!");
		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			char c = (char) System.in.read();
			System.err.println("I am Java, I received a str from cpp:\n  " + (int)c);
			for (int i=0; i<args.length; i++){
				System.out.print(((char) i));
			}
			System.out.println();
		}
	}
}