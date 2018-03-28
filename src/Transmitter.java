import AcousticNetwork.FileI;
import AcousticNetwork.FileO;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundIO;
class Transmitter{
	private int pack_size_ = 16;
	private FileI f_in_;
	private FileO f_out_;
	private CRC8 crc8_ ;
	public Transmitter() throws Exception{
		this("./I");
	}
	public Transmitter(String file) throws Exception{
		f_in_ = new FileI(file, FileI.TEXT01);
		// No modulation, so no sound now. Using file out.
		f_out_ = new FileO("./tmp", FileO.TEXT01);
		crc8_ = new CRC8(0x9c, (short) 0xff);
	}
	public void transmit() throws Exception{
		int byte_read = pack_size_ - 2;
		byte[] i_stream = new byte[byte_read];
		byte[] o_stream = new byte[pack_size_];

		// Initial read.
		int r = f_in_.getBytes(i_stream);
		while (r != -1){
			
			// Record total bytes in this packet.
			o_stream[1] = (byte) r;
			// Copy all the data in.
			System.arraycopy(i_stream, 0, o_stream, 2, r);
			// Add checksum
			crc8_.update(o_stream, 1, pack_size_-1);
			o_stream[0] = (byte) crc8_.getValue();
			// Modulation
			// Sound

			f_out_.putBytes(o_stream, pack_size_);
			// Read next bunch of data.
			i_stream = new byte[byte_read];
			r = f_in_.getBytes(i_stream);
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