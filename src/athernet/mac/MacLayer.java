package athernet.mac;

import athernet.physical.Receiver;
import athernet.physical.Transmitter;
import athernet.mac.MacPacket;

class MacLayer{
	// The caller of MacLayer should assign a Mac Address.
	public byte address_;

	// The maximum resend time before we rule a link failure.
	public int max_resend_;

	// Time we wait until we rule a timeout and resend the same pack.
	public double timeout_;

	private Receiver recv_;
	private Transmitter trans_;

	// Status of the MacLayer
	public int status_;
	public final int IDLEMAC = 0;
	public final int BUSYMAC = 1;
	public final int EMPTTXQ = 2;
	public final int EMPTRXQ = 3;
	public final int LINKERR = -1;

	public MacLayer(byte address){
		this(address, 0.1, 3);
	}
	public MacLayer(byte address, double timeout, int max_resend){
		address_ = address;
		max_resend_ = max_resend;
		timeout_ = timeout;
		recv_ = new Receiver();
		trans_ = new Transmitter();
	}

	public void requestSend(byte[]){
		return;
	}

	private void send(){
		return;
	}

	private void receive(){
		return;
	}

	private void timer(){
		return;
	}

	public void getStatus(){ return status_; }
}