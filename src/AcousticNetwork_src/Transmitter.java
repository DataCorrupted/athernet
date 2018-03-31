package AcoutsticNetwork;

import AcousticNetwork.FileO;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundO;
import AcousticNetwork.Modulation;
class Transmitter{
	// packet size no more than 128(byte).
	private int pack_size_;
	// The first 4 byte of a packet is header.
	// pack[0] = crc8
	// pack[1] = Packet label
	// pack[2~16] = Data;
	private int head_size_ = 2;
	private int data_size_;
	private FileI i_file_;
	//private FileO o_file_;
	private SoundO o_sound_;
	private CRC8 crc8_ ;
	private Modulation modulator_;
	public Transmitter() throws Exception{
		this(44100, 16);
	}
	public Transmitter(int pack_size) throws Exception{
		this(44100, pack_size);
	}
	public Transmitter(
	  int sample_rate, int packet_size) throws Exception{
	  	pack_size_ = packet_size;
	  	data_size_ = pack_size_ - head_size_;
		o_sound_ = new SoundO(sample_rate);
		crc8_ = new CRC8(0x9c, (short) 0xff);
		modulator_ = new Modulation(sample_rate);
	}
	public void transmitOnePack(byte[] out) throws Exception{
		if (out.length != pack_size_){
			System.out.println(
				"Warnin[Transmitter.transmitOnePack(byte[])]:" +
				"given data insufficient.");
			return;
		}
		// Add checksum
		crc8_.update(out, 1, pack_size_-1);
		out[0] = (byte) crc8_.getValue();
		// Modulation
		wave = modulator_.modulate(out);
		// Sound
		// You can pre save all the std wav through this. 
		// But I will develop this when we have the need.
		// o_sound_.saveData(wave);
		o_sound_.sound(wave);
		// Reset CRC8
		crc8_.reset();	
	}

	static public void main(String[] args) throws Exception{
		// Pack# 0: ..Hello wrold.
		byte[] out = {
			0x00, 0x00, 0x2e, 0x2e, 0x48, 0x65, 0x6c, 0x6c,
			0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64, 0x2e
		};
		Transmitter transmitter = new Transmitter(16, file);
		transmitter.transmitOnePack(out);
		transmitter.o_sound_.drain();
	}
}