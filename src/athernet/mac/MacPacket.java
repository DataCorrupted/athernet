package athernet.mac;


public class MacPacket {
    public final byte TYPE_ACK = 0;
    public final byte TYPE_DATA = 1;
    public final byte TYPE_INIT = 2;

    private byte mac_addr_;             // 2 bits
    private byte type_;                 // 6 bits
    private byte pack_id_;              // one byte
    private byte[] data_field_;               // 2^n - 3 bytes

    // For ACK Packet
    private byte ack_pack_id_;

    // For Data Packet
    private byte offset_;
    private byte[] data_;

    // For InitRequest Packet
    private int total_length_;

    // Constructor, build a MacFrame
    public MacPacket(byte mac_addr, byte type, byte pack_id, byte[] data_field){
        mac_addr_ = mac_addr;
        type_ = type;
        pack_id_ = pack_id;
        data_field_ = data_field;
    }

    // Constructor: decode the frames
    public MacPacket(byte[] frame){
        // fill in Mac attributes
        mac_addr_ = (byte)(frame[0] >> 6);
        type_ = (byte)(frame[0] & 0x40);
        pack_id_ = frame[1];
        data_field_ = new byte[frame.length - 2];
        System.arraycopy(frame,2,data_field_,0,data_field_.length);

        // decode data field
        decodeDataField();
    }


    // TODO (Constructors)
    // Constructor: build a ACK Packet

    // Constructor: build a Data Packet

    // Constructor: build a init_request packet

    // return -1 on error
    public byte get_ack_pack_id(){
        if (type_ == TYPE_ACK){
            return ack_pack_id_;
        }
        else{
            return -1;
        }
    }

    // return -1 on error
    public int get_total_length(){
        if (type_ == TYPE_INIT){
            return total_length_;
        }
        else{
            return -1;
        }
    }

    // return -1 on error
    public byte get_offset(){
        if (type_ == TYPE_DATA){
            return offset_;
        }
        else{
            return -1;
        }
    }

    // return empty array on error
    public byte[] get_data(){
        if (type_ == TYPE_DATA){
            return data_;
        }
        else{
            return new byte[0];
        }
    }

    private void decodeDataField(){
        if (type_ == TYPE_ACK){
            ack_pack_id_ = data_field_[0];
        }
        else if (type_ == TYPE_DATA){
            offset_ = data_field_[1];
            data_ = new byte[data_field_.length - 1];
            System.arraycopy(data_field_,1,data_,0,data_.length);
        }
        else if (type_ == TYPE_INIT){
            total_length_ = data_field_[0];
        }
        else{
            throw new RuntimeException("Unrecognized MACPacket Type");
        }
    }
}
