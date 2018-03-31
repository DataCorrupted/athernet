package AcousticNetwork;

import AcousticNetwork.FileI;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundI;
import AcousticNetwork.Modulation;

import java.util.concurrent.ArrayBlockingQueue;

public class Receiver{
	public final int RECEVED = 1;
	public final int TIMEOUT = 0;
	public final int CRCINVL = -1;

	private int pack_size_;
	private int head_size_ = 2;
	private int data_size_;
	private int sample_rate_;

	private CRC8 crc8_;
	private SoundI i_sound_;	
	private ArrayBlockingQueue<Double> double_q_;
	private Thread recorder_;
	private Modulation demodulator_;

	private boolean file_stop_ = false;

	public int getPackSize(){ return pack_size_; }
	public int getDataSize(){ return data_size_; }
	
	public double getPower(){ return i_sound_.getPower(); }
	public boolean hasSignal(){ return i_sound_.hasSignal(); }
	
	public Receiver() throws Exception{
		this(44100, 16, 0.1);
	}
	public Receiver(int pack_size) throws Exception{
		this(44100, pack_size, 0.1);
	}

	// buf_len: for how long(in seconds) should the buffer contain history sound data.
	public Receiver(
	  int sample_rate, int pack_size, double buf_len) throws Exception{
	  	sample_rate_ = sample_rate;
		pack_size_ = pack_size;
		data_size_ = pack_size_ - head_size_;
		crc8_ = new CRC8(0x9c, (short) 0xff);
		double_q_ = new ArrayBlockingQueue<Double>((int) (sample_rate * buf_len));
		i_sound_ = new SoundI(sample_rate, double_q_);
		demodulator_ = new Modulation(sample_rate);
	}

	public void startReceive() throws Exception{		
		recorder_ = new Thread(i_sound_);
		recorder_.start();

	}
	public void stopReceive() throws Exception{
		i_sound_.stopConcurrentReadThread();
		recorder_.join();
	}

	// Given a byte array, put received data in it.
	// Return;
	// 		RECEVED(1): received data.
	//		TIMEOUT(0): timeout when waiting for data.
	// 		CRCINVL(-1): CRC8 check failed.
	public int receiveOnePacket(byte[] in) throws Exception{
		if (in.length < pack_size_){
			System.out.println("Warning[Receiver.receiveOnePacket(byte[])]: given pack too small");
		}
		int time = 0;
		double timeout = 
			demodulator_.getHeaderLength() + 
			demodulator_.getBitLength() * pack_size_ * 8;
		timeout *= 2;	// 100% extra waiting time.
		// Offer double to demodulate until a packet is offered.
		// I think a better way is to let demodulate tell me what it's seeing
		// Whether the header is matched then I wait for longer,
		// or it's receiving nothing, then I timeout.
		int r = Modulation.NOTHING;

		while (r != Modulation.RCVEDDAT && time <= timeout) {
		//while (r != Modulation.RCVEDDAT) {
			r = demodulator_.demodulation(double_q_.take(), pack_size_);
			time += (r == Modulation.NOTHING)? 1:0;
		}
		in = demodulator_.getPacket();
		if (in.length == 0){
			// I suppose to get a full length packet, but something unexpected happened.
			System.out.println("No packet found, possibly time out when waiting for one.");
			return TIMEOUT;
		}
		// Initial read.
		crc8_.update(in, 1, pack_size_-1);
		int pack_cnt = in[1];
		if ((byte) crc8_.getValue() == in[0]){
			System.out.printf("Packet #%3d received.\n", pack_cnt);
		} else {
			// No useful byte in a broken pack.
			System.out.printf("Packet #%3d receive failed. CRC8 checksum wrong.\n", pack_cnt);
			return CRCINVL;
		}
		crc8_.reset();
		return RECEVED;
	}

	static public void main(String[] args) throws Exception{
		Receiver receiver = new Receiver(16);
		int f;
		byte[] recv_data = new byte[16];
		boolean from_file = true;
		if (args.length != 0) { from_file = false; }
		if (!from_file){
			receiver.startReceive();
			f = receiver.receiveOnePacket(recv_data);
			receiver.stopReceive();
		} else {
			final String i_path = "./std_output.wav";
			Thread simu_receiver = new Thread( new Runnable(){
				public void run() { 
					try{ receiver.receiveFromFile(i_path);}
					catch (Exception e) {;}
			}});
			simu_receiver.start();
			f = receiver.receiveOnePacket(recv_data);
			receiver.stopFileStream();
			simu_receiver.join();
		}

		for (int i=0; i<16; i++){
			System.out.print(recv_data[i] + " ");
		}
		System.out.println();
	}

	// This function should run in an independent thread.
	// Do not call it manually.
	private void receiveFromFile(String i_file) throws Exception{
		file_stop_ = false;
		double[] wave = i_sound_.readFile(i_file);
		int wait_time = 0; //(int) 1.0e4/sample_rate_;
		for (int i=0; i<wave.length; i++){

			while (!double_q_.offer(wave[i])){
				// Overflow.
				System.out.println("Warning[Receiver.receiveFromFile(String)]: Bufferoverflowed, the latested data just been throwed.");
				// Retrive the oldest one from the queue,
				// Regardless the queue is empty or not.
				double_q_.poll();
			}
		}
		// The file is over, in order to keep the queue moving
		// we stuck 0 to it.
		while (!file_stop_){
			double_q_.put(0.0);
		}
	}
	private void stopFileStream() { file_stop_ = true; }

}