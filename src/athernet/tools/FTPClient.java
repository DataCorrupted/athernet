package athernet.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import athernet.mac.*;
import athernet.nat.NatPacket;
import java.io.*;
import java.net.InetAddress;

class FTPClient{
	int[] server_addr_;
	MacLayer mac_layer_;
	final Scanner scanner_ = new Scanner(System.in);
	
	public FTPClient(int[] server_addr, MacLayer mac_layer) throws Exception{
		mac_layer_ = mac_layer;
		server_addr_ = server_addr;
	}

	public void sendCommand(String cmd) throws Exception{
		NatPacket nat_packet;
		nat_packet = new NatPacket(server_addr_, 21, cmd.getBytes());
		mac_layer_.requestSend(nat_packet.toArray());
		Thread.sleep(100);		
	}

	public String getCommand() throws Exception{
		String cmd = scanner_.nextLine();
		System.out.println("The command you input is: " + cmd);
		return cmd;
	}

	public String getResult() throws Exception{
		MacPacket mac_pack = mac_layer_.getOnePack();
		return new String(mac_pack.getData());
	}

	static public void main(String[] args) throws Exception{
		if (args.length < 1){
			System.err.println("You have to provided args.");
			return;
		}
		// Convert from string address to byte array.
		byte[] byte_addr = InetAddress.getByName(args[0]).getAddress();
		// Manually convert from byte to int.
		int[] int_addr = {byte_addr[0], byte_addr[1], byte_addr[2], byte_addr[3]};
		MacLayer mac_layer = new MacLayer((byte) 0x1, (byte) 0x2);

		FTPClient ftp_client = new FTPClient(int_addr, mac_layer);

		Thread result_thread = new Thread(new Runnable(){
			public void run(){
				// List<String> data_buffer = new ArrayList<String>();
				// List<String> data_offset = new ArrayList<String>();
				try{ for (int i=0; i<1000; i++){
					String ret = ftp_client.getResult();
					// handle data segement (need to asseble the segaments)
					if (ret.charAt(9) == '1'){
						System.err.println("Data segement detected\n");
					}


					// print out the rececived result
					System.err.println(ret);
				}} catch (Exception e) {; }
			}
		});

		mac_layer.startMacLayer();
		result_thread.start();

		for (int i=0; i<100; i++){
			String cmd = ftp_client.getCommand();
			ftp_client.sendCommand(cmd);
		}

		result_thread.join();
		mac_layer.stopMacLayer();
	}
}