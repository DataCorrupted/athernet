package AcousticNetwork;

class OFDM{
	public final double MINFREQ = 20;
	public final double MAXFREQ = 20000;

	// Total number of channels.
	private int channel_cnt_;

	// Bandwidth.
	private double bandwidth_;

	// Starting frequency.
	private double freq_;
	
	// An array for each carrier's frequency.
	private double[] freq_arr_;

	// For each channel, we generate a carrier with length bit_length_;
	private double[][] carrier_arr_;
	
	// Should be 44100 for performance.
	private int sample_rate_;

	// How much samples for a bit.
	private int bit_lenght_;

	// The length (in bits) of a package.
	private int pack_len_;

	public OFDM(){
		this(44100, 1000, 3000, 4, 44);
	}
	public OFDM(int sample_rate, double f, double b, int c, int bit_lenght, int pack_length){
		pack_len_ = pack_length;
		bit_len_ = bit_lenght;
		sample_rate_ = sample_rate;
		freq_ = f;
		bandwidth_ = b;
		channel_cnt_ = c;
		if ((c & 0x1) == 1){
			System.out.println("Warn[OFDM.OFDM(double, double, double)]: odd channel count given.");
		}
		if (f+b > MAXFREQ || f < MINFREQ){
			System.out.println("Warn[OFDM.OFDM(double, double, double)]: illegal bandwidth and frequency given.");
		} 
		freq_arr_ = new double[channel_cnt_];
		carrier_arr_ = new double[channel_cnt_][bit_lenght_];

		double delta_band_width = bandwidth_ / (channel_cnt_ - 1);
		for(int i=0; i<channel_cnt_; i++){
			freq_arr_[i] = freq_ + delta_band_width * i;
			for (int j=0; j<bit_lenght_; j++){
				carrier_arr_[i][j] = Math.cos(2*Math.PI*freq_arr_[i]*j/sample_rate_);
			}
		}

	// @input: 		sample, a sample from the wave
	// @output: 	whether a pack of data is found.
	public double demodulate(double sample){
		return false;
	}
	// @input: 		wave, given a received data
	// @output 		transform that data to bits.
	private byte[] waveToData(double[] wave){
		byte[] data = new byte[0];
		return data;		
	}

	public double[] modulate(byte[] data){
		return dataToWave(data);
	}
	private double[] dataToWave(byte[] data){
		double[] wave = new double[0];
		return wave;		
	}

	// @input: 
	// 		a, b: two vectors(array) of the same length
	// @output: a^Tb
	private double dot(double[] a, double[] b){
		if (a.length != b.length){
			System.out.println("Warning[OFDM.dot(double[], double[])]: the length of arrays differ.");
			return 0;
		}
		double sum = 0;
		for (int i = 0; i<a.length; i++){
			sum += a[i] * b[i];
		}
		return sum;
	}

	// @output: r*a
	private double[] mul(double r, double[] a){
		double[] m = new double[a.length];
		for (int i=0; i<a.length; i++){
			m[i] = r * a[i];
		}
		return m;
	}

	// @output: 	a+b
	private double[] add(double[] a, double[] b){
		double[][] matrix = {a, b};
		return add(matrix);
	}

	// @input: 		A \in R n*m
	// @output		Sigma A_i = A1 + A2 ... + An
	private double[] add(double[][] a){
		int n = a.length;
		int m = a[0].length;
		double[] sum = new double[m];
		for (int i=0; i<m; i++){
			for (int j=0; j<n; j++){
				sum[i] += a[i][j];
			}
		}
		return sum;
	}
}