package AcousticNetwork;

import AcousticNetwork.FileI;
import AcousticNetwork.CRC8;
import AcousticNetwork.SoundO;
import AcousticNetwork.OFDM;

class Transmitter{
	// Add given data's crc sum.
	private CRC8 crc8_ ;

	// Modulate given data.
	private OFDM modulator_;
	
	// Transmit the sound.
	private SoundO o_sound_;
	
	public Transmitter() throws Exception{
		this(44100);
	}

	// TODO: depreciate packet_size once ernest finishes.
	public Transmitter(int sample_rate) throws Exception{
		o_sound_ = new SoundO(sample_rate);
		crc8_ = new CRC8(0x9c, (short) 0xff);
		modulator_ = new OFDM(44100, 1000, 1000, 8);
	}

	public void transmitOnePack(byte[] out) throws Exception{
		// Add checksum
		crc8_.update(out, 1, out.length-1);
		out[0] = (byte) crc8_.getValue();
		// Modulation
		double[] wave = modulator_.modulate(out);
		// Sound
		o_sound_.sound(wave);
		// Reset CRC8
		crc8_.reset();	
	}

	static public void main(String[] args) throws Exception{
		FileI i_file_ = new FileI("./I", FileI.TEXT01);
		
		int head_size = 2;
		int pack_size = 128;
		int byte_read = pack_size - head_size;

		Transmitter transmitter = new Transmitter();

		byte[] o_stream = new byte[pack_size];

		int r = 0;
		short pack_cnt = 0;

		double start_time = System.nanoTime() / 1e9;
		r = i_file_.read(o_stream, head_size, byte_read);
		while (r != -1){
			o_stream[1] = (byte) (pack_cnt & 0xff);
			transmitter.transmitOnePack(o_stream);
			// Read next bunch of data.
			pack_cnt ++;
			r = i_file_.read(o_stream, head_size, byte_read);
		}
		
		transmitter.o_sound_.drain();
		double end_time = System.nanoTime() / 1e9;

		System.out.println("Time used for transmition: " + (end_time - start_time));
	}
}