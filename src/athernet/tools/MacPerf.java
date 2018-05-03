package athernet.tools;

import athernet.mac.*;

import java.util.List;

public class MacPerf {
    private byte dest_addr_;
    private MacLayer mac_layer_;

    // record # of package sent in each second
    private List<Integer> record_sent_;
    private int num_unsent_pack_;

    MacPerf(byte dest_addr){
        System.out.println("MacPerf dest_addr: "+dest_addr);
        dest_addr_ = dest_addr;

        try{
            mac_layer_ = new MacLayer(dest_addr);
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }
    }

    // get
    public static byte get_dest_addr(String[] args){
        if (args[0].toLowerCase().equals("node1")){
            return 1;
        }
        else if (args[0].toLowerCase().equals("node2")){
            return 2;
        }
        else if (args[0].toLowerCase().equals("node3")){
            return 3;
        }
        else{
            throw new IllegalArgumentException("Invalid argument for destination address");
        }
    }

    private void requestSendOnce(){
        // randomly generate a package
        byte[] out = new byte[61];
        for (int i = 0; i < out.length; i++){
            out[i] = (byte)(Math.random()*255);
        }

        // request send it
        try {
            mac_layer_.requestSend(dest_addr_, 0, out);
        }
        catch (Exception exception){
            System.out.println("requestSend throw exception");
            throw new RuntimeException("requestSend throw exception");
        }
    }

    private void print_speed(){
        // get the sum of overall sent packages in the window
        int tmp_sum = 0;
        for (int i = 0; i < record_sent_.size(); i++){
            tmp_sum += record_sent_.get(i);
        }

        double speed = (tmp_sum+0.0)/record_sent_.size()*61*8;
        System.out.println(String.format("Speed: %f bps", speed));
    }

    public void start_perfing(){
        for (int i = 0; i < 30; i++){
            requestSendOnce();
            num_unsent_pack_++;
        }

        while(true){
            // sleep for 1s
            try {
                Thread.sleep(1000);
            }
            catch(Exception exception){
                System.out.println("Failed to sleep");
            }

            // give 15 packages to sender if needed
            int new_num_unsent_pack =  mac_layer_.countUnsent(dest_addr_);
            if( new_num_unsent_pack == 0){
                System.out.println("[WARN], unset package reach 0, maybe you need to put more packets in");
            }

            record_sent_.add(num_unsent_pack_ - new_num_unsent_pack);
            num_unsent_pack_ = new_num_unsent_pack;
            print_speed();
            record_sent_.remove(0);

            // add new packets if needed
            if (new_num_unsent_pack < 30){
                for (int i = 0; i < 15; i++){
                    requestSendOnce();
                    num_unsent_pack_++;
                }
            }


        }
    }

    public static void main(String[] args){
        // get dest address and start perfing
        byte dest_addr = get_dest_addr(args);
        MacPerf mac_perf = new MacPerf(dest_addr);

        mac_perf.start_perfing();

    }
}
