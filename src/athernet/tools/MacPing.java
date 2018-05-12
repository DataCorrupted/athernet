package athernet.tools;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import java.util.ArrayList;

public class MacPing {
    private byte dest_addr_;
    private byte src_addr_;

    private MacLayer mac_layer_;
    private ArrayList<Byte> unconfirmed_ids;
    private ArrayList<Long> unconfirmed_time;

    public MacPing(byte src_addr, byte dest_addr){
        System.out.println("MacPing dest_addr: "+dest_addr);
        src_addr_ = src_addr;
        dest_addr_ = dest_addr;

        unconfirmed_ids = new ArrayList<Byte>();
        unconfirmed_time = new ArrayList<Long>();

        try{
            mac_layer_ = new MacLayer(src_addr_, dest_addr);
            mac_layer_.startMacLayer();
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

            try {
                Thread.sleep(500);
            }
            catch (Exception exception){
                System.out.println("Thread.sleep throw exception");
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

    public static void main(String[] args) throws Exception{
        NodeConfig node_config = new NodeConfig(args);
        if (args.length == 1){
            MacPing mac_ping = 
                new MacPing(node_config.get_src_addr(), node_config.get_dest_addr());
            mac_ping.start_ping();
        
        } else if (args[1].equals("-S") || args[1].equals("--server")){
            MacLayer mac_layer_ = 
                new MacLayer(node_config.get_src_addr(), node_config.get_dest_addr());
            mac_layer_.startMacLayer();
        } else {
            System.err.println("No such option.");
        }
    }
}
