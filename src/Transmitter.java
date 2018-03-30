import AcousticNetwork.FileI;
import AcousticNetwork.FileO;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundIO;
import AcousticNetwork.Modulation;
class Transmitter{
	// packet size no more than 128(byte).
	private int pack_size_;
	// The first 4 byte of a packet is header.
	// pack[0] = crc8
	// pack[1~2] = packet label(Short)
	// pack[3] = byte count in this packet.
	private int head_size_ = 4;
	private FileI i_file_;
	//private FileO o_file_;
	private SoundIO o_sound_;
	private CRC8 crc8_ ;
	private Modulation modulator_;
	public Transmitter() throws Exception{
		this(44100, 16, "./I");
	}
	public Transmitter(String file) throws Exception{
		this(44100, 16, file);
	}
	public Transmitter(
	  int sample_rate, int packet_size, String file) throws Exception{
	  	pack_size_ = packet_size;
		i_file_ = new FileI(file, FileI.TEXT01);
		// No modulation, so no sound now. Using file out.
		// o_file_ = new FileO("./mid", FileO.TEXT01);
		o_sound_ = new SoundIO(sample_rate);
		crc8_ = new CRC8(0x9c, (short) 0xff);
		modulator_ = new Modulation(sample_rate);
	}
	// Currently it transmits a whole file. 
	// Let's finish this project first and then we can
	// Talk about changes.
	public void transmit() throws Exception{
		int byte_read = pack_size_ - head_size_;
		byte[] o_stream = new byte[pack_size_];
		double[] wave;
		short pack_cnt = 0;
		// Initial read.
		int r = i_file_.read(o_stream, head_size_, byte_read);
		while (r != -1){
			o_stream[1] = (byte) (pack_cnt >>> 8);
			o_stream[2] = (byte) (pack_cnt & 0xff);
			// Record total bytes in this packet.
			o_stream[3] = (byte) r;
			// Copy all the data in.
			// Add checksum
			crc8_.update(o_stream, 1, pack_size_-1);
			o_stream[0] = (byte) crc8_.getValue();
			// Modulation
			wave = modulator_.modulate(o_stream);
			// Sound
			o_sound_.saveData(wave);
			o_sound_.sound(wave);
			// Read next bunch of data.
			pack_cnt ++;
			r = i_file_.read(o_stream, head_size_, byte_read);
			// Reset CRC8
			crc8_.reset();
			Thread.sleep(10);
		}
	}
	static public void main(String[] args) throws Exception{
		String file="./I";
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
			i++;
		}
		Transmitter transmitter = new Transmitter(file);
		transmitter.transmit();
		transmitter.o_sound_.drain();
		transmitter.o_sound_.saveDataToFile("./std_output.wav");
	}
}