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

	// This function should run in an independent thread.
	// Do not call it manually.
	private void receiveFromFile(String i_file){
		double[] wave = io.read_file(path);
		for (int i=0; i<wave.length; i++){
			double_q_.offer(wave[i]);
			// Make sure that approx. sample_rate amount 
			// of data is put into the queue.
			Thread.sleep(0, 1.0e9/sample_rate);
		}
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

		int time = 0;
		double timeout = 
			demodulator.getHeaderLength() + 
			demodulator.getBitLength() * pack_size_ * 8;
		timeout *= 1.2;	// 20% extra waiting time.
		// Offer double to demodulate until a packet is offered.
		// I think a better way is to let demodulate tell me what it's seeing
		// Whether the header is matched then I wait for longer,
		// or it's receiving nothing, then I timeout.
		 
		while (r != Modulation.RCVEDDAT && time <= timeout) {
			r = demodulator.demodulation(double_q_.take(), pack_size_);
			time += (r == Modulation.NOTHING)? 1:0
		}
		
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
	public byte[] receiveBytes(int byte_cnt, int pack_cnt){
		int k = 0;
		byte[] chunk = new byte[byte_cnt];
		int start_pos;
		while (k<pack_cnt){
			byte[] packet = receiveOnePacket();
			int useful_byte = i_stream[3];
			int pack_cnt = ((int)(i_stream[1]) << 8) + i_stream[2];		
			start_pos = pack_cnt * data_size_;
			for (int i=0; i<useful_byte; i++){
				if (start_pos + i < byte_cnt){
					chunk[start_pos + i] = packet[head_size_ + i];
				}
			}
			k ++;
		}
	}
	static public void main(String[] args) throws Exception{
		String o_file="./O";
		String i_file="./I";
		boolean from_file = false;
		int i=0;
		while (i<args.length){
			if (args[i].equals("-o")){
				i++;
				if (i == args.length){
					System.out.println("Error, no file provided.");
				} else {
					o_file = args[i];
				}
			} else if (args[i].equals("--from-file")){
				i++;
				if (i != args.length){
					i_file = args[i]; 
				}
				from_file = true;
			} else {
				System.out.println("Unrecognized command "+ args[i] + ", it will be ignored.");
			}
			i++;
		}
		Receiver receiver = new Receiver();
		if (!from_file){
			receiver.startReceive();
			receiver.receiveBytes(125, 11);
			receiver.stopReceive();
		} else {
			Thread simu_receiver = new Thread( new Runnable(){
				public void run() { receiver.receiveFromFile();}
			});
			simu_receiver.start();
			receive.receiveBytes(125, 11);
			simu_receiver.join();
		}
	}
}