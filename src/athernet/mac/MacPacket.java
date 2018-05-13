package athernet.mac;

import java.nio.ByteBuffer;

/***
 * The package definition:
 *      DestAddr (2 bits) + SrcAddr (2 bits) + Type (4 bits) + packet_id (one byte) + 
 *      MAC Payload (2^n - 4 bytes)
 */

public class MacPacket {
    public static final byte TYPE_ACK = 0;
    public static final byte TYPE_DATA = 1;
    public static final byte TYPE_INIT = 2;
    public static final byte TYPE_MACPING_REQST = 3;
    public static final byte TYPE_MACPING_REPLY = 4;

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_ACKED = 2;
    public static final int STATUS_LOST = -1;

    private byte dest_addr_;            // 2 bits
    private byte src_addr_;             // 2 bits
    private byte type_;                 // 4 bits
    private byte pack_id_;              // one byte
    private byte[] data_field_;         // 2^n - 4 bytes

    // for system maintenance
    private int resend_counter_ = 0;
    private double timestamp_ = 0;
    private long timestamp_ms_ = 0;
    private long received_ms_ = 0;
    private int status_ = 0;

    // For ACK Packet
    private byte ack_pack_id_;

    // For Data Packet
    private byte[] data_;

    // For InitRequest Packet
    private int total_length_;

    // Total number of packets about to be sent.
    private int pack_cnt_;

    // Constructor, build a MacFrame
//    public MacPacket(byte dest_addr, byte src_addr, byte type, byte[] data_field){
//        dest_addr_ = dest_addr;
//        src_addr_ = src_addr;
//        type_ = type;
//        data_field_ = data_field;
//    }

    // Constructor: decode the frames
    public MacPacket(byte[] frame){
        // fill in Mac attributes
        dest_addr_ = (byte)((frame[1] & 0xC0) >>> 6);
        src_addr_ = (byte)((frame[1] & 0x30) >>> 4);
        type_ = (byte)(frame[1] & 0x0F);
        pack_id_ = frame[0];
        data_field_ = new byte[frame.length - 2];
        System.arraycopy(frame,2,data_field_,0,data_field_.length);
        status_ = STATUS_WAITING;
        // decode data field
        decodeDataField();
    }


    // Constructor: build a Data Packet
    public MacPacket(byte dest_addr, byte src_addr, byte[] data){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_DATA;

        data_field_ = new byte[data.length];
        System.arraycopy(data,0,data_field_,0,data.length);
    }

    // Constructor: build a ACK Packet
    public MacPacket(byte dest_addr, byte src_addr, byte ack_pack_id){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_ACK;
        data_field_ = new byte[1];
        data_field_[0] = ack_pack_id;
    }

    // Constructor: build a init_request packet
    public MacPacket(byte dest_addr, byte src_addr, int total_length){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_INIT;
        data_field_ = new byte[2];
        data_field_[0] = (byte)((total_length & 0xFF00) >>> 8);
        data_field_[1] = (byte)(total_length & 0xFF);
    }

    // Constructor: build a MACPING packet
    public MacPacket(byte dest_addr, byte src_addr){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_MACPING_REQST;
        data_field_ = new byte[0];
    }

    public void convertMacRequestToMacReply(){ 
        if (type_ != TYPE_MACPING_REQST){
            System.err.println(
                "Warning: Converting non Mac-request packet to mac-reply.");
            return;
        }
        type_ = TYPE_MACPING_REPLY; 
        // Swap address.
        byte temp = src_addr_;
        src_addr_ = dest_addr_;
        dest_addr_ = temp;
    }

    public int getSubPackid(){
        if ((type_ == TYPE_MACPING_REPLY) || (type_ == TYPE_MACPING_REQST)) {
            return ((int)data_field_[0]) & 0xff;
        }
        else{
            return -1;
        }
    }

    public long getReceivedMS(){
        return received_ms_;
    }

    public void setReceivedMS(long curr_ms){
        received_ms_ = curr_ms;
    }

    public void setTimestampMs(long time_ms){
        timestamp_ms_ = time_ms;
    }

    public long getTimestampMs(){
        return timestamp_ms_;
    }

    // returintn -1 on error
    public int getACKPacketID(){
        return (type_ == TYPE_ACK) ? ((int) ack_pack_id_) & 0xff: -1;
    }

    // return -1 on error
    public int getTotalLength(){
        return (type_ == TYPE_INIT) ? total_length_: -1;
    }

    // return empty array on error
    public byte[] getData(){
        return (type_ == TYPE_DATA)? data_: new byte[0];
    }

    public int getPacketID(){ return ((int)pack_id_) & 0xff; }

    public byte getSrcAddr(){ return src_addr_; }
    
    public byte getDestAddr(){ return dest_addr_; }
    
    public byte getType(){ return type_; }

    public int getResendCounter(){ return resend_counter_; }

    public void onResendOnce(){ resend_counter_ ++; }

    public double getTimeStamp(){ return timestamp_; }

    public void setTimeStamp(double new_timestamp){ timestamp_ = new_timestamp; }

    public int getStatus(){ return status_; }

    public void setStatus(int status){ status_ = status; }

    public void setPacketID(byte pack_id){
        pack_id_ = pack_id;

        // for MacPing Request, the sub_packid = packid
        if (type_ == TYPE_MACPING_REQST){
            data_field_ = new byte[1];
            data_field_[0] = pack_id;
        }
    }

    // encode all fields to a String
    byte[] toArray(){
        byte[] frame = new byte[data_field_.length + 2];
        frame[1] = 
            (byte)(
                ((dest_addr_ & 0x03) << 6) | ((src_addr_ & 0x03) << 4) | (type_ & 0x0F)
            );
        frame[0] = pack_id_;
        System.arraycopy(data_field_,0,frame,2,data_field_.length);
        return frame;
    }

    // decode the data field only
    private void decodeDataField(){
        if (type_ == TYPE_ACK){
            ack_pack_id_ = data_field_[0];
        } else if (type_ == TYPE_DATA){
            data_ = new byte[data_field_.length];
            System.arraycopy(data_field_,0,data_,0,data_.length);
        } else if (type_ == TYPE_INIT){
            total_length_ = 
                (int)((data_field_[0] & 0xFF) << 8) + (int)(data_field_[1] & 0xFF);
        } else if (type_ == TYPE_MACPING_REPLY || type_ == TYPE_MACPING_REQST){
            data_ = new byte[0];
        } else{
            throw new RuntimeException("Unrecognized MACPacket Type");
        }
    }

    public static void main(String[] args){
        // parameters
        byte dest_addr_test = 0;
        byte src_addr_test = 3;

        // for init_request pack
        int total_legnth_test = 50000;

        // for data packet
        byte[] data = {1,3,4,5,6};

        // for ACK packet
        byte ack_packet_id = (byte)240;

        long timestamp_test = 12004830;

        // create a new package
        MacPacket pack_1 = new MacPacket(dest_addr_test,src_addr_test);
        // MacPacket pack_1 = new MacPacket(dest_addr_test,src_addr_test, data );

        // for reply packet test only
//        pack_1.convertMacRequestToMacReply();
//        byte tmp_addr = src_addr_test;
//        src_addr_test = dest_addr_test;
//        dest_addr_test = tmp_addr;

        pack_1.setPacketID((byte)0);
        byte[] pack_1_str = pack_1.toArray();

        MacPacket pack_recv = new MacPacket(pack_1_str);

        for (int i = 0; i < pack_1_str.length; i++) {
            System.out.println(String.format("%02X ", pack_1_str[i]));
        }

        if (pack_recv.getDestAddr() != dest_addr_test){
            System.out.println("DestAddr Mismatch");
        }
        else if(pack_recv.getSrcAddr() != src_addr_test){
            System.out.println("SrcAddr Mismatch");
        }
        else if(pack_recv.getType() != MacPacket.TYPE_MACPING_REQST){
            System.out.println(pack_recv.type_);
            System.out.println("Type Mismatch");
        }
        else if(pack_recv.getPacketID() != 0){
            System.out.println("PacketID Mismatch");
        }
        else if(pack_recv.getTotalLength() != -1){
            System.out.println("TotalLength Mismatch");
        }
        else if(pack_recv.getACKPacketID() != -1){
            System.out.println("ACKPacketID Mismatch");
        }
//        else if(!Arrays.equals(pack_recv.getData(),data)){
//            System.out.println("--------Data Mismatch---------");
//            byte[] tmp = pack_recv.getData();
//            for (int i = 0; i < tmp.length; i++){
//                System.out.println(tmp[i]);
//            }
//            System.out.println("Data Mismatch");
//        }
        else {
            System.out.println("Success");
        }
    }
}
