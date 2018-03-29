package com.company;

import com.sun.org.apache.xpath.internal.operations.Bool;

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
    private int count_down_;            // The waiting windows for identifying the header

    // need to set in constructor
    private int bit_length_;            // the duration for a bit (in sample points, usually 44)
    private int header_length_;         // the length of the header (in samples points, usually 440)
    private int sample_rate_;           // usually 44100 Hz
    private int max_frame_length_;      // the length of a frame (in bytes), excluding the header

    private double[] carrier_;          // the carrier signal
    private double[] sync_header_;      // the sync header (for sync)

    // for demodulation only
    private Queue<Double> processing_header_;               // used in identifying the header
    private Queue<Double> processing_data_;                 // used in receiving a single frame
    private Queue<Double> unconfirmed_data_;                // the buffer for unconfirmed data (when determining)
    private int state_;                                     // indicate whether the current data bit is part of the data
    private boolean[] packet_;

    /* Someone want to overload this */
    public Modulation(int sample_rate){
        this(44,  440, sample_rate, 1100, 200, 10000);
    }

    /*
        Params:
            carrier_freq: the frequency for carrier (usually 10^3Hz)
     */
    public Modulation(int bit_length, int header_length, int sample_rate, int max_frame_length, int count_down_,
                      int carrier_freq){
        state_ = 0;

        bit_length_ = bit_length;
        header_length_ = header_length;
        sample_rate_ = sample_rate;
        max_frame_length_ = max_frame_length;

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
    // TODO: maybe convert the byte array
    public double[] modulate(byte[] frame_bytes){
        boolean[] frame_booleans = convert_bytes_to_booleans(frame_bytes);
        double[] output_data = new double[frame_in.length * ( *sample_rate_)];
        for (int i = 0 ; i < frame_in.length; i++){
            // convert each bits to a frame
            for (int j = 0; j < carrier_.length; j++){
                // phrase modulation
                output_data[j + i * carrier_.length] = carrier_[j] * (frame_in[i] * 2 - 1);
            }
        }

        // output_frame = sync_header_.clone() + output_frame.clone();
        double[] output_frame = DoubleStream.concat(Arrays.stream(sync_header_),Arrays.stream(output_data)).toArray();
        return output_frame;
    }

    /*
    Return value:
        True: when a complete datapacket is ready.
        False: Otherwise
    */

    public boolean demodulation(double sample){
        // given a recved signal. Demodulate it.
        // Whether current signals are data or just nothing important.

        if (state_ == 0){
            // identify the header
        }
        else if (state_ == 1){
            // waiting for the counter down to be 0, make sure the sync_header is the local maximum
        }
        else if (state_ == 2){
            // add the data to the buffer
            processing_data_.add(sample);
            if (processing_data_.size() == frame_length_ * 8 * bit_length_){
                // process and clear the buffer
                state_ = 0;
                // TODO: convert double queue buffer to data
                processing_data_.clear();
            }
        }
        else {
            throw new RuntimeException(new String("Invalid state"));
        }

        if (state_){
            // for data segment, directly append that to the processing_buffer
            processing_data_.add(sample);
            // until one data packet is completed
            if (processing_data_.size() == frame_length_){
                is_data_ = false;
                // convert the processing buffer to array TODO
                packet_ = new int [processing_data_.size()];
                packet_ =
                packet_ = processing_data_.toArray();
            }
        }
        else{
            // try to identify the sync_header
            // add the current element to the header
            if (processing_header_.size() == header_length_){

            }
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

    // return true only when the sync header is meet at the head of
    private boolean check_sync_header(){

        return true;
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

    public static void main(String[] args){


    }

}