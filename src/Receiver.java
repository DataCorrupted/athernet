import AcousticNetwork.FileO;
import AcousticNetwork.FileI;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundIO;
import AcousticNetwork.Modulation;
import AcousticNetwork.CheckIO;

import java.util.concurrent.ArrayBlockingQueue;

class Receiver{
	private FileO o_file_;
	// We use file input for now;
	// private FileI i_file_;
	private CRC8 crc8_;
	private SoundIO i_sound_;
	private int pack_size_;
	private int head_size_ = 2;
	private int data_size_;
	private ArrayBlockingQueue<Double> double_q_;
	private Thread recorder_;
	private Modulation demodulator_;
	private int sample_rate_;
	private boolean file_stop_ = false;
	private boolean useful_packet = false;
	// After debug, delete it.
	private int last_pack = -1;
	public Receiver() throws Exception{
		this(44100, 16, 0.1, "./O");
	}
	public Receiver(int pack_size, String file) throws Exception{
		this(44100, pack_size, 0.1, file);
	}
	// buf_len: for how long(in seconds) should the buffer contain history sound data.
	public Receiver(
	  int sample_rate, int pack_size, double buf_len, String file) throws Exception{
	  	sample_rate_ = sample_rate;
		pack_size_ = pack_size;
		data_size_ = pack_size_ - head_size_;
		o_file_ = new FileO(file, FileO.TEXT01);
		crc8_ = new CRC8(0x9c, (short) 0xff);
		double_q_ = new ArrayBlockingQueue<Double>((int) (sample_rate * buf_len));
		i_sound_ = new SoundIO(sample_rate, double_q_);
		demodulator_ = new Modulation(sample_rate);
	}

	// This function should run in an independent thread.
	// Do not call it manually.
	private void receiveFromFile(String i_file) throws Exception{
		file_stop_ = false;
		double[] wave = i_sound_.read_file(i_file);
		int wait_time = 0; //(int) 1.0e4/sample_rate_;
		for (int i=0; i<wave.length; i++){
			double_q_.put(wave[i]);
			//System.out.println("\t" + double_q_.size() + " " + i );
			// Make sure that approx. sample_rate amount 
			// of data is put into the queue.
			// Turns out, there is no need to sleep at all.
			// Thread.sleep(0, wait_time);
		}
		// The file is over, in order to keep the queue moving
		// we stuck 0 to it.
		while (!file_stop_){
			double_q_.put(0.0);
		}
	}
	private void stopFileStream() { file_stop_ = true; }

	public void startReceive() throws Exception{		
		recorder_ = new Thread(i_sound_);
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
		double timeout = 
			demodulator_.getHeaderLength() + 
			demodulator_.getBitLength() * pack_size_ * 8;
		timeout *= 0.9;	// 20% extra waiting time.
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
		i_stream = demodulator_.getPacket();
		if (i_stream.length == 0){
			// I suppose to get a full length packet, but something unexpected happened.
			System.out.println("No packet found, possibly time out when waiting for one.");
			useful_packet = false;
			return new byte[pack_size_];
		}
		// Initial read.
		crc8_.update(i_stream, 1, pack_size_-1);
		int pack_cnt = i_stream[1];
		if ((byte) crc8_.getValue() == i_stream[0]){
			System.out.printf("Packet #%3d received.\n", pack_cnt);
			last_pack = pack_cnt;
			i_stream[0] = 1;
		} else {
			// No useful byte in a broken pack.
			i_stream[0] = 1;
			last_pack ++;
			i_stream[1] = (byte) (last_pack & 0xff);
			System.out.printf("Packet #%3d receive failed. CRC8 checksum wrong.\n", pack_cnt);
//			for (int i=0; i<pack_cnt, i++){
				//System.out.print()
//			}
		}
		crc8_.reset();
		return i_stream;
	}
	public byte[] receiveBytes(int byte_cnt, double timeout) throws Exception{
		byte[] chunk = new byte[byte_cnt];
		int start_pos;
		double start_time = System.nanoTime() / 1e9;
		while (System.nanoTime()/1e9 - start_time <= timeout){
			byte[] packet = receiveOnePacket();
			//if (packet[0] == 0) { continue; }
			int pack_cnt = packet[1];	
			start_pos = pack_cnt * data_size_;
			for (int i=0; i<data_size_; i++){
				if (start_pos + i < byte_cnt){
					chunk[start_pos + i] = packet[head_size_ + i];
//					if (pack_cnt == 0){
//						System.out.println((start_pos + i) + " " + chunk[start_pos + i] + " " + packet[head_size_ + i]);
//					}
				}
			}
		}
		return chunk;
	}
	static public void main(String[] args) throws Exception{
		String o_path="./O";
		String i_path_tmp="./I";
		boolean from_file = false;
		double time_limit = 15;
		int i=0;
		while (i<args.length){
			if (args[i].equals("-o")){
				i++;
				if (i == args.length){
					System.out.println("Error, no file provided.");
				} else {
					o_path = args[i];
				}
			} else if (args[i].equals("--from-file")){
				i++;
				if (i != args.length){
					i_path_tmp = args[i]; 
				}
				from_file = true;
			} else if (args[i].equals("--error-correction")) {
				time_limit = 30;
				System.out.println("Using error correction mode, listen for 30s");
			} else {
				System.out.println("Unrecognized command "+ args[i] + ", it will be ignored.");
			}
			i++;
		}
		Receiver receiver = new Receiver(16, o_path);
		byte[] f;
		if (!from_file){
			receiver.startReceive();
			f = receiver.receiveBytes(1250, time_limit);
			receiver.stopReceive();
		} else {
			final String i_path = i_path_tmp;
			Thread simu_receiver = new Thread( new Runnable(){
				public void run() { 
					try{ receiver.receiveFromFile(i_path);}
					catch (Exception e) {;}
			}});
			simu_receiver.start();
			f = receiver.receiveBytes(125, 1);
			receiver.stopFileStream();
			simu_receiver.join();
		}
		receiver.o_file_.write(f, 0, f.length);
		CheckIO checker = new CheckIO();
		System.out.println(checker.summary());
	}
}