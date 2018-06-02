package athernet.tools;

import athernet.mac.*;
import athernet.nat.NatPacket;
import java.io.*;
import java.net.InetAddress;

class Client{
	static public void sendFileUsingUDP(
	  String file_name, int[] addr, MacLayer mac_layer) throws Exception{
		//Create object of FileReader
		FileReader input_file = new FileReader(file_name);

		//Instantiate the BufferedReader Class
		BufferedReader buffer_reader = new BufferedReader(input_file);

		//Variable to hold the one line data
		String line;
		
		NatPacket nat_packet;		

		// Read file line by line and print on the console
		while ((line = buffer_reader.readLine()) != null)   {
			nat_packet = new NatPacket(addr, 16384, line.getBytes());
			mac_layer.requestSend(nat_packet.toArray());
		}

		mac_layer.stopMacLayer();
	}

	static public void pingUsingICMP(
	  int cnt, int[] addr, MacLayer mac_layer) throws Exception{
		// Setting up Nat Packet.
		byte[] content = "Hello World.".getBytes();

		NatPacket nat_packet = new NatPacket(addr, 0, content);
		
		byte[] data = nat_packet.toArray();

		for (int i=0; i<cnt; i++){
			mac_layer.requestSend(data);
		}
	}

	static public void main(String[] args) throws Exception{
		if (args.length < 3){
			System.err.println("You have to provided args.");
		}
		// Convert from string address to byte array.
		byte[] byte_addr = InetAddress.getByName(args[2]).getAddress();
		// Manually convert from byte to int.
		int[] int_addr = {byte_addr[0], byte_addr[1], byte_addr[2], byte_addr[3]};

		MacLayer mac_layer = new MacLayer((byte) 0x1, (byte) 0x2);
		mac_layer.startMacLayer();

		if (args[0].equals("file")){
			sendFileUsingUDP(args[1], int_addr, mac_layer);
		} else if (args[0].equals("ping")){
			pingUsingICMP(Integer.parseInt(args[1]), int_addr, mac_layer);
		}

		mac_layer.stopMacLayer();
	}	
}