package athernet.tools;

import athernet.mac.*;
import athernet.nat.NatPacket;
import java.io.*;

class Client{
	static public void sendFileUsingUDP(String file_name) throws Exception{
		//Create object of FileReader
		FileReader input_file = new FileReader(file_name);

		//Instantiate the BufferedReader Class
		BufferedReader buffer_reader = new BufferedReader(input_file);

		//Variable to hold the one line data
		String line;
		
		NatPacket nat_packet;		
		int[] addr = {192, 168, 1, 2};

		MacLayer mac_layer = new MacLayer((byte) 0x1, (byte) 0x2);
		mac_layer.startMacLayer();

		// Read file line by line and print on the console
		while ((line = buffer_reader.readLine()) != null)   {
			nat_packet = new NatPacket(addr, 16384, line.getBytes());
			mac_layer.requestSend(nat_packet.toArray());
		}

		mac_layer.stopMacLayer();
		buffer_reader.close();		
	}

	static public void pingUsingICMP(int cnt) throws Exception{
		// Setting up Nat Packet.
		byte[] content = "Hello World.".getBytes();
		int[] addr = {192, 168, 1, 2};

		MacLayer mac_layer = new MacLayer((byte) 0x1, (byte) 0x2);
		NatPacket nat_packet = new NatPacket(addr, 0, content);
		mac_layer.startMacLayer();

		byte[] data = nat_packet.toArray();

		for (int i=0; i<cnt; i++){
			mac_layer.requestSend(data);
		}

		mac_layer.stopMacLayer();
	}
	static public void main(String[] args) throws Exception{
		if (args.length < 3){
			System.err.println("You have to provided args.");
		} else {
			if (args[0].equals("-f")){
				sendFileUsingUDP(args[1]);
			} else if (args[0].equals("ping")){
				pingUsingICMP(Integer.parseInt(args[1]));
			}
		} 
	}	
}