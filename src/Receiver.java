import AcousticNetwork.FileO;
import AcousticNetwork.FileI;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundIO;

class Receiver{
	private FileO o_file_;
	// We use file input for now;
	private FileI i_file_;
	private CRC8 crc8_;
	private int pack_size_ = 16;
	private int head_size_ = 4;

	public Receiver() throws Exception{
		this("./O");
	}
	public Receiver(String file) throws Exception{
		i_file_ = new FileI("./mid", FileI.TEXT01);
		// No modulation, so no sound now. Using file out.
		o_file_ = new FileO(file, FileO.TEXT01);
		crc8_ = new CRC8(0x9c, (short) 0xff);
	}

	public void receive() throws Exception{
		int byte_read = pack_size_ - head_size_;
		byte[] i_stream = new byte[pack_size_];

		// Initial read.
		int r = i_file_.read(i_stream, 0, pack_size_);
		while (r > 0){
			crc8_.update(i_stream, 1, pack_size_-1);
			if ((byte) crc8_.getValue() == i_stream[0]){
				int useful_byte = i_stream[3];
				int pack_cnt = ((int)(i_stream[1]) << 8) + i_stream[2];
				o_file_.write(i_stream, head_size_, byte_read);
				System.out.printf("Packet #%3d received with %3d bytes in it.\n", pack_cnt, useful_byte);
			} else {
				System.out.println("A broken packet read. CRC8 checksum wrong.");
			}
			crc8_.reset();
			r = i_file_.read(i_stream, 0, pack_size_);
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
		receiver.receive();
	}
}