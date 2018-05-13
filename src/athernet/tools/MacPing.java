package athernet.tools;

import athernet.mac.MacLayer;
import athernet.mac.MacPacket;

import java.util.ArrayList;
import java.util.Set;

public class MacPing {
    private byte dest_addr_;
    private byte src_addr_;

    private MacLayer mac_layer_;
    private ArrayList<MacPacket> sent_packs;

    private int counter = 0;

    public MacPing(byte src_addr, byte dest_addr){
        System.out.println("MacPing dest_addr: "+dest_addr);
        src_addr_ = src_addr;
        dest_addr_ = dest_addr;

        counter = 0;

        sent_packs = new ArrayList<MacPacket>();

        try{
            mac_layer_ = new MacLayer(src_addr_, dest_addr);
            mac_layer_.startMacLayer();
            mac_layer_.turnEcho();
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }
    }

    // RequestSend(mac_pack)
    // GetOnePack
    public void start_ping() throws Exception{
        while(true){
            if (counter % 100 == 0) {
                sendOnePacket();
                counter = 0;
            }
            counter ++;

            // receive one packet
            if (mac_layer_.countDataPack() != 0) {
                // System.out.println("mac_layer countDataPack != 0");
                MacPacket received_pack = mac_layer_.getOnePack();
                // System.out.printf("[MacPing::receive] SystemTime: %d\n",System.currentTimeMillis());
                byte packid = received_pack.getSubPackid();
                System.out.printf("packid: %d\n",packid);

                // find the packet
                Boolean found_flag = false;
                for (int i = 0; i < sent_packs.size(); i++){
                    if (sent_packs.get(i).getPacketID() == packid){
                        // received packet found
                        double rtt = (received_pack.getReceivedMS() - sent_packs.get(i).getTimestampMs())/ 1e3;

                        System.out.printf(
                                "Received packid: %d, RTT: %.9f\n", packid, rtt);

                        sent_packs.remove(i);
                        found_flag = true;
                        break;
                    }
                }

                if (!found_flag){
                    System.out.printf("No matching for received packet. PackID: %d \n", packid);
                }
            }

            // check timeout
            while((sent_packs.size() > 0) && (sent_packs.get(0).getTimestampMs() + 2e3) < System.currentTimeMillis()){
                System.out.printf("[MacPing2] Packet %d Timeout\n", sent_packs.get(0).getPacketID());
                sent_packs.remove(0);
            }

            try {
                Thread.sleep(1);
            }
            catch (Exception exception){
                System.out.println("Thread.sleep throw exception");
            }
        }
    }


    private void sendOnePacket(){
        long curr_time = 0;
        MacPacket packet = new MacPacket(dest_addr_,src_addr_);
        packet.setPacketID((byte)0);

        try {
            mac_layer_.requestSend(packet);
        }
        catch (Exception exception){
            System.out.println("MacLayer throw exception");
        }

        while(packet.getTimestampMs() == 0){
            try {
                Thread.sleep(20);
            }
            catch (Exception exception){
                System.out.println("Thread throw exception");
            }
        }

        // save the packet_id

        sent_packs.add(packet);
        // System.out.printf("[MacPing::send] SystemTime: %d\n",System.currentTimeMillis());
    }

    public static void main(String[] args) throws Exception{
        NodeConfig node_config = new NodeConfig(args);
        System.out.printf("Source Address: %d\n", node_config.get_src_addr());
        System.out.printf("Target Address: %d\n", node_config.get_dest_addr());

        if (args.length == 1){
            MacPing mac_ping = 
                new MacPing(node_config.get_src_addr(), node_config.get_dest_addr());
            mac_ping.start_ping();
        
        } else if (args[1].equals("-S") || args[1].equals("--server")){
            System.err.println("Server started.");
            MacLayer mac_layer_ = 
                new MacLayer(node_config.get_dest_addr(), node_config.get_src_addr());
            mac_layer_.startMacLayer();
        } else {
            System.err.println("No such option.");
        }
    }
}
