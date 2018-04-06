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
	private int bit_len_;

	// The length (in bits) of a package.
	private int pack_len_;

	// The length (in samples) of a sync header and the generated header.
	private int header_len_;
	private double[] sync_header_;
	
	public OFDM(){
		this(44100, 1000, 3000, 4, 44, 128, 440);
	}
	public OFDM(int sample_rate, double f, double b, int c, 
				int bit_lenght, int pack_length, int header_length){
		pack_len_ = pack_length;
		bit_len_ = bit_lenght;
		header_len_ = header_length;
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
		carrier_arr_ = new double[channel_cnt_][bit_len_];

		double delta_band_width = bandwidth_ / (channel_cnt_ - 1);
		for(int i=0; i<channel_cnt_; i++){
			freq_arr_[i] = freq_ + delta_band_width * i;
			for (int j=0; j<bit_len_; j++){
				carrier_arr_[i][j] = Math.cos(2*Math.PI*freq_arr_[i]*j/sample_rate_);
			}
		}
		sync_header_ = generateSyncHeader();
	}

	private double[] generateSyncHeader(){
		// TODO: change the header generation function for optimization?
		// generate the base function
		int start_freq = 2000;
		int end_freq = 10000;

		double[] fp = new double[header_len_];
		fp[0] = start_freq;
		for (int i = 1; i < header_len_/2; i++){
			fp[i] = fp[i-1] + ((end_freq-start_freq)+0.0)/(header_len_/2);
		}
		fp[header_len_/2] = end_freq;
		for (int i = header_len_/2+1; i < header_len_; i++){
			fp[i] = fp[i-1] - ((end_freq-start_freq)+0.0)/(header_len_/2);
		}

		// use numerical cumulative integral to generate omega
		double[] omega = new double[header_len_];
		omega[0] = 0;
		for (int i = 1; i < header_len_; i++){
			// TODO: from reference program, using t (sample rate) instead of header_length?
			omega[i] = omega[i-1] + (fp[i]+fp[i-1])/2.0*(1.0/sample_rate_);
		}

		double[] sync_header = new double[header_len_];
		for (int i = 0; i < header_len_; i++){
			sync_header[i] = Math.sin(2*Math.PI*omega[i]);
		}
		return sync_header;
	}

	// @input: 		sample, a sample from the wave
	// @output: 	whether a pack of data is found.
	public boolean demodulate(double sample){
		return false;
	}
	// @input: 		wave, given a received data
	// @output 		transform that data to bits.
	private byte[] waveToData(double[] wave){
		byte[] data = new byte[0];
		return data;		
	}

	public double[] modulate(byte[] byte_data){
		boolean[] data = byteToBoolean(byte_data);
		for (int i=0; i<data.length / channel_cnt_; i++){
			for (int j =0; j<channel_cnt_; j++){
				
			}
		}
		return dataToWave(data);
	}

	private boolean[] byteToBoolean(double[] byte_data){
		boolean[] data = boolean[byte_data.lengyh >>> 3];
		for (int i=0; i<byte_data.length; i++){
			int mask = 0x80;
			for (int j=0; j<8; j++){
				data[i*8+j] = (byte_data[i] & mask == mask);
				mask >>> 1;
			}
		}
		return data;
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