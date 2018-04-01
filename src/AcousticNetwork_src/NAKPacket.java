/*
Description: This definition of a NAK packet
    NAK packet: packet_id(255) =
 */
package AcousticNetwork;

import java.util.ArrayList;
import java.util.List;

public class NAKPacket {
    private List<Integer> loss_packets_;
    private int max_num_packets_;                // the maximum number of packets allowed

    public NAKPacket(int max_num_packets){
        max_num_packets_ = max_num_packets;
        loss_packets_ = new ArrayList<>();
    }

    public NAKPacket(List<Integer> loss_packets, int max_num_packets){
        loss_packets_ = loss_packets;
        max_num_packets_ = max_num_packets;
    }

    /*
    Return Value:
        false: when the number of packets excludes the max_packet_limit, the current packet will not be inserted
        true: succeed
     */
    public boolean add_loss_packet(int packet_id){
        if (loss_packets_.size() < max_num_packets_) {
            loss_packets_.add(packet_id);
            return true;
        }
        return false;
    }

    public void clear(){
        loss_packets_ = new ArrayList<>();
    }

    /*
    Note:
        zero(packet_id=255) padding if (number of loss_packet) < expected_length
    Params:
        expected_length: (in bytes)
    Return Value:
        if no loss packet, this would return an empty array
     */
    public byte[] to_array(int expected_length){
        if (loss_packets_.size() == 0){
            return new byte[0];
        }

        byte[] out_array = new byte[expected_length];
        out_array[1] = (byte)254;                   // packet_id for NAK
        for (int i = 0; i < expected_length-2; i++){
            if (i < loss_packets_.size()){
                int tmp_num = loss_packets_.get(i);
                out_array[i+2] = (byte)tmp_num;
            }
            else{
                out_array[i+2] = (byte)0xff;
            }
        }
        return out_array;
    }

    // standard API for java ...
    public byte[] toArray(int expected_length){
        return to_array(expected_length);
    }

    public static void main (String[] args){
        NAKPacket nak_pack = new NAKPacket(14);
        nak_pack.add_loss_packet(0);
        nak_pack.add_loss_packet(15);

        byte[] nak_array = nak_pack.toArray(16);

        for (int i = 0; i < nak_array.length; i++){
            System.out.println(nak_array[i]);
        }

        nak_pack.clear();

        nak_pack.add_loss_packet(234);
        for (int i = 0; i < nak_array.length; i++){
            System.out.println(nak_array[i]);
        }
    }

}
