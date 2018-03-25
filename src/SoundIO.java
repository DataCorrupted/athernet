import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

class SoundIO{
	private int sample_rate_;
	private AudioFormat format_;
	private DataLine.Info o_info_;
	private SourceDataLine o_line_;
	private final int FRAMESIZE = 2;
	private final int BYTESIZE = 8;

	public SoundIO(int sr) throws LineUnavailableException{
		this.sample_rate_ = sr;
		this.format_ = new AudioFormat(44100, FRAMESIZE * BYTESIZE, 1, true, true);
		this.o_info_ = new DataLine.Info(SourceDataLine.class, format_);
		if (!AudioSystem.isLineSupported(this.o_info_)){
			System.out.println(
				"Line matching " + this.o_info_ + " is not supported.");
			throw new LineUnavailableException();
		}
		this.o_line_ = (SourceDataLine)AudioSystem.getLine(this.o_info_);
	}


	private void play(ByteBuffer out) throws LineUnavailableException {
		o_line_.open(this.format_);
		o_line_.start();
		o_line_.write(out.array(), 0, out.capacity());
		// Drain every data in the buffer before it's closed.
		o_line_.drain();
		// Close it.
		o_line_.close();
	}

	public void play_file(String path)
		throws UnsupportedAudioFileException, IOException, LineUnavailableException{
		File f = new File(path);
		AudioInputStream src = AudioSystem.getAudioInputStream(f);
		AudioFormat src_format = src.getFormat();
		AudioInputStream dst = AudioSystem.getAudioInputStream(this.format_, src);

		int byte_cnt = (int) dst.getFrameLength() / src_format.getFrameSize() * FRAMESIZE;

		byte[] bs = new byte[byte_cnt];
		int r = dst.read(bs, 0, byte_cnt);
		
		ByteBuffer out = ByteBuffer.wrap(bs);

		play(out);

	}

	// Try modify this to test.
	public static void main(String[] args) throws Exception{
		double dur = 10;
		int sample_rate = 44100;
		int sample_cnt = (int) (dur * sample_rate);
		ByteBuffer out = ByteBuffer.allocate(sample_cnt*2);
		for (int i=0; i<sample_cnt; i++){
			double t = (double) i / sample_rate;
			double data = 
				Math.sin(2*Math.PI*1000*t) + Math.sin(2*Math.PI*10000*t);
			out.putShort((short)(Short.MAX_VALUE * data));
		}
		SoundIO sound_io = new SoundIO(sample_rate);

		String path = "../wav/record1_41.wav";
		System.out.println(path);
		//sound_io.play_file(path);
		
		sound_io.play(out);
	}
}