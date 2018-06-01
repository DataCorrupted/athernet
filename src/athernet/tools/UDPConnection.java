package athernet.tools;

import org.savarese.vserv.tcpip.*;
import com.savarese.rocksaw.net.RawSocket;
import static com.savarese.rocksaw.net.RawSocket.PF_INET;
import static com.savarese.rocksaw.net.RawSocket.getProtocolByName;
import java.net.InetAddress;

class UDPConnection{
	public static void send() throws Exception{
		RawSocket socket = new RawSocket();
		socket.open(PF_INET, getProtocolByName("udp"));
		UDPPacket send_pack = new UDPPacket(25);
		InetAddress addr = InetAddress.getByName("10.20.203.138");

		int ip_header_len = 5;
		int udp_header_len = 8;		
		int data_len = 12;

		byte[] data = 
			new byte[ip_header_len + udp_header_len + data_len];

		send_pack.setData(data);

		send_pack.setIPHeaderLength(ip_header_len);
		send_pack.setSourcePort(16384);
		send_pack.setDestinationPort(16384);

		byte[] str = "Hello World.".getBytes();
		System.arraycopy(str, 0, data, ip_header_len + udp_header_len, str.length);

		send_pack.computeUDPChecksum();

		for (int i=0; i<10; i++){
			socket.write(addr, data, ip_header_len, send_pack.getUDPPacketLength());
			Thread.sleep(1000);
		}

		socket.close();
	}
	public static void receive() throws Exception{
		RawSocket socket = new RawSocket();
		socket.open(PF_INET, getProtocolByName("udp"));
		UDPPacket recv_pack = new UDPPacket(1);

		int ip_header_len = 5;
		int udp_header_len = 8;		

		byte[] data = 
			new byte[ip_header_len + udp_header_len + 12];
		recv_pack.setData(data);
		
		byte[] addr = {(byte)10, (byte)20, (byte)193, (byte)201};
		for (int i=0; i<10; i++){
			socket.read(data, addr);

			String src_addr = recv_pack.getSourceAsInetAddress().toString();
			String dst_addr = addr.toString();
			int src_port = recv_pack.getSourcePort();
			int dst_port = recv_pack.getDestinationPort();
			int payload = recv_pack.getUDPPacketLength();
			
			System.out.println(
				"Got packet from " + src_addr + ":" + src_port +
				" to " + dst_addr + ":" + dst_port + 
				" with a payload of " + payload + " bytes.");
		}

		socket.close();
	}

	public static void main(String[] args) throws Exception{
		if (args.length == 0){
			System.out.println(
				"No parameter given! Halt.\n" +
				"Usage: java -cp . athernet.tools.UDPConnection [send|receive]");
		} else if (args[0].equals("send")){
			send();
		} else if (args[0].equals("receive")){
			receive();
		}
	}
}