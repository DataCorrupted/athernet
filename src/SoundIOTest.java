import AcousticNetwork.SoundIO;

class SoundIOTest{

	private static void testSin(SoundIO io) throws Exception{
		double dur = 4;
		int sample_rate = 44100;
		int sample_cnt = (int) (dur * sample_rate);
		double[] wave = new double[sample_cnt];
		for (int i=0; i<sample_cnt; i++){
			double t = (double) i / sample_rate;
			wave[i] = 
				0.5*(Math.sin(2*Math.PI*1000*t) + Math.sin(2*Math.PI*10000*t));
		}		
		io.sound(wave);
	}

	private static double[] testReadFile(SoundIO io) throws Exception{
		String path = "../wav/record1_41.wav";
		return io.read_file(path);
	}

	private static void testRecordAndPlayback(SoundIO io, double dur) throws Exception{
		double[] wave = io.record(dur);
		io.sound(wave);
	}

	private static void testSaveFile(SoundIO io) throws Exception{
		double[] wave = testReadFile(io);
		io.save_file(wave, "./test.wav");
	}

	// Try modify this to test.
	public static void main(String[] args) throws Exception{

		SoundIO sound_io = new SoundIO();
	}
}