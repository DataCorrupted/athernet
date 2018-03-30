import AcousticNetwork.FileO;
import AcousticNetwork.FileI;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundIO;
import AcousticNetwork.Modulation;

import java.util.concurrent.ArrayBlockingQueue;

class Receiver{
	private FileO o_file_;
	// We use file input for now;
	// private FileI i_file_;
	private CRC8 crc8_;
	private SoundIO i_sound_;
	private int pack_size_;
	private int head_size_;
	private int data_size_;
	private ArrayBlockingQueue<Double> double_q_;
	private Thread recorder_;

	public Receiver() throws Exception{
		this(44100, 16, 0.1, "./O");
	}
	public Receiver(String file) throws Exception{
		this(44100, 16, 0.1, file);
	}
	// buf_len: for how long(in seconds) should the buffer contain history sound data.
	public Receiver(
	  int sample_rate, int pack_size, double buf_len, String file) throws Exception{
		pack_size_ = pack_size;
		data_size_ = pack_size_ - head_size_;
		o_file_ = new FileO(file, FileO.TEXT01);
		crc8_ = new CRC8(0x9c, (short) 0xff);
		double_q_ = new ArrayBlockingQueue<Double>((int) (sample_rate * buf_len));
		i_sound_ = new SoundIO(sample_rate, double_q_);
		demodulator = new Modulation(sample_rate);
	}

	public void startReceive(){		
		recorder_ = new Thread(i_sound_);
		recorder_.start();

	}
	public void stopReceive(){
		i_sound_.stopConcurrentReadThread();
		recorder_.join();
	}

	public byte[] receiveOnePacket() throws Exception{
		int byte_read = data_size_;
		byte[] i_stream = new byte[pack_size_];

		// Offer double to demodulate until a packet is offered.
		while (!demodulator.demolation(double_q_.take(), pack_size_)) {; }
		i_stream = demolator.getPacket();
		if (i_stream.length == 0){
			// I suppose to get a full length packet, but something unexpected happened.
			return i_stream;
		}
		// Initial read.
		crc8_.update(i_stream, 1, pack_size_-1);
		if ((byte) crc8_.getValue() == i_stream[0]){
			int useful_byte = i_stream[3];
			int pack_cnt = ((int)(i_stream[1]) << 8) + i_stream[2];
			System.out.printf("Packet #%3d received with %3d bytes in it.\n", pack_cnt, useful_byte);
		} else {
			System.out.println("A broken packet read. CRC8 checksum wrong.");
		}
		crc8_.reset();
		return i_stream;
	}
	public byte[] receiveBytes(int len){
		int k = 0;
		byte[] chunk = new byte[len];
		int start_pos;
		while (k<len){
			byte[] packet = receiveOnePacket();
			int useful_byte = i_stream[3];
			int pack_cnt = ((int)(i_stream[1]) << 8) + i_stream[2];
			k += data_size_;			
			start_pos = pack_cnt * (data_size_);
			for (int i=0; i<useful_byte; i++){
				if (start_pos + i < len){
					chunk[start_pos + i] = packet[head_size_ + i];
				}
			}
		}
	}
	static public void main(String[] args) throws Exception{
		String file="./O";
		int i=0;
		while (i<args.length){
			if (args[i].equals("-f")){
				i++;
				if (i == args.length){
					System.out.println("Error, no file provided.");
				} else {
					file = args[i];
				}
			} else {
				System.out.println("Unrecognized command "+ args[i] + ", it will be ignored.");
			}
		}
		Receiver receiver = new Receiver();
		receiver.receiveBytes(4000);
	}
}