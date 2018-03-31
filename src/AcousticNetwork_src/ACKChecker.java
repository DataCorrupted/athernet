package AcousticNetwork;

import java.util.ArrayList;
import java.util.List;

public class ACKChecker {
    private int last_expected_pack_id_;
    // if pack received, received_state[pack_id] = true
    private boolean[] received_status_;

    /*
    Params:
        the last packet_id expected
     */
    ACKChecker(int last_expected_pack_id){
        last_expected_pack_id_ = last_expected_pack_id;

        received_status_ = new boolean[last_expected_pack_id+1];
        for (int i = 0; i<= last_expected_pack_id; i++){
            received_status_[i] = false;
        }
    }

    /*
    Return Value:
        false: indicate the packet_id is invalid (excluding last expected packet_id)
        true: succeed
     */
    public boolean on_ack(int packet_id){
        if (packet_id > last_expected_pack_id_){
            System.out.println("[WARNING] ACKChecker: invalid packet_id in on_ack().");
            return false;
        }
        received_status_[packet_id] = true;
        return true;
    }

    public boolean has_loss_packet(){
        for (int i = 0; i <= last_expected_pack_id_; i++){
            if (!received_status_[i])       return true;
        }
        return false;
    }

    // return an array containing all nak packets
    public List<Integer> get_loss_list(){
        List<Integer> out_list = new ArrayList<>();
        for (int i = 0; i <= last_expected_pack_id_; i++){
            if (!received_status_[i])           out_list.add((i));
        }
        return out_list;
    }

    public static void main(String[] args){
        // unit test for this class
        ACKChecker ack_checker = new ACKChecker(10);



        for (byte i = 0; i <= 5; i++){
            ack_checker.on_ack(i);
        }


        ack_checker.on_ack(8);
        ack_checker.on_ack(10);


        ack_checker.on_ack(255);

        List<Integer> loss_list = ack_checker.get_loss_list();
        for (int i = 0; i < loss_list.size(); i++){
            System.out.println(loss_list.get(i));
        }
    }
}