package athernet.tools;

import athernet.mac.*;

import java.util.List;

public class MacPerf {
    private byte dest_addr_;
    private byte src_addr_;
    private MacLayer mac_layer_;

    // record # of package sent in each second
    private List<Integer> record_sent_;
    private int num_unsent_pack_;

    MacPerf(byte src_addr, byte dest_addr){
        System.out.println("MacPerf dest_addr: "+dest_addr);
        src_addr_ = src_addr;
        dest_addr_ = dest_addr;

        try{
            mac_layer_ = new MacLayer(src_addr_,dest_addr);
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
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
            mac_layer_.requestSend(0, out);
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
            int new_num_unsent_pack =  mac_layer_.countUnsent();
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
        NodeConfig node_config = new NodeConfig(args);

        MacPerf mac_perf = new MacPerf(node_config.get_src_addr(),node_config.get_dest_addr());

        mac_perf.start_perfing();
    }
}
