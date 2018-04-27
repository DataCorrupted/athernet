package athernet.mac;

import athernet.physical.Receiver;
import athernet.physical.Transmitter;
import athernet.mac.MacPacket;

import java.util.ArrayList;

class MacLayer{

	// Status of the MacLayer
	public int status_;
	public final int IDLEMAC = 0;
	public final int BUSYMAC = 1;
	public final int EMPTTXQ = 2;
	public final int EMPTRXQ = 3;
	public final int LINKERR = -1;

	public static final byte TYPE_DATA = 1;
	public static final byte TYPE_INIT = 2;

	// The caller of MacLayer should assign a Mac Address.
	private byte address_;

	// The maximum resend time before we rule a link failure.
	private int max_resend_;

	// Time we wait until we rule a timeout and resend the same pack.
	private double timeout_;

	private Receiver recv_;
	private Transmitter trans_;

	// Manually stop all the threads.
	private boolean stop_

	// Max window size for moving window.
	private int window_size_ = 3;
	private MacPacket[] packet_array_ = new MacPacket[256];
	private ArrayList<Int>[] sending_list_ = new ArrayList<Int>[4];
	// All available ID.
	private ArrayBlockingQueue<Int> available_q_ 
		= new ArrayBlockingQueue<Int>(256);

	// Buffer for all received data.
	private ArrayBlockingQueue<MacPacket> data_q_ 
		= new ArrayBlockingQueue<MacPacket>(256);

	// Timer to tell if timeout.
	private double time;

	// Time(in ms) to sleep between opeartions.
	private int sleep_time_ = 20;
	public MacLayer(byte address){
		this(address, 0.1, 3, 3);
	}
	public MacLayer(byte address, double timeout, int max_resend, int window_size){
		address_ = address;
		max_resend_ = max_resend;
		timeout_ = timeout;
		window_size_ = window_size;
		recv_ = new Receiver();
		trans_ = new Transmitter();
		// Init id queue with all available ids.
		for (int i=0; i<256; i++){ available_q_.offer(i); }
	}

	// Send data pack.
	public void requestSend(byte dst, byte[] data){
		requestSend(new MacPacket(dst, address_, data));
	}
	// Send init pack.
	public void requestSend(byte dst, int len){
		requestSend(new MacPacket(dst, address_, len));
	}

	// Send pack.
	private void requestSend(MacPacket pack){
		// Making this pack id unavailable by moving it to 
		// sending queue.
		// Using take, we have to wait if necessary.
		int id = available_q_.take();
		sending_list_[dst].add(id);

		pack.setPacketID((byte) id);
		packet_array_[id] = pack;
	}

	private void send(){
		int id;
		int status;
		double curr_time;
		while (!stop_){
			for (int dst = 0; dst<4; dst++){
				ArrayList<Int> sending = sending_list_[dst];
				// Only cares whatever in the window.
				for (int i=0; 
				  i<Math.min(sending.size(), window_size); i++){
					
					id = sending.get(i)
					status = packet_array_[id].getStatus();
					curr_time = System.nanoTime()/1e9;
					if (status == MacPacket.STATUS_WATING){
						packet_array_[id].setStatus(MacPacket.STATUS_SENT);
						packet_array_[id].setTimeStamp(curr_time);
						while (!recv_.hasSignal()) {thread.sleep(1);}
						trans_.transmitOnePack(packet_array_[id].toArray());
					} else if (
					  status == MacPacket.STATUS_SENT &&
					  curr_time - packet_array_[id].getTimeStamp() > timeout_){
						packet_array_[id].setStatus(MacPacket.STATUS_WATING);
						packet_array_[id].onResendOnce();
					}
				}
			}
			thread.sleep(sleep_time_);
		}
	}

	private void recycleID(){
		int head;
		while (!stop_){
			for (int src = 0; src < 4; src ++){
				if (sending_list_[src].size() == 0){ 
					continue; 
				} else {
					head = sending_list_[src].get(0);
					// Just received ACK for the head. 
					// Remove it. Recycle packet id.
					if (packet_array_[head].getStatus == STATUS_ACKED) {
						sending_list_[src].remove(0);
						available_q_.put(id);
						status_ = MacLayer.LINK_OK;
					}
					// It has timeout so many times. We forget about it.
					if (packet_array_[head].getResendCounter() == max_resend_){
						sending_list_[src].remove(0);
						available_q_.put(id);
						status_ = MacLayer.LINKERR;
					}
				}
			}
			thread.sleep(sleep_time_);
		}
	}
	private void receive(){
		MacPacket macpack;
		while (!stop_){
			pack = MacPacket(recv_.receiveOnePacket());
			if (pack.getType() = TYPE_ACK_){
			// An ACK packet.
				int id = pack.getACKPacketID();
				int src = pack.getSrcAddr();
				packet_array_[id].setStatus(STATUS_ACKED);
			} else if (pack.getDstAddr() == address_) {
			// Not an ACK and the packet is for me.
				// Throws it away if the queue if full.
				if data_q_.offer(pack){
					// Or send an ACK to reply.
					requestSend(
						new MacPacket(pack.getSrcAddr, address_, pack.getPacketID())
					)
				}
			}
		}
	}

	public void getStatus(){ return status_; }
	public void stopMacLayer() { stop_ = true }
}