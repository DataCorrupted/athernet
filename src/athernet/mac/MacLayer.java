package athernet.mac;

import athernet.physical.Receiver;
import athernet.physical.Transmitter;
import athernet.mac.MacPacket;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MacLayer{
	// Whether mac layer displays debug information.
	private boolean echo_ = true;

	// Status of the MacLayer
	private int status_;
	public static final int LINKBSY = 1;
	public static final int LINKERR = -1;

	// The caller of MacLayer should assign a Mac Address.
	private byte src_addr_;
	private byte dst_addr_;

	// The maximum resend time before we rule a link failure.
	private int max_resend_;

	// Time we wait until we rule a timeout and resend the same pack.
	private double timeout_;

	// Transmitter and receiver.
	private Receiver recv_;
	private Transmitter trans_;

	// Max window size for moving window.
	private int window_size_;

	// All current using packet is stored here for easy reference.
	private MacPacket[] packet_array_ = new MacPacket[256];
	// Sending array for all addresses.
	// Java cannot create generic typed array.
	private ArrayList<Integer> sending_list_ = new ArrayList<Integer>();
	// All available ID.
	private ArrayBlockingQueue<Integer> available_q_ 
		= new ArrayBlockingQueue<Integer>(300);

	// Buffer for all received data.
	private MacPacket[] received_array_ = new MacPacket[256];
	private int head_idx_ = 0;
	private int window_pack_cnt = 0;

	private ArrayBlockingQueue<MacPacket> data_q_ 
		= new ArrayBlockingQueue<MacPacket>(300);

	// Whether or not to use CSMA
	// By default it's turned off.
	private boolean csma_ = false;
	private int backoff_time_ = 10;

	// Seperate threads to do their jobs.
	Thread send_thread_;	
	Thread recv_thread_;
	// Manually stop all the threads.
	private boolean stop_ = false;
	private final Lock mutex_ = new ReentrantLock(true);


	// Time(in ms) to sleep between opeartions.
	private int sleep_time_ = 20;
	public MacLayer(byte src_address, byte dst_address) throws Exception{
		this(src_address, dst_address, 0.5, 3, 30);
	}
	public MacLayer(
	  byte src_address, byte dst_address, 
	  double timeout, int max_resend, int window_size) 
	  throws Exception{
		dst_addr_ = dst_address;
		src_addr_ = src_address;
		max_resend_ = max_resend;
		timeout_ = timeout;
		window_size_ = window_size;
		
		recv_ = new Receiver();
		trans_ = new Transmitter();
		
		status_ = MacLayer.LINKBSY;
		// Init id queue with all available ids.
		for (int i=0; i<256; i++){ available_q_.offer(i); }

		send_thread_ = new Thread(new Runnable(){
			public void run() { try { send(); } catch (Exception e){;} }
		});
		recv_thread_ = new Thread(new Runnable(){
			public void run() { try { receive(); } catch (Exception e){;} }
		});
	}
	public int getStatus(){ return status_; }
	public void startMacLayer(){
		recv_thread_.start();
		System.out.println("\nThread receive() started.");
		recv_.startReceive();
		System.out.println("Thread record() started.");
		send_thread_.start();
		System.out.println("Thread send() started.\n");
	}
	public void stopMacLayer() throws Exception{ 
		stop_ = true; 
		recv_thread_.join();
		System.out.println("\nThread receive() finished.");
		recv_.stopReceive();
		System.out.println("Thread record() finished.");
		send_thread_.join();
		System.out.println("Thread send() finished.\n");
	}

	// Send data pack.
	public int requestSend(int offset, byte[] data) throws Exception{
		return 
			requestSend(new MacPacket(dst_addr_, src_addr_, (byte)offset, data));
	}
	// Send init pack.
	public int requestSend(int len) throws Exception{
		return requestSend(new MacPacket(dst_addr_, src_addr_, len));
	}

	// Send pack.
	public int requestSend(MacPacket pack) throws Exception{

		mutex_.lock();
		// Making this pack id unavailable by moving it to 
		// sending queue.
		// Using take, we have to wait if necessary.
		int id = available_q_.take();
		sending_list_.add(id);

		pack.setPacketID((byte) id);
		packet_array_[id] = pack;
		mutex_.unlock();
		return id;
	}

	private void send() throws Exception{
		int id;
		int status;
		double curr_time;
		while (!stop_){
			// Only cares whatever in the window.
			for (int i=0; 
			  i<Math.min(sending_list_.size(), window_size_); 
			  i++){
				id = sending_list_.get(i);
				status = packet_array_[id].getStatus();
				curr_time = System.nanoTime()/1e9;
				if (status == MacPacket.STATUS_WAITING){
					if (packet_array_[id].getType() == MacPacket.TYPE_DATA ||
					  packet_array_[id].getType() == MacPacket.TYPE_INIT){
						packet_array_[id].setStatus(MacPacket.STATUS_SENT);
					} else {
						// ACK packet don't need to be acked back. 
						// MAC ping reply and request too.
						// By default we consider it being acked.
						packet_array_[id].setStatus(MacPacket.STATUS_ACKED);
					}
					// Sleep for 0.5ms;
					Thread.sleep(0, 5000);
					while (csma_ && recv_.hasSignal()) {Thread.sleep(backoff_time_);}
					trans_.transmitOnePack(packet_array_[id].toArray());
					if (echo_){ System.err.printf("Packet #%4d sent.\n", id); }
					packet_array_[id].setTimeStamp(curr_time);
				} else if (
				  status == MacPacket.STATUS_SENT &&
				  curr_time - packet_array_[id].getTimeStamp() > timeout_){
					if (echo_){ System.err.printf("Packet #%4d timeout, resend.\n", id); }
					packet_array_[id].setStatus(MacPacket.STATUS_WAITING);
					packet_array_[id].onResendOnce();
				}
			}
			recycleID();
			Thread.sleep(sleep_time_);
		}
	}

	private void recycleID() throws Exception{

		if (sending_list_.size() == 0){ 
			return;
		}
		int head = sending_list_.get(0);
		while (packet_array_[head].getStatus() == MacPacket.STATUS_ACKED 
		  || packet_array_[head].getResendCounter() == max_resend_){
			// Just received ACK for the head. 
			// Remove it. Recycle packet id.
			if (
			  packet_array_[head].getStatus() == MacPacket.STATUS_ACKED){
				status_ = MacLayer.LINKBSY;
			}
			// It has timeout so many times. We forget about it.
			if (packet_array_[head].getResendCounter() == max_resend_){
				packet_array_[head].setStatus(MacPacket.STATUS_LOST);
				status_ = MacLayer.LINKERR;
				System.out.printf(
					"Packet #%4d cannot be delivered due to link error.\n", 
					packet_array_[head].getPacketID());
			}
			sending_list_.remove(0);
			available_q_.put(head);		
			if (sending_list_.size() == 0) {
				break;
			}
			head = sending_list_.get(0);	
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
			// Meaning this is sender's receiver.
			if (mac_pack.getDestAddr() != src_addr_ 
			  || mac_pack.getSrcAddr() != dst_addr_){
				continue;
			}

			// An ACK packet.
			if (mac_pack.getType() == MacPacket.TYPE_ACK){
				int id = mac_pack.getACKPacketID();
				packet_array_[id].setStatus(MacPacket.STATUS_ACKED);
				if (echo_) { System.out.printf(
					"Packet #%4d ACK received.\n",
					mac_pack.getACKPacketID()
				);}

			// Data or Init.
			} else if (mac_pack.getType() == MacPacket.TYPE_INIT ||
			  mac_pack.getType() == MacPacket.TYPE_DATA){
				if (echo_){ System.out.printf(
						"Packet #%4d received. ", 
						mac_pack.getPacketID()); 
				}
				// Throws it away if the queue if full.
				if (countDataPack() + window_pack_cnt <= 256){
					// Or send an ACK to reply.
					int id = requestSend(
						new MacPacket(
							dst_addr_, 
							src_addr_, 
							(byte) mac_pack.getPacketID()
					));

					if (echo_) { System.out.printf(
						"Packet type %s confirmed. ACK packet #%d sending.\n", 
						(mac_pack.getType() == MacPacket.TYPE_INIT) ? 
							"Init": "Data",
						id
					);}
					if (getIdxInWindow(mac_pack.getPacketID()) < window_size_){
						received_array_[mac_pack.getPacketID()] = mac_pack;
						window_pack_cnt ++;
						// Windows head received.
						while (received_array_[head_idx_] != null){
							// Put it to data q.
							data_q_.offer(received_array_[head_idx_]);
							// Remove it from window
							received_array_[head_idx_] = null;
							window_pack_cnt --;
							// Move window.
							head_idx_ = (head_idx_ + 1) % 256;
						}
					}
				} else {
					System.out.println(
						"Data queue is full, ignoring this packet.");
				}

			// Mac request.
			} else if (mac_pack.getType() == MacPacket.TYPE_MACPING_REQST) {
				mac_pack.convertMacRequestToMacReply();
				requestSend(mac_pack);

			// Mac reply. 
			} else if (mac_pack.getType() == MacPacket.TYPE_MACPING_REPLY) {
				data_q_.offer(mac_pack);
			}
		}
	}

	private int getIdxInWindow(int id){
		// Converts negative number x to (x + 256) while positive remains unchanged.
		return (id - head_idx_) & 0xFF;
	}

	public MacPacket getOnePack() throws Exception{
		return data_q_.take();
	}
	public int countDataPack() {
		return data_q_.size();
	}
	public int countUnAcked(){
		int cnt = 0;
		for (int i=0; i<sending_list_.size(); i++){
			int id = sending_list_.get(i);
			if (packet_array_[id].getType() == MacPacket.TYPE_DATA &&
			  (packet_array_[id].getStatus() == MacPacket.STATUS_WAITING ||
			  packet_array_[id].getStatus() == MacPacket.STATUS_SENT)){
				cnt ++;
			}
		}
		return cnt;
	}
	public void turnCSMA() { csma_ = !csma_; }
	public void turnRecvEcho() { recv_.echo_ = !recv_.echo_; }
	public void turnEcho() { echo_ = !echo_; }
	public boolean isIdle(){ return available_q_.size() == 256; }
}