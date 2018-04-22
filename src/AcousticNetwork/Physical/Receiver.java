package AcousticNetwork.Physical;

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
	private int head_size_ = 2;

	public Receiver() throws Exception{
		this(44100, 0.1, 5000);
	}
	public Receiver(double timeout) throws Exception{
		this(44100, 0.1, timeout);
	}

	// buf_len: for how long(in seconds) should the buffer contain history sound data.
	public Receiver(
	  int sample_rate, double buf_len, double timeout) throws Exception{
	  	sample_rate_ = sample_rate;

		double_q_ = new ArrayBlockingQueue<Double>((int) (sample_rate * buf_len));

		i_sound_ = new SoundI(sample_rate, double_q_);		
		recorder_ = new Thread(i_sound_);

		demodulator_ = new OFDM(44100, 1000, 1000, 8);

		crc8_ = new CRC8(0x9c, (short) 0xff);
		
		timeout_ = timeout;
	}
	public void startReceive() throws Exception{	
		recorder_.start();
	}
	public void stopReceive() throws Exception{
		i_sound_.stopConcurrentReadThread();
		recorder_.join();
	}

	public byte[] receiveOnePacket() throws Exception{
		int time = 0;
		// Offer double to demodulate until a packet is offered.
		// I think a better way is to let demodulate tell me what it's seeing
		// Whether the header is matched then I wait for longer,
		// or it's receiving nothing, then I timeout.
		int r = OFDM.NOTHING;

		while (r != OFDM.RCVEDDAT && time <= timeout_) {
			r = demodulator_.demodulate(double_q_.take());
			time += (r == OFDM.NOTHING)? 1:0;
		}
		byte[] i_stream = demodulator_.getPacket();
		if (i_stream.length == 0){
			// I suppose to get a full length packet, but something unexpected happened.
			System.out.println("No packet found, possibly time out when waiting for one.");
			return new byte[0];
		}
		// Initial read.
		crc8_.update(i_stream, 1, i_stream.length-1);
		if ((byte) crc8_.getValue() == i_stream[0]){
			System.out.printf("Packet #%4d received.\n", i_stream[1]);
			i_stream[0] = 1;
		} else {
			// No useful byte in a broken pack.
			i_stream = new byte[0];
			System.out.printf("Failed to receive packet. CRC8 checksum wrong.\n");
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
			if (packet.length == 0) { continue; }
			int pack_cnt = packet[1];	
			int data_size = packet.length - head_size_;
			start_pos = pack_cnt * data_size;
			for (int i=0; i<data_size; i++){
				if (start_pos + i < byte_cnt){
					frame[start_pos + i] = packet[head_size_ + i];
				}
			}
		}
		return frame;
	}

}