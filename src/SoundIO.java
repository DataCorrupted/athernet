import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.nio.ByteBuffer;

class SoundIO{
	private int freq;

	public static void play(ByteBuffer out) throws InterruptedException, LineUnavailableException {

		AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		SourceDataLine line;
		if (!AudioSystem.isLineSupported(info)){
			System.out.println("Line matching " + info + " is not supported.");
			throw new LineUnavailableException();
		}

		line = (SourceDataLine)AudioSystem.getLine(info);
		line.open(format);  
		line.start();

		line.write(out.array(), 0, 22050);
		line.write(out.array(), 0, 22050);

		line.drain();                                         
		line.close();
	}

	// Try modify this to test.
	public static void main(String[] args) throws Exception{
		double dur = 0.5;
		int sample_rate = 44100;
		int sample_cnt = (int) (dur * sample_rate);
		System.out.println(Short.MAX_VALUE);
		ByteBuffer out = ByteBuffer.allocate(sample_cnt*2);
		for (int i=0; i<sample_cnt; i++){
			out.putShort((short)(Short.MAX_VALUE * Math.cos(2*Math.PI * 15000 * i/sample_rate)));
		}
		play(out);
	}
}