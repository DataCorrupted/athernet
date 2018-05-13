package athernet.physical;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFileFormat.Type;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;
import java.nio.ByteBuffer;

import java.util.Vector;

public class SoundO {
	private final int FRAMESIZE = 2;
	private final int BYTESIZE = 8;

	private int sample_rate_;
	// The data format of the sound device.
	private AudioFormat format_;
	// Data line's information.
	private DataLine.Info o_info_;
	// Data line.
	private SourceDataLine o_line_;

	public SoundO() throws Exception{
		this(48000);
	}
	public SoundO(int sr) throws Exception{
		dvt_ = new Vector<Double>();
		sample_rate_ = sr;
		this.format_ = 
			new AudioFormat(sample_rate_, FRAMESIZE*BYTESIZE, 1, true, true);
		this.setUpDevice();		
	}
	private void setUpDevice() throws Exception{
		this.o_info_ = new DataLine.Info(SourceDataLine.class, format_, 480);
		
		if (!AudioSystem.isLineSupported(this.o_info_)){
			System.out.println(
				"Line matching " + this.o_info_ + " is not supported.");
			throw new LineUnavailableException();
		}
		this.o_line_ = (SourceDataLine)AudioSystem.getLine(o_info_);
		o_line_.open(this.format_);
		// Now this is disturbing. 
		// I should shut the lines down by calling close() in the
		// descructor, but funny thing is, there is no such a thing
		// in Java, as memory is managed by java, not the programmer.
		// this.o_line_.start();
	}

	// Given a ByteBuffer, play it.
	private void play(ByteBuffer out) throws LineUnavailableException {
		this.o_line_.start();
		// System.out.printf("[SoundO] SystemTime: %d\n",System.currentTimeMillis());
		this.o_line_.write(out.array(), 0, out.capacity());
		this.o_line_.drain();
		this.o_line_.stop();
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

	// Same as MATLAB sound.
	public void sound(double[] arr) throws Exception{
		play(doubleToByteBuf(arr));
	}

	public void saveToFile(double[] arr, String path) throws Exception{
		// Not sure why it uses input stream for output.
		ByteBuffer buf = doubleToByteBuf(arr);
		InputStream in = new ByteArrayInputStream(buf.array());
		// Streaming uses sample as counting unit.
		AudioInputStream stream = 
			new AudioInputStream(in, format_, buf.capacity()/FRAMESIZE);
		File f = new File(path);
		AudioSystem.write(stream, Type.WAVE, f);		
	}
	
	// Drain every data in the buffer before it's closed.
	public void drain(){ o_line_.drain();}

	public static void main(String[] args) throws Exception{
		SoundO o = new SoundO();
		double dur = 5;
		int sample_rate = 48000;
		int sample_cnt = (int) (dur * sample_rate);
		double[] wave = new double[sample_cnt];
		for (int i=0; i<sample_cnt; i++){
			double t = (double) i / sample_rate;
			// Weighted mean for better frequency responce.
			wave[i] = 
				0.3*Math.sin(2*Math.PI*1000*t) + 0.7*Math.sin(2*Math.PI*10000*t);
		}		
		o.sound(wave);
	}

	// You can save data to this vector and output them to a file.
	private Vector<Double> dvt_;

	// The conversion from double[] to Vector and from Vector to double[]
	// is not only ugly but also inefficient.
	// But since this is a debug utility, don'e care any more.
	public void saveData(double[] data){
		for (int i=0; i<data.length; i++){
			dvt_.add(data[i]);
		}
	}
	public double[] toArray(){
		double[] wave = new double[dvt_.size()];
		for (int i=0; i<dvt_.size(); i++){
			wave[i] = dvt_.get(i);
		}		
		return wave;
	}
	public void playPreserved() throws Exception{
		sound(toArray());
	}
	public void savePreservedToFile(String path) throws Exception{
		saveToFile(toArray(), path);
	}
	public void clearPreservedVector(){
		dvt_.clear();
	}

}

