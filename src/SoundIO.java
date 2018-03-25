import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFileFormat.Type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

class SoundIO{
	private int sample_rate_;
	private AudioFormat format_;
	private DataLine.Info o_info_;
	private SourceDataLine o_line_;
	private DataLine.Info i_info_;
	private TargetDataLine i_line_;
	private final int FRAMESIZE = 2;
	private final int BYTESIZE = 8;

	public SoundIO(int sr) throws LineUnavailableException{
		this.sample_rate_ = sr;
		this.format_ = 
			new AudioFormat(sample_rate_, FRAMESIZE * BYTESIZE, 1, true, true);

		this.o_info_ = new DataLine.Info(SourceDataLine.class, format_);
		this.i_info_ = new DataLine.Info(TargetDataLine.class, format_);
		
		if (!AudioSystem.isLineSupported(this.o_info_)){
			System.out.println(
				"Line matching " + this.o_info_ + " is not supported.");
			throw new LineUnavailableException();
		}
		this.o_line_ = (SourceDataLine)AudioSystem.getLine(o_info_);

		if (!AudioSystem.isLineSupported(this.i_info_)){
			System.out.println(
				"Line matching " + this.o_info_ + " is not supported.");
			throw new LineUnavailableException();
		}
		this.i_line_ = (TargetDataLine)AudioSystem.getLine(i_info_);
		o_line_.open(this.format_);
		i_line_.open(this.format_);
		
		i_line_.start();
		o_line_.start();

		// Now this is disturbing. 
		// I should shut the lines down by calling close() in the
		// descructor, but funny thing is, there is no such a thing
		// in Java, as memory is managed by java, not the programmer.
	}
	public ByteBuffer doubleToByteBuf(double[] arr) throws Exception{
		int len = arr.length;
		ByteBuffer out = ByteBuffer.allocate(len * FRAMESIZE);
		for (int i=0; i<len; i++){
			if (Math.abs(arr[i]) > 1){
				arr[i] = (arr[i] < 0) ? -1:1;
				System.out.printf("Warn on data frame %d, value too large, fixed to MAX value\n", i);
			}
			out.putShort((short) (Short.MAX_VALUE * arr[i]));
		}
		return out;
	}
	public double[] byteBufToDouble(ByteBuffer buf){
		int cap = buf.capacity();
		double[] out = new double[cap];
		for (int i=0; i < cap / FRAMESIZE; i++){
			out[i] = (double) buf.getShort() / Short.MAX_VALUE;
		}	
		return out;	
	}

	public void sound(double[] arr) throws Exception{
		play(doubleToByteBuf(arr));
	}
	private void play(ByteBuffer out) throws LineUnavailableException {
		o_line_.write(out.array(), 0, out.capacity());
		// Drain every data in the buffer before it's closed.
		o_line_.drain();
	}
	public double[] record(int sample_cnt){
		int byte_cnt = sample_cnt * this.FRAMESIZE;
		ByteBuffer in = ByteBuffer.allocate(byte_cnt);

		System.out.println("Recording...");
		i_line_.read(in.array(), 0, byte_cnt);
		System.out.println("Record finished.");

		return byteBufToDouble(in);
	}

	public double[] record(double time) throws Exception{
		return this.record((int)(time * sample_rate_));
	}

	public void play_file(String path)
		throws UnsupportedAudioFileException, IOException, LineUnavailableException{
		File f = new File(path);
		AudioInputStream src = AudioSystem.getAudioInputStream(f);
		AudioFormat src_format = src.getFormat();
		AudioInputStream dst = AudioSystem.getAudioInputStream(this.format_, src);

		int byte_cnt = (int) dst.getFrameLength() / src_format.getFrameSize() * FRAMESIZE;

		ByteBuffer buf = ByteBuffer.allocate(byte_cnt);
		int r = dst.read(buf.array(), 0, byte_cnt);
		System.out.printf(
			"%d samples(%4.3fs) read from file, now playing...",
			r / FRAMESIZE, 
			(double)r / sample_rate_ / FRAMESIZE);
		play(buf);
	}
	public void save_file(double[] arr, String path) throws Exception{
		// Not sure why it uses input stream for output.
		ByteBuffer buf = doubleToByteBuf(arr);
		InputStream in = new ByteArrayInputStream(buf.array());
		AudioInputStream stream = new AudioInputStream(in, format_, buf.capacity());
		File f = new File(path);
		AudioSystem.write(stream, Type.WAVE, f);		
	}

	// Try modify this to test.
	public static void main(String[] args) throws Exception{
		double dur = 4;
		int sample_rate = 44100;
		int sample_cnt = (int) (dur * sample_rate);
		double[] wave = new double[sample_cnt];
		for (int i=0; i<sample_cnt; i++){
			double t = (double) i / sample_rate;
			wave[i] = 
				0.5*(Math.sin(2*Math.PI*1000*t) + Math.sin(2*Math.PI*10000*t));
		}
		SoundIO sound_io = new SoundIO(sample_rate);

		//String path = "../wav/record1_41.wav";
		//sound_io.play_file(path);
		//sound_io.sound(wave);
		//wave = sound_io.record(100000);
		//sound_io.sound(wave);
		sound_io.save_file(wave, "t.wav");
	}
}