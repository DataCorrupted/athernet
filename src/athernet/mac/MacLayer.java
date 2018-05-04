package athernet.mac;

import athernet.physical.Receiver;
import athernet.physical.Transmitter;
import athernet.mac.MacPacket;

public class MacLayer{

	// The caller of MacLayer should assign a Mac Address.
	private byte address_;

	// Transmitter and receiver.
	private Receiver recv_;
	private Transmitter trans_;

	private MacLink[] links_ = new MacLink[4];
	// Seperate threads to do their jobs.
	Thread send_thread_;	
	Thread recv_thread_;
	// Manually stop all the threads.
	private boolean stop_ = false;

	// Time(in ms) to sleep between opeartions.
	private int sleep_time_ = 1;

	public MacLayer(byte address) throws Exception{
		this(address, 1.5, 5, 3);
	}
	public MacLayer(
	  byte src_addr, double timeout, int max_resend, int window_size) 
	  throws Exception{
		address_ = src_addr;

		recv_ = new Receiver();
		recv_.echo_ = true;
		trans_ = new Transmitter();
		
		for (byte dst = 0; dst<4; dst++){
			links_[dst] = new MacLink(src_addr, dst, timeout, max_resend, window_size);
		}
		send_thread_ = new Thread(new Runnable(){
			public void run() { try { send(); } catch (Exception e){;} }
		});
		recv_thread_ = new Thread(new Runnable(){
			public void run() { try { receive(); } catch (Exception e){;} }
		});
	}

	public void startMacLayer(){
		recv_thread_.start();
		System.out.println("Thread receive() started.");
		recv_.startReceive();
		System.out.println("Thread record() started.");
		send_thread_.start();
		System.out.println("Thread send() started.");
	}
	public void stopMacLayer() throws Exception{ 
		stop_ = true; 
		recv_thread_.join();
		System.out.println("Thread receive() finished.");
		recv_.stopReceive();
		System.out.println("Thread record() finished.");
		send_thread_.join();
		System.out.println("Thread send() finished.");
	}

	// Send data pack.
	public int requestSend(int dst, int offset, byte[] data) throws Exception{
		return 
			links_[dst].requestSend((byte)offset, data);
	}
	// Send init pack.
	public int requestSend(int dst, int pack_cnt, int len) throws Exception{
		return 
			links_[dst].requestSend(pack_cnt, len);
	}

	// Send pack.
	public int requestSend(MacPacket pack) throws Exception{
		return links_[pack.getDestAddr()].requestSend(pack);
	}

	private void send() throws Exception{
		MacPacket mac_pack;
		while (!stop_){
			for (int dst = 0; dst<4; dst++){
				mac_pack = links_[dst].pollPackToSend();
				if (mac_pack == null) { continue; }
				while (recv_.hasSignal()) {Thread.sleep(1);}
				trans_.transmitOnePack(mac_pack.toArray());
			}
			Thread.sleep(sleep_time_);
		}
	}

	private void receive() throws Exception{

		MacPacket mac_pack;
		while (!stop_){
			byte[] data = recv_.receiveOnePacket();
			if (data.length == 0) {
				continue;
			} 
			mac_pack = new MacPacket(data);
			// An ACK packet.
			// Meaning this is sender's receiver.
			if (mac_pack.getDestAddr() != address_){
				continue;
			}	
			links_[mac_pack.getSrcAddr()].giveReceivedPack(mac_pack);
		}
	}

	public MacPacket getOnePack(int address) throws Exception{
		return links_[address].getOnePack();
	}
	public int countDataPack(int address) {
		return links_[address].countDataPack();
	}
	public int countUnsent(int address){
		return links_[address].countUnsent();
	}
	public int getStatus(int address){ 
		return links_[address].getStatus(); 
	}
	public String getLinkInfo(int address){
		return links_[address].getLinkInfo();
	}
}