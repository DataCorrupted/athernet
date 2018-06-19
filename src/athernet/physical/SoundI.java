package athernet.physical;

import athernet.physical.SoundO;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.TargetDataLine;

import java.util.concurrent.ArrayBlockingQueue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SoundI implements Runnable {
	private final int FRAMESIZE = 2;
	private final int BYTESIZE = 8;

	private int sample_rate_;
	private AudioFormat format_;
	private DataLine.Info i_info_;
	private TargetDataLine i_line_;
	private ArrayBlockingQueue<Double> double_q_;
	private double avg_power_ = -1;
	private boolean stop_ = false;

	// Record from another thread.
	// Put all the data in the double_q_
	@Override
	public void run(){
		i_line_.start();
		int samples_per_read = 44;
		ByteBuffer in = ByteBuffer.allocate(samples_per_read);
		// Loops until someone interrupts this.
		while (!stop_){
			in.clear();
			i_line_.read(in.array(), 0, samples_per_read);
			double[] wave = byteBufToDouble(in);
			for (int i=0; i<wave.length; i++){
				// Update avg power.
				avg_power_ = avg_power_ * 23 / 24 + wave[i] * wave[i] / 24;
				while (!double_q_.offer(wave[i])){
					// Overflow.
					// System.err.println("Warning[SoundI.run()]: Bufferoverflowed, the latested data just been throwed.");
					// Retrive the oldest one from the queue,
					// Regardless the queue is empty or not.
					double_q_.poll();
				}
			}
		}
	}

	public double getPower(){ return avg_power_; }
	// Let's consider power 0.3 as the lowest power we consider as signal.
	public boolean hasSignal(){ return avg_power_ > 0.3; }

	// Have to use a flag to sign and stop the thread. 
	// There is a method called Thread.stop(), but it's 
	// depreciated, check for reasons here:
	// https://docs.oracle.com/javase/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
	public void stopConcurrentReadThread(){ stop_ = true; }

	public SoundI() throws Exception{
		this(48000);
	}
	public SoundI(int sr) throws Exception{
		sample_rate_ = sr;
		this.format_ = 
			new AudioFormat(sample_rate_, FRAMESIZE*BYTESIZE, 1, true, true);
		this.setUpDevice();		
	}
	// Offer a blocking queue for multi threading.
	public SoundI(int sr, ArrayBlockingQueue<Double> double_buf) throws Exception{
		this(sr);
		double_q_ = double_buf;
	}

	protected void setUpDevice() throws Exception{
		this.i_info_ = new DataLine.Info(TargetDataLine.class, format_);
		
		if (!AudioSystem.isLineSupported(this.i_info_)){
			System.err.println(
				"Line matching " + this.i_info_ + " is not supported.");
			throw new LineUnavailableException();
		}
		this.i_line_ = (TargetDataLine)AudioSystem.getLine(i_info_);
		i_line_.open(this.format_);
		i_line_.start();
		// Now this is disturbing. 
		// I should shut the lines down by calling close() in the
		// destructor, but funny thing is, there is no such a thing
		// in Java, as memory is managed by Java, not the programmer.
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

	public double[] record(int sample_cnt){
		if (sample_cnt < 200) { 
			System.err.println(
				"Warning[SoundI.record(int)]: Too few samples to record. "+
				"Did you mean " + sample_cnt + " seconds or " +
				+sample_cnt + " samples? To record for certain duration, use SoundI.record(double)");
		}
		int byte_cnt = sample_cnt * this.FRAMESIZE;
		ByteBuffer in = ByteBuffer.allocate(byte_cnt);
		i_line_.start();
		System.err.printf(
			"Recording for %3.2fs...\n",
			(double) sample_cnt / sample_rate_);
		i_line_.read(in.array(), 0, byte_cnt);
		System.err.println("Recording finished.");
		i_line_.close();
		return byteBufToDouble(in);
	}

	public double[] record(double time) throws Exception{
		return this.record((int)(time * sample_rate_));
	}

	public double[] readFile(String path)
		throws UnsupportedAudioFileException, IOException, LineUnavailableException{
		File f = new File(path);
		AudioInputStream src = AudioSystem.getAudioInputStream(f);
		
		AudioFormat src_format = src.getFormat();
		AudioInputStream dst = AudioSystem.getAudioInputStream(this.format_, src);
		int byte_cnt = (int) dst.getFrameLength() * FRAMESIZE;
		ByteBuffer buf = ByteBuffer.allocate(byte_cnt);
		
		int r = dst.read(buf.array(), 0, byte_cnt);
		System.err.printf(
			"%d samples(%3.2fs) read from file.\n",
			r / FRAMESIZE, 
			(double)r / sample_rate_ / FRAMESIZE);
		return byteBufToDouble(buf);
	}
	public static void main(String[] args) throws Exception{
		SoundI i = new SoundI();
		SoundO o = new SoundO();
		double[] wave = i.record(5.0);
		o.sound(wave);
	}
}

