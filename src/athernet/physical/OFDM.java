package athernet.physical;

import java.util.List;
import java.util.*;
import java.util.stream.DoubleStream;

public class OFDM{
	public static final int NOTHING = 0;
	public static final int RCVEDDAT = -1;
	public static final int RCVINGDAT = 2;
	public static final int CNFIRMING = 1;

	public final double MINFREQ = 20;
	public final double MAXFREQ = 20000;

	// for header
	// hyper-parameters
	private int init_count_down_;            // The waiting windows for identifying the header
	// The length (in samples) of a sync header and the generated header.
	private int header_len_;
	private double[] sync_header_;
	// for debug only
	public List<Double> sync_power_debug;
	// for demodulation only
	private List<Double> processing_header_;               // used in identifying the header
	private List<Double> processing_data_;                 // used in receiving a single frame
	private List<Double> unconfirmed_data_;                // the buffer for unconfirmed data (when determining)
	private int state_;                                     // indicate whether the current data bit is part of the data
	private int count_down_;
	private double header_score_;
	private double power_energy_;
	private int bit_counter_ = 0;
	private int last_bit_counter_ = 0;

	// the returning bit array
	private byte[] packet_;

	// Total number of channels.
	private int channel_cnt_;

	// Bandwidth.
	private double bandwidth_;

	// Starting frequency.
	private double start_freq_;
	
	// An array for each carrier's frequency.
	private double[] freq_arr_;

	// For each channel, we generate a carrier with length bit_length_;
	private double[][] carrier_arr_;
	
	// Should be 48000 for performance.
	private int sample_rate_;

	// How much samples for a bit.
	private int bit_len_;

	// The length (in bits) of a package.
	private int pack_len_;

	// Max pack length(in bytes)
	private int max_len_;
	// Sin wave header.
	private double[] sin_header_;
	private int dummy_sin_length_ = 100;
	public OFDM(){
		this(48000, 1000, 3000, 4, 48, 440, 1024);
	}
	// Construct OFDM based on given channel count and delta frequency.
	public OFDM(int sample_rate, double start_frequency, 
				double delta, int channel_cnt){
		this(sample_rate, start_frequency, 
			(channel_cnt-1) * delta, channel_cnt, 48, 440, 1024);
	}
	public OFDM(int sample_rate, double start_frequency, 
				double bandwidth, int channel_cnt, 
				int bit_lenght, int header_length, int max_len){
		// hyper-parameters
		init_count_down_ = 200;

		pack_len_ = -1;
		bit_len_ = bit_lenght;
		header_len_ = header_length;
		sample_rate_ = sample_rate;
		start_freq_ = start_frequency;
		bandwidth_ = bandwidth;
		channel_cnt_ = channel_cnt;
		max_len_ = max_len;

		state_ = 0;
		count_down_ = init_count_down_;
		header_score_ = 0;
		power_energy_ = 0.0;
		bit_counter_ = 0;
		processing_header_ = new ArrayList<>();
		processing_data_ = new ArrayList<>();
		unconfirmed_data_ = new ArrayList<>();
		sync_power_debug = new ArrayList<>();
		packet_ = new byte[0];

//		if ((c & 0x1) == 1){
//			System.out.println("Warn[OFDM.OFDM(double, double, double)]: odd channel count given.");
//		}
		if (start_frequency+bandwidth > MAXFREQ || start_frequency < MINFREQ){
			System.out.println("Warn[OFDM.OFDM(double, double, double)]: illegal bandwidth and frequency given.");
		} 
		freq_arr_ = new double[channel_cnt_];
		carrier_arr_ = new double[channel_cnt_][bit_len_];

		double delta_band_width = (channel_cnt_ == 1)? 0:bandwidth_ / (channel_cnt_ - 1);
		for(int i=0; i<channel_cnt_; i++){
			freq_arr_[i] = start_freq_ + delta_band_width * i;
			for (int j=0; j<bit_len_; j++){
				carrier_arr_[i][j] = Math.cos(2*Math.PI*freq_arr_[i]*j/sample_rate_);
			}
		}
		sync_header_ = generateSyncHeader();
		generateDummySin();
	}

	public int getHeaderLength(){
		return header_len_;
	}

	public int getBitLength(){
		return bit_len_;
	}

    private void generateDummySin(){
        sin_header_ = new double[dummy_sin_length_];
        for (int i=0; i< dummy_sin_length_; i++){
            double t = (i +0.0)/ sample_rate_;
            sin_header_[i] = 0.5*(Math.sin(2*Math.PI*1000*t));
        }
    }

	private double[] generateSyncHeader(){
		// generate the base function
		double start_freq = 2000;
		double end_freq = 10000;

		double[] fp = new double[header_len_];
		fp[0] = start_freq;
		for (int i = 1; i < header_len_/2; i++){
			fp[i] = fp[i-1] + (end_freq-start_freq)/(header_len_/2);
		}
		fp[header_len_/2] = end_freq;
		for (int i = header_len_/2+1; i < header_len_; i++){
			fp[i] = fp[i-1] - (end_freq-start_freq)/(header_len_/2);
		}

		// use numerical cumulative integral to generate omega
		double[] omega = new double[header_len_];
		omega[0] = 0;
		for (int i = 1; i < header_len_; i++){
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
	public int demodulate(double sample){
		// given a received signal. Demodulate it.
		// Whether current signals are data or just nothing important.
		bit_counter_ ++;

		// calculate the power
		power_energy_ = power_energy_ * (1-1.0/64.0) + (sample * sample) / 64;

		if (state_ == 0){
			// identify the header
			if (processing_header_.size() == header_len_){
				// if header is already full, remove the first one
				processing_header_.remove(0);
			}
			processing_header_.add(sample);
			if (checkSyncHeader()){
				state_ ++;                  // next state
//                System.out.println("sync_header check passed once, entering confirming state. at bit: " + bit_counter_);
				return CNFIRMING;
			}
			return NOTHING;
		} else if (state_ == 1){
			// waiting for the counter down to be 0, make sure the sync_header is the local maximum
			// add the data into unconfirmed buffer, and recheck the header value
			unconfirmed_data_.add(sample);
			processing_header_.remove(0);
			processing_header_.add(sample);
			count_down_  = count_down_ - 1;

			// call for recheck
			if (checkSyncHeader()){
//				System.out.println("\tHeader reconfirmed at bit: " + bit_counter_ + " " + (bit_counter_ - last_bit_counter_));
				last_bit_counter_ = bit_counter_;
				unconfirmed_data_.clear();
				count_down_ = init_count_down_;
			}

			// if count_down completed, turn the unconfirmed data into actual data
			if (count_down_ == 0){
				// convert data
				while(unconfirmed_data_.size()>0){
					processing_data_.add(unconfirmed_data_.get(0));
					unconfirmed_data_.remove(0);
				}

				// reset initial value
				count_down_ = init_count_down_;
				header_score_ = 0;
				processing_header_.clear();

				// change state
                //System.out.println("Header confirmed, goto receiving data");
				state_ ++;
			}
			return (state_ == CNFIRMING)? CNFIRMING: RCVINGDAT;
		} else if (state_ == 2){
			// add the data to the buffer
			processing_data_.add(sample);

			// calculate the length field
			if ((pack_len_ == -1) && (processing_data_.size() >= 8 * bit_len_ / channel_cnt_)){
				// CRC is no longer one of the frame.
				pack_len_ = decode_length() + 1;
				// Prevent too long pack.
				if (pack_len_ >= max_len_){
					state_ = 0;
					pack_len_ = -1;
					return NOTHING;
				}
				// clear the processing buffer
				for (int i = 0; i < (8 * bit_len_ / channel_cnt_); i++ ){
					processing_data_.remove(0);
				}
			}

			if (processing_data_.size() < pack_len_ * 8 * bit_len_ / channel_cnt_) {
				return RCVINGDAT;               // not enough data to decode
			}

//            System.out.println("Get all data, start demodulation");
			// decode the waveform to get the bits
			// process and clear the buffer
			state_ = 0;
			double[] data_buffer = new double[processing_data_.size()];
			for (int i = 0; i < processing_data_.size(); i++){
				data_buffer[i] = processing_data_.get(i);
			}
			boolean[] packet_boolean = waveToData(data_buffer, pack_len_*8);

			// reserve last several bits for searching window for next packet
			int recheck_length = 100;
			for (int i = 0; i < recheck_length; i++){
				processing_header_.add(processing_data_.get(processing_data_.size() - (recheck_length -i)));
			}

			processing_data_.clear();
			pack_len_ = -1;

			// remove the first one (length field and return)
			packet_ = convertBoolsToBytes(packet_boolean);
//			byte[] packet_tmp_ = convertBoolsToBytes(packet_boolean);
//			packet_ = new byte[packet_tmp_.length - 1];
//			for (int i = 0; i < packet_.length; i++){
//				packet_[i] = packet_tmp_[i+1];
//			}

			return RCVEDDAT;                // new data packet is ready
		} else {
			throw new RuntimeException("Invalid state");
		}
	}

	// Decode the length field
	// The length field is always in the font of processing_data
	private int decode_length(){
		// only give necessary data to waveToData()
		double[] length_buffer = new double[8*bit_len_/channel_cnt_];
		for (int i = 0; i < length_buffer.length; i++){
			length_buffer[i] = processing_data_.get(i);
		}


		boolean[] packet_boolean = waveToData(length_buffer,8);

		boolean[] length_boolean = new boolean[8];
		for (int i = 0; i < 8; i++){
			length_boolean[i] = packet_boolean[i];
		}
		byte[] tmp = convertBoolsToBytes(length_boolean);
		return (int)Math.pow(2,tmp[0]&0x7F);
	}

	public double[] modulate(byte[] byte_data){
		// check the length of the input array
		if (byte_data.length > 256){
			throw new RuntimeException("OFDM Modulate: the length of input data array is too huge");
		}

		// encode the length
		byte data_length = (byte)(Math.log(byte_data.length - 1)/Math.log(2));

		byte[] new_byte_data = new byte[byte_data.length + 1];
		new_byte_data[0] = data_length;
		System.arraycopy(byte_data,0,new_byte_data,1,byte_data.length);

		double[] wave = modulateWithLength(new_byte_data);

		// add the sync header part to the frame and return modulated signal
		double[] output_frame =
				DoubleStream.concat(
						Arrays.stream(sync_header_),
						Arrays.stream(wave)
				).toArray();
		output_frame =
				DoubleStream.concat(
						Arrays.stream(sin_header_),
						Arrays.stream(output_frame)
				).toArray();

		return output_frame;
	}


	// @input: 		wave, given a received data
	// @output 		transform that data to bits.
	// pack_len = wave / bit_len (44)
	private boolean[] waveToData(double[] wave, int pack_len){
		boolean[] data = new boolean[pack_len];
		int chunk_cnt = pack_len / channel_cnt_;
		double[] sub_wave = new double[bit_len_];
		for (int i = 0; i<chunk_cnt; i++){
			System.arraycopy(wave, i*bit_len_, sub_wave, 0, bit_len_);
			for (int j=0; j<channel_cnt_; j++){
				double sum = dot(sub_wave, carrier_arr_[j]);
				data[i * channel_cnt_ + j] = (sum > 0);
			}
		}
		return data;
	}


	// Modulate the data (length has been encoded and put to the front of the data)
	private double[] modulateWithLength(byte[] byte_data){
		boolean[] data = bytesToBools(byte_data);

		int chunk_cnt = data.length / channel_cnt_;
		double[] wave = new double[chunk_cnt * bit_len_];
		for (int i=0; i<chunk_cnt; i++){
			double[] chunk_wave = new double[bit_len_];
			for (int j =0; j<channel_cnt_; j++){
				int phase = data[i*channel_cnt_ + j]? 1: -1;
				chunk_wave = add(chunk_wave, mul(phase, carrier_arr_[j]));
			}
			System.arraycopy(chunk_wave, 0, wave, i*bit_len_, bit_len_);
		}

		// Normalize.
		wave = mul(1.0/channel_cnt_, wave);

		return wave;
	}

	private boolean[] bytesToBools(byte[] byte_data){
		boolean[] data = new boolean[byte_data.length << 3];
		for (int i=0; i<byte_data.length; i++){
			int mask = 0x80;
			for (int j=0; j<8; j++){
				data[i*8+j] = ((byte_data[i] & mask) == mask);
				mask = mask >>> 1;
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
		for (int j=0; j<m; j++){
			for (int i=0; i<n; i++){
				sum[j] += a[i][j];
			}
		}
		return sum;
	}

	private boolean checkSyncHeader(){
		if (processing_header_.size() < header_len_){
			sync_power_debug.add(0.0);
			return false;
		}
		// normalize the header
		// List<Double> normalized_header = normalize_data(processing_header_);

		// calculate the dot product
		double sync_power = 0;
		for (int i = 0; i < header_len_; i++){
			sync_power = sync_power + sync_header_[i] * processing_header_.get(i);
		}
		// TODO(jianxiong cai): for some reason, reference program said divided by 200
		sync_power = sync_power / 200;
		sync_power_debug.add(sync_power);
		if ( (sync_power > (power_energy_ * power_energy_)) && (sync_power > header_score_) && (sync_power > 0.05)){
			header_score_ = sync_power;
			return true;
		}
		else{
			return false;
		}
	}

	private byte[] convertBoolsToBytes(boolean[] array_in){
		// check for safety
		if (array_in.length/8*8 != array_in.length){
			throw new RuntimeException("Trying to convert misaligned array to bytes[]");
		}
		byte[] array_out = new byte[array_in.length/8];

		int counter = 0;

		for (int i = 0; i < array_out.length; i++){
			int tmp_char = 0;
			for (int j = 0; j < 8; j++){
				// append a new boolean into the byte
				tmp_char = tmp_char << 1;
				// if the boolean is true, change the last bit to 1
				if (array_in[counter]){
					tmp_char = (tmp_char | 0x01);
				}
				counter ++;
			}
			array_out[i] = (byte)tmp_char;
		}

		return array_out;
	}

	public byte[] getPacket(){
       // convert packet_ to byte[]
        if (packet_.length == 0){
            return new byte[0];
        }

        byte[] array_out = packet_.clone();

        // clear packet_ on return
        packet_ = new byte[0];

        return array_out;
    }


	public static void main(String[] args){
		// The data is: "exprs sth in 16 "
		byte[] data = {
			0x65, 0x78, 0x70, 0x72, 0x73, 0x20, 0x73, 0x74, 
			0x68, 0x20, 0x69, 0x6e, 0x20, 0x31, 0x36, 0x20
		};
		OFDM ofdm = new OFDM(48000, 1000, 10000, 8, 44, 440, 1024);
		double[] wave = ofdm.modulate(data);

		for (int i=0; i<wave.length; i++){
			ofdm.demodulate(wave[i]);
		}

		byte[] recv_dat = ofdm.getPacket();
		for (int i=0; i<data.length; i++){
			System.out.println(recv_dat[i] + " " + data[i] + " " + (recv_dat[i] == data[i]));
		}

	}
}

