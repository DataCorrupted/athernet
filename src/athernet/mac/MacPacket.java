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


    // Constructor: build a ACK Packet
    public MacPacket(byte mac_addr, byte pack_id, byte[] data){
        mac_addr_ = mac_addr;
        type_ = TYPE_DATA;
        pack_id_ = pack_id;
        data_field_ = data;
    }

    // Constructor: build a Data Packet
    public MacPacket(byte mac_addr, byte pack_id, byte ack_pack_id){
        mac_addr_ = mac_addr;
        type_ = TYPE_ACK;
        pack_id_ = pack_id;
        data_field_ = new byte[1];
        data_field_[0] = ack_pack_id;
    }

    // Constructor: build a init_request packet
    public MacPacket(byte mac_addr, byte pack_id, int total_length){
        mac_addr_ = mac_addr;
        type_ = TYPE_ACK;
        pack_id_ = pack_id;
        data_field_ = new byte[2];
        data_field_[0] = (byte)(total_length >> 8);
        data_field_[1] = (byte)(total_length & 0xFF);
    }

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
            total_length_ = (int)data_field_[0] << 8 + (int)data_field_[1];
        }
        else{
            throw new RuntimeException("Unrecognized MACPacket Type");
        }
    }
}
