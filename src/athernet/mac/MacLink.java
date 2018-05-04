package athernet.mac;

import athernet.physical.Receiver;
import athernet.physical.Transmitter;
import athernet.mac.MacPacket;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

class MacLink{

	// Status of the MacLayer
	private int status_;
	public static final int LINK_OK = 1;
	public static final int LINKERR = -1;
	public static final int LINKIDL = 0;

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
	private int window_size_ = 3;

	// All current using packet is stored here for easy reference.
	private MacPacket[] packet_array_ = new MacPacket[256];

	// Sending array for all addresses.
	// Java cannot create generic typed array.
	private ArrayList<Integer> sending_list_ = new ArrayList<Integer>();
	// All available ID.

	private ArrayBlockingQueue<Integer> available_q_ 
		= new ArrayBlockingQueue<Integer>(256);

	// Buffer for all received data.
	private ArrayBlockingQueue<MacPacket> data_q_ 
		= new ArrayBlockingQueue<MacPacket>(256);

	private MacPacket[] received_pack_ = new MacPacket[256];
	private int win_head_ = 0;

	private String link_info_;
	public MacLink(
	  byte src, byte dst, 
	  double timeout, int max_resend, int window_size){
		max_resend_ = max_resend;
		timeout_ = timeout;
		window_size_ = window_size;

		src_addr_ = src;
		dst_addr_ = dst;

		status_ = MacLink.LINKIDL;
		// Init id queue with all available ids.
		for (int i=0; i<256; i++){ available_q_.offer(i); }
		link_info_ = "Link from 0x" +String.format("%1s", Integer.toHexString(src_addr_))
					+ " to 0x" + String.format("%1s", Integer.toHexString(dst_addr_)); 
	}
	public String getLinkInfo() { return link_info_; }
	public int getStatus(){ return status_; }
	
	public int requestSend(int offset, byte[] data) throws Exception{
		return 
			requestSend(new MacPacket(dst_addr_, src_addr_, (byte)offset, data));
	}
	// Send init pack.
	public int requestSend(int pack_cnt, int len) throws Exception{
		return 
			requestSend(new MacPacket(dst_addr_, src_addr_, pack_cnt, len));
	}

	// Send pack.
	public int requestSend(MacPacket pack) throws Exception{
		// Making this pack id unavailable by moving it to 
		// sending queue.
		// Using take, we have to wait if necessary.
		int id = available_q_.take();
		sending_list_.add(id);

		pack.setPacketID((byte) id);
		packet_array_[id] = pack;
		return id;
	}


	public MacPacket pollPackToSend() throws Exception{
		int id;
		int status;
		double curr_time;
		MacPacket to_send = null;
		// Only cares whatever in the window.
		for (int i=0; 
		  i<Math.min(sending_list_.size(), window_size_); 
		  i++){
			id = sending_list_.get(i);
			status = packet_array_[id].getStatus();
			curr_time = System.nanoTime()/1e9;
			if (status == MacPacket.STATUS_WAITING){
				if (packet_array_[id].getType() != MacPacket.TYPE_ACK){
					packet_array_[id].setStatus(MacPacket.STATUS_SENT);
				} else {
					// ACK packet don't need to be acked back. 
					// By default we consider it being acked.
					packet_array_[id].setStatus(MacPacket.STATUS_ACKED);
				}
				to_send = packet_array_[id];
				System.err.printf(link_info_ + ": packet #%3d sent\n", id);
				packet_array_[id].setTimeStamp(curr_time);
			} else if (
			  status == MacPacket.STATUS_SENT &&
			  curr_time - packet_array_[id].getTimeStamp() > timeout_){
				System.err.printf(link_info_ + ": packet #%3d timeout, resend.\n", id);
				packet_array_[id].setStatus(MacPacket.STATUS_WAITING);
				packet_array_[id].onResendOnce();
			}
		}
		recycleID();
		return to_send;
	}
	private void recycleID() throws Exception{
		// Empty queue.
		if (sending_list_.size() == 0){ 
			return; 
		}

		// Non-empty queue, recycle ID.
		int head;
		head = sending_list_.get(0);
		// Just received ACK for the head. 
		if (
		  packet_array_[head].getStatus() == MacPacket.STATUS_ACKED){
			sending_list_.remove(0);
			available_q_.put(head);
			status_ = MacLink.LINK_OK;
		}
		// It has timeout so many times. We forget about it.
		if (packet_array_[head].getResendCounter() == max_resend_){
			packet_array_[head].setStatus(MacPacket.STATUS_LOST);
			sending_list_.remove(0);
			available_q_.put(head);
			status_ = MacLink.LINKERR;
		}
	}

	// Give a pack, organize queues accordingly.
	public void giveReceivedPack(MacPacket mac_pack) throws Exception{
		
		if (mac_pack.getType() == MacPacket.TYPE_ACK){
			int id = mac_pack.getACKPacketID();
			int src = mac_pack.getSrcAddr();
			packet_array_[id].setStatus(MacPacket.STATUS_ACKED);
			System.out.printf(
				link_info_ + ": packet #%3d ACK received.\n",
				mac_pack.getACKPacketID()
			);
		// Not an ACK.
		} else {
			int id = mac_pack.getPacketID();
			int win_idx = getIndexInWindow(win_head_, id);
			System.out.printf(
				link_info_ + ": packet #%3d received. ", 
				mac_pack.getPacketID());
			// Throws it away if the queue if full.
			if (win_idx + countDataPack() < 256){
				// Or send an ACK to reply.
				requestSend(new MacPacket(dst_addr_, src_addr_, mac_pack.getPacketID()));
				received_pack_[id] = mac_pack;
				// Make sure it's not null and it can be it's taken, move it forward.
				while (received_pack_[win_head_] instanceof MacPacket
				  && received_pack_[win_head_].getStatus() != MacPacket.STATUS_TAKEN){
					data_q_.offer(mac_pack);
					// Circule windows head around.
					win_head_ = (win_head_ + 1) % 256;
				}
				System.out.println("ACK sent.");
			} else {
				System.out.println(
					"Data queue is full, ignoring this packet.");
			}
		}
	}
	public int getIndexInWindow(int win_head_, int id){
		if (id >= win_head_){
			return id - win_head_;
		} else {
			return (id + 256 - win_head_);
		}
	}
	public MacPacket getOnePack() throws Exception{
		MacPacket mac_pack = data_q_.take();
		mac_pack.take();
		return mac_pack;
	}
	public int countDataPack() {
		return data_q_.size();
	}
	public int countUnsent(){
		int cnt = 0;
		for (int i=0; i<sending_list_.size(); i++){
			int id = sending_list_.get(i);
			if (packet_array_[id].getStatus() == MacPacket.STATUS_WAITING){
				cnt ++;
			}
		}
		return cnt;
	}
}