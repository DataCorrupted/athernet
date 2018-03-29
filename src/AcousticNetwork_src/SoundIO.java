package AcousticNetwork;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFileFormat.Type;

import java.util.concurrent.ArrayBlockingQueue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SoundIO implements Runnable {
	private int sample_rate_;
	private AudioFormat format_;
	private DataLine.Info o_info_;
	private SourceDataLine o_line_;
	private DataLine.Info i_info_;
	private TargetDataLine i_line_;
	private final int FRAMESIZE = 2;
	private final int BYTESIZE = 8;
	private ArrayBlockingQueue<Double> double_buf_;
	private boolean stop_ = false;

	@Override
	public void run(){
		i_line_.start();
		int samples_per_bit = 44;
		ByteBuffer in = ByteBuffer.allocate(samples_per_bit);
		double[] wave;
		// Loops until someone interrupts this.
		while (!stop_){
			in.clear();
			i_line_.read(in.array(), 0, samples_per_bit);
			wave = byteBufToDouble(in);
			for (int i=0; i<samples_per_bit; i++){
				while (!double_buf_.offer(wave[i])){
					// Retrive the oldest one from the queue,
					// Regardless the queue is empty or not.
					double_buf_.poll();
				}
			}
		}
	}

	// Have to use a flag to sign and stop the thread. 
	// There is a method called Thread.stop(), but it's 
	// depreciated, check for reasons here:
	// https://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
	public void stopConcurrentReadThread(){ stop_ = true; }

	public SoundIO(int sr, ArrayBlockingQueue<Double> double_buf) throws Exception{
		this(sr);
		double_buf_ = double_buf;
	}
	public SoundIO() throws Exception{
		this(44100);
	}
	public SoundIO(int sr) throws Exception{
		sample_rate_ = sr;
		this.format_ = 
			new AudioFormat(sample_rate_, FRAMESIZE*BYTESIZE, 1, true, true);
		this.setUpDevice();		
	}
	protected void setUpDevice() throws Exception{
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
		
		//i_line_.start();
		//o_line_.start();
		// Now this is disturbing. 
		// I should shut the lines down by calling close() in the
		// descructor, but funny thing is, there is no such a thing
		// in Java, as memory is managed by java, not the programmer.
	}
	private ByteBuffer doubleToByteBuf(double[] arr) throws Exception{
		int sampele_cnt = arr.length;
		ByteBuffer out = ByteBuffer.allocate(sampele_cnt * FRAMESIZE);
		for (int i=0; i<sampele_cnt; i++){
			if (Math.abs(arr[i]) > 1){
				arr[i] = (arr[i] < 0) ? -1:1;
				System.out.printf("Warn on data frame %d, value too large, fixed to MAX value\n", i);
			}
			out.putShort((short) (Short.MAX_VALUE * arr[i]));
		}
		return out;
	}
	private double[] byteBufToDouble(ByteBuffer buf){
		int cap = buf.capacity();
		int sample_cnt = cap / FRAMESIZE;
		double[] out = new double[sample_cnt];
		// Two bytes each time.
		for (int i=0; i < sample_cnt; i++){
			out[i] = (double) buf.getShort()/ Short.MAX_VALUE;
		}	
		return out;	
	}

	public void sound(double[] arr) throws Exception{
		play(doubleToByteBuf(arr));
	}
	private void play(ByteBuffer out) throws LineUnavailableException {
		this.o_line_.start();
		this.o_line_.write(out.array(), 0, out.capacity());
		// Drain every data in the buffer before it's closed.
		this.o_line_.drain();
		this.o_line_.close();
	}
	public double[] record(int sample_cnt){
		int byte_cnt = sample_cnt * this.FRAMESIZE;
		ByteBuffer in = ByteBuffer.allocate(byte_cnt);
		i_line_.start();
		System.out.printf(
			"Recording for %3.2fs...\n",
			(double) sample_cnt / sample_rate_);
		i_line_.read(in.array(), 0, byte_cnt);
		System.out.println("Recording finished.");
		i_line_.close();
		return byteBufToDouble(in);
	}

	public double[] record(double time) throws Exception{
		return this.record((int)(time * sample_rate_));
	}

	public double[] read_file(String path)
		throws UnsupportedAudioFileException, IOException, LineUnavailableException{
		File f = new File(path);
		AudioInputStream src = AudioSystem.getAudioInputStream(f);
		
		AudioFormat src_format = src.getFormat();
		AudioInputStream dst = AudioSystem.getAudioInputStream(this.format_, src);

		int byte_cnt = (int) dst.getFrameLength() / src_format.getFrameSize() * FRAMESIZE;
		ByteBuffer buf = ByteBuffer.allocate(byte_cnt);
		
		int r = dst.read(buf.array(), 0, byte_cnt);
		System.out.printf(
			"%d samples(%3.2fs) read from file.\n",
			r / FRAMESIZE, 
			(double)r / sample_rate_ / FRAMESIZE);
		return byteBufToDouble(buf);
	}
	public void save_file(double[] arr, String path) throws Exception{
		// Not sure why it uses input stream for output.
		ByteBuffer buf = doubleToByteBuf(arr);
		//this.play(buf);
		InputStream in = new ByteArrayInputStream(buf.array());

		// Streaming uses sample as counting unit.
		AudioInputStream stream = 
			new AudioInputStream(in, format_, buf.capacity()/FRAMESIZE);
		File f = new File(path);
		AudioSystem.write(stream, Type.WAVE, f);		
	}

}

