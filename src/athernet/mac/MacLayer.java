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
	// Let's please Java with Object first, but we all know
	// what type it is, so we cast it down.
	// This is because Java can't creat generic typed array.
	@SuppressWarnings("unchecked")
	private ArrayList<Integer>[] sending_list_ 
		= (ArrayList<Integer>[]) new Object[4];
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
		this(address, 0.1, 3, 3);
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
		send_thread_.start();
		recycle_thread_.start();
		recv_thread_.start();
	}
	public void stopMacLayer() throws Exception{ 
		stop_ = true; 
		send_thread_.join();
		recycle_thread_.join();
		recv_thread_.join();
	}

	// Send data pack.
	public void requestSend(byte dst, byte offset, byte[] data) throws Exception{
		requestSend(new MacPacket(dst, address_, offset, data));
	}
	// Send init pack.
	public void requestSend(byte dst, int len) throws Exception{
		requestSend(new MacPacket(dst, address_, len));
	}

	// Send pack.
	private void requestSend(MacPacket pack) throws Exception{
		// Making this pack id unavailable by moving it to 
		// sending queue.
		// Using take, we have to wait if necessary.
		int id = available_q_.take();
		sending_list_[pack.getDestAddr()].add(id);

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
				  i<Math.min(sending_list_[dst].size(), window_size_); i++){
					
					id = sending_list_[dst].get(i);
					status = packet_array_[id].getStatus();
					curr_time = System.nanoTime()/1e9;
					if (status == MacPacket.STATUS_WAITING){
						packet_array_[id].setStatus(MacPacket.STATUS_SENT);
						packet_array_[id].setTimeStamp(curr_time);
						while (!recv_.hasSignal()) {Thread.sleep(1);}
						trans_.transmitOnePack(packet_array_[id].toArray());
					} else if (
					  status == MacPacket.STATUS_SENT &&
					  curr_time - packet_array_[id].getTimeStamp() > timeout_){
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
				if (sending_list_[src].size() == 0){ 
					continue; 
				} else {
					head = sending_list_[src].get(0);
					// Just received ACK for the head. 
					// Remove it. Recycle packet id.
					if (
					  packet_array_[head].getStatus() == MacPacket.STATUS_ACKED){
						sending_list_[src].remove(0);
						available_q_.put(head);
						status_ = MacLayer.LINK_OK;
					}
					// It has timeout so many times. We forget about it.
					if (packet_array_[head].getResendCounter() == max_resend_){
						sending_list_[src].remove(0);
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
			mac_pack = new MacPacket(recv_.receiveOnePacket());
			if (mac_pack.getType() == MacPacket.TYPE_ACK){
			// An ACK packet.
				int id = mac_pack.getACKPacketID();
				int src = mac_pack.getSrcAddr();
				packet_array_[id].setStatus(MacPacket.STATUS_ACKED);
			} else if (mac_pack.getDestAddr() == address_) {
			// Not an ACK and the packet is for me.
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

	public void getOnePack() throws Exception{
		return data_q_.take();
	}

	public static void main(String[] args){
		mac_layer = new MacLayer(0x1);
		mac_layer.requestSend(0x1, 16);
		mac_layer.getOnePack();
	}
}