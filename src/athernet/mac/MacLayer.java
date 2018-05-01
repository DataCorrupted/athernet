package athernet.mac;

import athernet.physical.Receiver;
import athernet.physical.Transmitter;
import athernet.mac.MacPacket;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

class MacLayer{

	// Status of the MacLayer
	public int status_;
	public static final int LINKIDL = 0;
	public static final int LINK_OK = 1;
	public static final int LINKERR = -1;

	// The caller of MacLayer should assign a Mac Address.
	private byte address_;

	// The maximum resend time before we rule a link failure.
	private int max_resend_;

	// Time we wait until we rule a timeout and resend the same pack.
	private double timeout_;

	// Transmitter and receiver.
	private Receiver recv_;
	private Transmitter trans_;

	// Max window size for moving window.
	private int window_size_ = 3;

	// All current using packet is stored here for easy reference.
	private MacPacket[] packet_array_ = new MacPacket[256];
	// Sending array for all addresses.
	// Java cannot create generic typed array.
	private ArrayList<ArrayList<Integer>> sending_list_ 
		= new ArrayList<ArrayList<Integer>>();
	// All available ID.
	private ArrayBlockingQueue<Integer> available_q_ 
		= new ArrayBlockingQueue<Integer>(256);

	// Buffer for all received data.
	private ArrayBlockingQueue<MacPacket> data_q_ 
		= new ArrayBlockingQueue<MacPacket>(256);

	// Seperate threads to do their jobs.
	Thread send_thread_;	
	Thread recycle_thread_;
	Thread recv_thread_;
	// Manually stop all the threads.
	private boolean stop_ = false;

	// Time(in ms) to sleep between opeartions.
	private int sleep_time_ = 20;
	public MacLayer(byte address) throws Exception{
		this(address, 0.5, 3, 3);
	}
	public MacLayer(
	  byte address, double timeout, int max_resend, int window_size) 
	  throws Exception{
		address_ = address;
		max_resend_ = max_resend;
		timeout_ = timeout;
		window_size_ = window_size;
		
		recv_ = new Receiver();
		trans_ = new Transmitter();
		
		status_ = MacLayer.LINKIDL;
		// Init id queue with all available ids.
		for (int i=0; i<256; i++){ available_q_.offer(i); }

		// Create 4 array for each address.
		for (int i=0; i<4; i++){
			sending_list_.add(new ArrayList<Integer>());
		}

		send_thread_ = new Thread(new Runnable(){
			public void run() { try { send(); } catch (Exception e){;} }
		});
		recycle_thread_ = new Thread(new Runnable(){
			public void run() { try { recycleID(); } catch (Exception e){;} }
		});
		recv_thread_ = new Thread(new Runnable(){
			public void run() { try { receive(); } catch (Exception e){;} }
		});
	}
	public int getStatus(){ return status_; }
	public void startMacLayer(){
		recv_thread_.start();
		System.out.println("Thread receive() started.");
		recv_.startReceive();
		System.out.println("Thread record() started.");
		send_thread_.start();
		System.out.println("Thread send() started.");
		recycle_thread_.start();
		System.out.println("Thread recycleID() started.");
	}
	public void stopMacLayer() throws Exception{ 
		stop_ = true; 
		recv_thread_.join();
		System.out.println("Thread receive() finished.");
		recv_.stopReceive();
		System.out.println("Thread record() finished.");
		send_thread_.join();
		System.out.println("Thread send() finished.");
		recycle_thread_.join();
		System.out.println("Thread recycleID() finished.");
	}

	// Send data pack.
	public void requestSend(int dst, int offset, byte[] data) throws Exception{
		requestSend(new MacPacket((byte)dst, address_, (byte)offset, data));
	}
	// Send init pack.
	public void requestSend(int dst, int len) throws Exception{
		requestSend(new MacPacket((byte)dst, address_, len));
	}

	// Send pack.
	private void requestSend(MacPacket pack) throws Exception{
		// Making this pack id unavailable by moving it to 
		// sending queue.
		// Using take, we have to wait if necessary.
		int id = available_q_.take();
		sending_list_.get(pack.getDestAddr()).add(id);

		pack.setPacketID((byte) id);
		packet_array_[id] = pack;
	}

	private void send() throws Exception{
		int id;
		int status;
		double curr_time;
		while (!stop_){
			for (int dst = 0; dst<4; dst++){
				// Only cares whatever in the window.
				for (int i=0; 
				  i<Math.min(sending_list_.get(dst).size(), window_size_); 
				  i++){
					id = sending_list_.get(dst).get(i);
//					System.out.println(id);
					status = packet_array_[id].getStatus();
					curr_time = System.nanoTime()/1e9;
					if (status == MacPacket.STATUS_WAITING){
						packet_array_[id].setStatus(MacPacket.STATUS_SENT);
						while (recv_.hasSignal()) {Thread.sleep(1);}
						trans_.transmitOnePack(packet_array_[id].toArray());
						System.err.printf("Packet #%4d sent.\n", id);
						packet_array_[id].setTimeStamp(curr_time);
					} else if (
					  status == MacPacket.STATUS_SENT &&
					  curr_time - packet_array_[id].getTimeStamp() > timeout_){
						System.err.printf("Packet #%4d timeout, resend.\n", id);
						packet_array_[id].setStatus(MacPacket.STATUS_WAITING);
						packet_array_[id].onResendOnce();
					}
				}
			}
			Thread.sleep(sleep_time_);
		}
	}

	private void recycleID() throws Exception{

		int head;
		while (!stop_){
			for (int src = 0; src < 4; src ++){
				if (sending_list_.get(src).size() == 0){ 
					continue; 
				} else {
					head = sending_list_.get(src).get(0);
					// Just received ACK for the head. 
					// Remove it. Recycle packet id.
					if (
					  packet_array_[head].getStatus() == MacPacket.STATUS_ACKED){
						sending_list_.get(src).remove(0);
						available_q_.put(head);
						status_ = MacLayer.LINK_OK;
					}
					// It has timeout so many times. We forget about it.
					if (packet_array_[head].getResendCounter() == max_resend_){
						sending_list_.get(src).remove(0);
						available_q_.put(head);
						status_ = MacLayer.LINKERR;
					}
				}
			}
			if (available_q_.size() == 256){
				status_ = MacLayer.LINKIDL;
			}
			Thread.sleep(sleep_time_);
		}
	}

	private void receive() throws Exception{
		

		MacPacket mac_pack;
		while (!stop_){
			byte[] data = recv_.receiveOnePacket();
			System.out.println(data.length);
			if (data.length == 0) {
				continue;
			} 
			mac_pack = new MacPacket(data);
			System.out.println("EXE");
			// An ACK packet.
			System.out.println(mac_pack.getDestAddr());
			System.out.println(address_);
			System.out.println(mac_pack.getDestAddr() == address_);
			if (mac_pack.getType() == MacPacket.TYPE_ACK){
				int id = mac_pack.getACKPacketID();
				int src = mac_pack.getSrcAddr();
				packet_array_[id].setStatus(MacPacket.STATUS_ACKED);
			
			// Not an ACK and the packet is for me.
			} else if (mac_pack.getDestAddr() == address_) {
				// Throws it away if the queue if full.
				if (data_q_.offer(mac_pack)){
					// Or send an ACK to reply.
					requestSend(
						new MacPacket(
							mac_pack.getSrcAddr(), 
							address_, 
							mac_pack.getPacketID()
					));
				}
			}
		}
	}

	public MacPacket getOnePack() throws Exception{
		return data_q_.take();
	}
	public int countDataPack() {
		return data_q_.size();
	}
	public int countUnsent(byte address){
		int cnt = 0;
		for (int i=0; i<sending_list_.get(address).size(); i++){
			int id = sending_list_.get(address).get(i);
			if (packet_array_[id].getStatus() == MacPacket.STATUS_WAITING){
				cnt ++;
			}
		}
		return cnt;
	}

	public static void main(String[] args) throws Exception{
		MacLayer mac_layer = new MacLayer((byte)0x1);

		// hello world.
		final byte[] data1 = { 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 
								0x77, 0x6f, 0x72, 0x6c, 0x64, 0x2e};
		// .dlrow olleh
		final byte[] data2 = {0x2e, 0x64, 0x6c, 0x72, 0x6f, 0x77,
								0x20, 0x6f, 0x6c, 0x6c, 0x65, 0x68};

		mac_layer.startMacLayer();

		mac_layer.requestSend(0x1, data1.length + data2.length);
		Thread.sleep(3000);

		mac_layer.requestSend(0x1, 0, data1);
		mac_layer.requestSend(0x1, 12, data2);
		Thread.sleep(1000);

		MacPacket mac_pack;
		// Length packet must come first.
		if (mac_layer.countDataPack() == 2){
			mac_pack = mac_layer.getOnePack();
			System.out.println(mac_pack.getData());
			mac_pack = mac_layer.getOnePack();
			System.out.println(mac_pack.getData());
		} else {
			System.out.println("Error: Packet number mismatch.");
		}

		mac_layer.stopMacLayer();
	}
}