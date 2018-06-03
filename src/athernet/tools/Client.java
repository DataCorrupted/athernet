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
		Thread.sleep(10000);
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

	static public void pingWaitReply(int cnt, int[] addr, MacLayer mac_layer) throws Exception{
		for (int i=0; i<cnt; i++){
			byte[] data = mac_layer.getOnePack().getData();
			NatPacket nat_packet = new NatPacket(data);

			byte[] content = nat_packet.getContent();
			
			// Get the following from the content. 
			long send_time = 0;

			long recv_time = System.nanoTime();
			System.err.println(
				"Ping to " + 
				addr[0] + "." + addr[1] + "." + addr[2] + "." + addr[3] +
				" time=" + recv_time - send_times
			); 
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
			// Split receive thread and ping thread.
			int cnt = Integer.parseInt(args[1])
			ping_thread = new Thread(new Runnable(){
				public void run(){
					System.err.println("Ping request thread started.");
					try { pingUsingICMP(cnt, int_addr, mac_layer); }
					catch (Exception e) {;}
					System.err.println("Ping request thread stopped.");
				}
			});
			recv_thread = new Thread(new Runnable(){
				public void run() { 
					System.err.println("Ping receive thread started.");
					try { pingWaitReply(cnt, int_addr, mac_layer); } 
					catch (Exception e){;} 
					System.err.println("Ping receive thread stopped.");
				}
			});

			recv_thread.start();
			ping_thread.start();

			ping_thread.join();
			recv_thread.join();
		}

		mac_layer.stopMacLayer();
	}	
}