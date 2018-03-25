import AcousticNetwork.SoundIO;

class SoundIOTest{

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
		SoundIO sound_io = new SoundIO();

		String path = "../wav/record1_41.wav";
		sound_io.play_file(path);
		//sound_io.sound(wave);
		wave = sound_io.record(100000);
		//sound_io.sound(wave);
		//sound_io.save_file(wave, "t.wav");
	}
}