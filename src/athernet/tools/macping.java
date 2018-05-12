package athernet.tools;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class macping {
    private byte dest_addr_;
    private byte src_addr_;

    private MacLayer mac_layer_;
    private ArrayList<Byte> unconfirmed_ids;
    private ArrayList<Long> unconfirmed_time;

    public macping(byte src_addr, byte dest_addr){
        System.out.println("MacPing dest_addr: "+dest_addr);
        src_addr_ = src_addr;
        dest_addr_ = dest_addr;

        unconfirmed_ids = new ArrayList<Byte>();
        unconfirmed_time = new ArrayList<Long>();

        try{
            mac_layer_ = new MacLayer(src_addr_,dest_addr);
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }
    }

    // RequestSend(mac_pack)
    // GetOnePack
    public void start_ping(){
        while(true){
            if (mac_layer_.isIdle()) { sendOnePacket(); }
            // receive one packet
            if (mac_layer_.countDataPack() != 0) {
                try {
                    MacPacket received_pack = mac_layer_.getOnePack();
                    long curr_time = System.nanoTime();
                    long rtt = curr_time - received_pack.get_timestamp_macping();
                    System.out.printf(
                        "Received packid: %d, RTT: %d\n", 
                        received_pack.getPacketID(), rtt);

                    // print timeout
                    while (unconfirmed_ids.get(0) != received_pack.getPacketID()){
                        System.out.printf("Packet %d Timeout\n", unconfirmed_ids.get(0));
                        unconfirmed_ids.remove(0);
                        unconfirmed_time.remove(0);
                    }

                    // remove the received one
                    unconfirmed_ids.remove(0);
                    unconfirmed_time.remove(0);
                } catch (Exception exception) {
                    System.out.println("mac_layer_.getOnePack received one exception");
                }
            }

            // check timeout
            while((unconfirmed_time.get(0) + 2e9) > System.nanoTime()){
                System.out.printf("Packet %d Timeout\n", unconfirmed_ids.get(0));
                unconfirmed_time.remove(0);
                unconfirmed_ids.remove(0);
            }
        }
    }


    private void sendOnePacket(){
        long curr_time = System.nanoTime();
        MacPacket packet = new MacPacket(dest_addr_,src_addr_,curr_time);
        packet.setPacketID((byte)0);

        try {
            mac_layer_.requestSend(packet);
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }

        while(packet.getPacketID() == 0){
            try {
                Thread.sleep(0, 20);
            }
            catch (Exception exception){
                System.out.println("Thread throw exception");
            }
        }

        // save the packet_id
        unconfirmed_ids.add(packet.getPacketID());
        unconfirmed_time.add(packet.get_timestamp_macping());
    }

    public static void main(String args[]){
        NodeConfig node_config = new NodeConfig(args);

        macping mac_ping = 
            new macping(node_config.get_src_addr(), node_config.get_dest_addr());
        mac_ping.start_ping();
    }

}
