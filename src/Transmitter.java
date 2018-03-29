import AcousticNetwork.FileI;
import AcousticNetwork.FileO;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundIO;
class Transmitter{
	// packet size no more than 255.
	private int pack_size_ = 16;
	private int head_size_ = 4;
	private FileI f_in_;
	private FileO f_out_;
	private CRC8 crc8_ ;
	public Transmitter() throws Exception{
		this("./I");
	}
	public Transmitter(String file) throws Exception{
		f_in_ = new FileI(file, FileI.TEXT01);
		// No modulation, so no sound now. Using file out.
		f_out_ = new FileO("./mid", FileO.TEXT01);
		crc8_ = new CRC8(0x9c, (short) 0xff);
	}
	public void transmit() throws Exception{
		int byte_read = pack_size_ - head_size_;
		byte[] o_stream = new byte[pack_size_];


		short pack_cnt = 0;
		// Initial read.
		int r = f_in_.read(o_stream, head_size_, byte_read);
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
			// Sound
			f_out_.write(o_stream, 0, pack_size_);
			// Read next bunch of data.
			pack_cnt ++;
			r = f_in_.read(o_stream, head_size_, byte_read);
			// Reset CRC8
			crc8_.reset();
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
		}
		Transmitter transmitter = new Transmitter(file);
		transmitter.transmit();
	}
}