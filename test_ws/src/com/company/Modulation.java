package com.company;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.math.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/* Description: A PSK modulation method
   TODO;    Add sync head to the output
 */

/*
Implementation note:
    state_:     0       =>  initial receiving state (identify the sync header)
                1       =>  possible header identified, waiting for double confirmation (peak following?)
                2       =>  entering receiving state
    count_down_:        How many bits to wait till the confirmation of a sync_header
 */

class Modulation{
    // hyper-parameters
    private int init_count_down_;            // The waiting windows for identifying the header

    // need to set in constructor
    private int bit_length_;            // the duration for a bit (in sample points, usually 44)
    private int header_length_;         // the length of the header (in samples points, usually 440)
    private int sample_rate_;           // usually 44100 Hz
    private int max_frame_length_;      // the length of a frame (in bytes), excluding the header

    private double[] carrier_;          // the carrier signal
    private double[] sync_header_;      // the sync header (for sync)

    // for demodulation only
    private List<Double> processing_header_;               // used in identifying the header
    private List<Double> processing_data_;                 // used in receiving a single frame
    private List<Double> unconfirmed_data_;                // the buffer for unconfirmed data (when determining)
    private int state_;                                     // indicate whether the current data bit is part of the data
    private int count_down_;
    private double header_score_;
    private boolean[] packet_;

    /* Someone want to overload this */
    public Modulation(int sample_rate){
        this(44,  440, sample_rate, 1100/8, 200, 10000);
    }

    /*
        Params:
            carrier_freq: the frequency for carrier (usually 10^3Hz)
     */
    public Modulation(int bit_length, int header_length, int sample_rate, int max_frame_length, int init_count_down,
                      int carrier_freq){
        state_ = 0;

        bit_length_ = bit_length;
        header_length_ = header_length;
        sample_rate_ = sample_rate;
        max_frame_length_ = max_frame_length;

        // count down related
        init_count_down_ = init_count_down;
        count_down_ = init_count_down_;

        // initialize
        processing_header_ = new ArrayList<>();
        processing_data_ = new ArrayList<>();
        unconfirmed_data_ = new ArrayList<>();

        // generate one standard frame unit (the waveform for max_frame_length)
        carrier_ = new double[bit_length * max_frame_length * 8];

        // the carrier is generated in 10kHZ
        for (int i = 0; i < carrier_.length; i++){
            carrier_[i] = Math.sin( 2 * Math.PI * carrier_freq * i/sample_rate);
        }

        // generate the sync_header
        // TODO: change the header generation function
        sync_header_ = new double[header_length];
        for (int i = 0; i < header_length; i++){
            sync_header_[i] = Math.sin(2*Math.PI*i);
        }
    }

    // given a frame array (bits), return its modulated signal
    // 1 is the same as carrier, while 0 is -phrase
    public double[] modulate(byte[] frame_bytes){
        boolean[] frame_booleans = convert_bytes_to_booleans(frame_bytes);
        double[] output_data = new double[frame_booleans.length * bit_length_];

        int counter = 0;

        for (int i = 0 ; i < frame_booleans.length; i++){
            // convert each bits to a waveform
            int phrase = 1;
            if (!frame_booleans[i])          phrase = -1;
            for (int j = 0; j < bit_length_; j++){
                output_data[counter] = carrier_[j] * phrase;
                counter ++;
            }
        }

        // add the sync header part to the frame and return modulated signal
        double[] output_frame = DoubleStream.concat(Arrays.stream(sync_header_),Arrays.stream(output_data)).toArray();
        return output_frame;
    }

    /*
    Params:
        sample: an single sample from SoundIO
        expected_frame_length: (in bytes)
            the frame length to decode after catching a header.
            When this one is set to 0, it means infinity. However, that function will not be supported now.
            TODO: support expected_frame_length infinity.
    Return value:
        True: when a complete datapacket is ready.
        False: Otherwise
    */
    public boolean demodulation(double sample, int expected_length){
        // given a recved signal. Demodulate it.
        // Whether current signals are data or just nothing important.

        if (state_ == 0){
            // identify the header
            processing_header_.add(sample);
            if (processing_header_.size() < header_length_){
                return false;               // return when not adequate data
            }
            if (check_sync_header()){
                state_ ++;                  // next state
            }
            return false;                   // anyway, the data can not be returned
        }
        else if (state_ == 1){
            // waiting for the counter down to be 0, make sure the sync_header is the local maximum
            // add the data into unconfirmed buffer, and recheck the header value
            unconfirmed_data_.add(sample);
            processing_header_.remove(0);
            processing_header_.add(sample);
            count_down_  = count_down_ - 1;

            // call for recheck
            if (check_sync_header()){
                // previous is wrong, this is the new header, passed data are noise
                unconfirmed_data_.clear();
                count_down_ = init_count_down_;
            }

            // if count_down completed, turn the unconfirmed data into actual data
            if (count_down_ == 0){
                // convert data
                for (int i = 0; i < unconfirmed_data_.size(); i++){
                    processing_data_.add(unconfirmed_data_.get(0));
                    unconfirmed_data_.remove(0);
                }

                // reset initial value
                count_down_ = init_count_down_;
                header_score_ = 0;
                processing_header_.clear();

                // change state
                state_ ++;
            }

            return false;
        }
        else if (state_ == 2){
            // add the data to the buffer
            processing_data_.add(sample);
            if (processing_data_.size() < expected_length * 8 * bit_length_) {
                return false;               // not enough data to decode
            }

            // decode the waveform to get the bits
            // process and clear the buffer
            state_ = 0;
            // TODO: convert double queue buffer to data
            processing_data_.clear();

        }
        else {
            throw new RuntimeException(new String("Invalid state"));
        }



		/*
		if (is_data) {
			if can be demodulated to 0 or 1 {
				put 0/1 in packet
				if (packet is full){
					is_data = false
				}
			} else {
				wait for more sample until it can be demodulated.
				( Should have same samples for each bit)
			}
		} else {
			match for head.
			If head matched, {
				is_data = true;
			}
		}
		return is_packet_full;*/
	}

	/*
	Return value:
		Return an empty byte[] when no packet is valid
	*/
    public byte[] getPacket(){
       // convert packet_ to byte[]
        if (packet_.length == 0){
            return new byte[0];
        }

        for (int i = 0; i < frame_length_; i++){
            for (int j = 0; j < 8; j++)

        }

        return packet_;
    }

    /*  Description:
            return true only when all of the following condition are met:
            1. a possible sync header is meet
            2. the score for this match > header_score (previous match)
        Return Value:
            true: when the current sample is identified as the end of a header (based on current knowledge)
      */
    private boolean check_sync_header(){
        // calculate the dot product
        double sync_power = 0;
        for (int i = 0; i < header_length_; i++){
            sync_power = sync_power + sync_header_[i] * processing_header_.get(i);
        }
        // TODO: enforce more tight condition
        if (sync_power > header_score_){
            header_score_ = sync_power;
            return true;
        }
        else{
            return false;
        }
    }

    private boolean[] convert_bytes_to_booleans(byte[] array_in){
        boolean[] array_out = new boolean[array_in.length * 8];

        int counter = 0;

        for (int i = 0; i < array_in.length; i++){

            byte tmp_char = array_in[i];
            int mask = 0x80;

            for (int j = 0; j < 8; j++){
                array_out[counter] = ((tmp_char & mask) == mask);
                mask = mask >>> 1;              // unsigned shift
                counter ++;
            }
        }

        return array_out;
    }

    private byte[] convert_booleans_to_bytes(boolean[] array_in){
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

    // decode the processing_data (receiver side)
    // convert sound to boolean[]
    private boolean[] convert_processing_data(){

    }

    public static void main(String[] args){


    }

}