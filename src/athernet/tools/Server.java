package athernet.tools;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;
import athernet.nat.NatPacket;

class Server{
	static public void receiveUDP(int[] addr, MacLayer mac_layer) 
	  throws Exception{
	  	byte[] data = mac_layer.getOnePack().getData();
	  	NatPacket nat_pack = new NatPacket(data);
	  	System.err.println(
	  		"Received a UDP packet from " + 
	  		nat_pack.getIPString() + ":" + nat_pack.getPort() +
	  		" with the following content: " + nat_pack.getContentString()
	  	);
	}
	static public void replyICMP(int[] addr, MacLayer mac_layer) 
	  throws Exception{
	  	// Get one packet, send it right away.
		mac_layer.requestSend(mac_layer.getOnePack().getData());
	}

	static public void main(String[] args) throws Exception{
		if (args.length < 2){
			System.err.println("You have to provid args.");
		}
		int[] addr = {192, 168, 1, 1};

		MacLayer mac_layer = new MacLayer((byte) 0x1, (byte) 0x2);
		mac_layer.startMacLayer();

		if (args[0].equals("file")){
			for (int i=0; i<100; i++){
				receiveUDP(addr, mac_layer);
			}
		} else if (args[0].equals("ping")){
			for (int i=0; i<100; i++){
				replyICMP(addr, mac_layer);
			}
		}

		mac_layer.stopMacLayer();
	}
}