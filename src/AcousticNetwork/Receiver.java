package AcousticNetwork;

import AcousticNetwork.FileO;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundI;
import AcousticNetwork.OFDM;
import AcousticNetwork.CheckIO;

import java.util.concurrent.ArrayBlockingQueue;

class Receiver{
	// Input sound.
	private SoundI i_sound_;

	// A thread to record all sound.
	private Thread recorder_;

	// Buffer to store the sound.
	private ArrayBlockingQueue<Double> double_q_;

	// Demodulate sound data received.
	private OFDM demodulator_;

	// Check whether received data is correct.
	private CRC8 crc8_;

	// How long do we wait if there is nothing.
	private double timeout_;

	private int sample_rate_;
	private int pack_size_;
	private int head_size_ = 2;
	private int data_size_;

	public Receiver() throws Exception{
		this(44100, 64, 0.1);
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

		double_q_ = new ArrayBlockingQueue<Double>((int) (sample_rate * buf_len));

		i_sound_ = new SoundI(sample_rate, double_q_);		
		recorder_ = new Thread(i_sound_);

		demodulator_ = new OFDM(44100, 1000, 1000, 8, pack_size_*8);

		crc8_ = new CRC8(0x9c, (short) 0xff);
		
		// 90% of a pack's time for timeout.
		timeout_ = 
			(demodulator_.getHeaderLength() + 
			 demodulator_.getBitLength() * pack_size_ * 8) * 0.9;
	}
	public void startReceive() throws Exception{	
		recorder_.start();
	}
	public void stopReceive() throws Exception{
		i_sound_.stopConcurrentReadThread();
		recorder_.join();
	}

	public byte[] receiveOnePacket() throws Exception{
		int byte_read = data_size_;
		byte[] i_stream = new byte[pack_size_];

		int time = 0;
		// Offer double to demodulate until a packet is offered.
		// I think a better way is to let demodulate tell me what it's seeing
		// Whether the header is matched then I wait for longer,
		// or it's receiving nothing, then I timeout.
		int r = OFDM.NOTHING;

		while (r != OFDM.RCVEDDAT && time <= timeout_) {
		//while (r != Modulation.RCVEDDAT) {
			r = demodulator_.demodulate(double_q_.take());
			time += (r == OFDM.NOTHING)? 1:0;
		}
		i_stream = demodulator_.getPacket();
		if (i_stream.length == 0){
			// I suppose to get a full length packet, but something unexpected happened.
			System.out.println("No packet found, possibly time out when waiting for one.");
			return new byte[pack_size_];
		}
		// Initial read.
		crc8_.update(i_stream, 1, pack_size_-1);
		int pack_cnt = i_stream[1];
		if ((byte) crc8_.getValue() == i_stream[0]){
			System.out.printf("Packet #%4d received.\n", pack_cnt);
			i_stream[0] = 1;
		} else {
			// No useful byte in a broken pack.
			i_stream[0] = 0;
			System.out.printf("Packet #%4d receive failed. CRC8 checksum wrong.\n", pack_cnt);
		}
		crc8_.reset();
		return i_stream;
	}
	static public void main(String[] args) throws Exception{
		FileO o_file = new FileO("./O", FileO.TEXT01);

		double time_limit = 10;
		int file_length = 6250;

		Receiver receiver = new Receiver();

		receiver.startReceive();
		byte[] f = receiver.testReceive(file_length, time_limit);
		receiver.stopReceive();
		
		o_file.write(f, 0, f.length);

		CheckIO checker = new CheckIO();
		System.out.println(checker.summary());
	}
	private byte[] testReceive(int byte_cnt, double timeout) throws Exception{
		byte[] frame = new byte[byte_cnt];
		int start_pos;
		double start_time = System.nanoTime() / 1e9;
		while (System.nanoTime()/1e9 - start_time <= timeout){
			byte[] packet = receiveOnePacket();
			if (packet[0] == 0) { continue; }
			int pack_cnt = packet[1];	
			start_pos = pack_cnt * data_size_;
			for (int i=0; i<data_size_; i++){
				if (start_pos + i < byte_cnt){
					frame[start_pos + i] = packet[head_size_ + i];
				}
			}
		}
		return frame;
	}

}