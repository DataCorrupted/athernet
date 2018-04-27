package athernet.mac;

/***
 * The package definition:
 *      DestAddr (2 bits) + SrcAddr (2 bits) + Type (4 bits) + packet_id (one byte) + MAC Payload (2^n - 3 bytes)
 */

public class MacPacket {
    public static final byte TYPE_ACK = 0;
    public static final byte TYPE_DATA = 1;
    public static final byte TYPE_INIT = 2;
    public static final int STATUS_WAITING = 0;
    public static final int STATUS_SENT = 1;

    private byte dest_addr_;            // 2 bits
    private byte src_addr_;             // 2 bits
    private byte type_;                 // 4 bits
    private byte pack_id_;              // one byte
    private byte[] data_field_;         // 2^n - 3 bytes

    // for system maintenance
    private int resend_counter_ = 0;
    private double timestamp_ = 0;
    private int status_ = 0;

    // For ACK Packet
    private byte ack_pack_id_;

    // For Data Packet
    private byte offset_;               // one byte
    private byte[] data_;

    // For InitRequest Packet
    private int total_length_;

    // Constructor, build a MacFrame
    public MacPacket(byte dest_addr, byte src_addr, byte type, byte pack_id, byte[] data_field){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = type;
        pack_id_ = pack_id;
        data_field_ = data_field;
    }

    // Constructor: decode the frames
    public MacPacket(byte[] frame){
        // fill in Mac attributes
        dest_addr_ = (byte)(frame[0] >> 6);
        src_addr_ = (byte)((frame[0] & 0x30) >> 4);
        type_ = (byte)(frame[0] & 0x0F);
        pack_id_ = frame[1];
        data_field_ = new byte[frame.length - 2];
        System.arraycopy(frame,2,data_field_,0,data_field_.length);

        // decode data field
        decodeDataField();
    }


    // Constructor: build a ACK Packet
    public MacPacket(byte dest_addr, byte src_addr, byte pack_id, byte[] data){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_DATA;
        pack_id_ = pack_id;
        data_field_ = data;
    }

    // Constructor: build a Data Packet
    public MacPacket(byte dest_addr, byte src_addr, byte pack_id, byte ack_pack_id){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_ACK;
        pack_id_ = pack_id;
        data_field_ = new byte[1];
        data_field_[0] = ack_pack_id;
    }

    // Constructor: build a init_request packet
    public MacPacket(byte dest_addr, byte src_addr, byte pack_id, int total_length){
        dest_addr_ = dest_addr;
        src_addr_ = src_addr;
        type_ = TYPE_ACK;
        pack_id_ = pack_id;
        data_field_ = new byte[2];
        data_field_[0] = (byte)(total_length >> 8);
        data_field_[1] = (byte)(total_length & 0xFF);
    }

    public byte getPacketID(){
        return pack_id_;
    }

    public byte getSrcAddr(){
        return src_addr_;
    }

    public byte getDestAddr(){
        return dest_addr_;
    }

    public byte getType(){
        return type_;
    }

    // return -1 on error
    public byte getACKPacketID(){
        if (type_ == TYPE_ACK){
            return ack_pack_id_;
        }
        else{
            return -1;
        }
    }

    // return -1 on error
    public int getTotalLength(){
        if (type_ == TYPE_INIT){
            return total_length_;
        }
        else{
            return -1;
        }
    }

    // return -1 on error
    public byte getOffset(){
        if (type_ == TYPE_DATA){
            return offset_;
        }
        else{
            return -1;
        }
    }

    // return empty array on error
    public byte[] getData(){
        if (type_ == TYPE_DATA){
            return data_;
        }
        else{
            return new byte[0];
        }
    }

    public int getResendCounter(){
        return resend_counter_;
    }

    public void onResendOnce(){
        resend_counter_ ++;
    }

    public double getTimeStamp(){
        return timestamp_;
    }

    public void setTimeStamp(double new_timestamp){
        timestamp_ = new_timestamp;
    }

    public int getStatus(){
        return status_;
    }

    public void setStatus(int status){
        status_ = status;
    }

    // encode all fields to a String
    byte[] toArray(){
        byte[] frame = new byte[data_field_.length + 2];
        frame[0] = (byte)(((dest_addr_ & 0x03) << 6) | ((src_addr_ & 0x03) << 4) | (type_ & 0x0F));
        frame[1] = pack_id_;
        System.arraycopy(data_field_,0,frame,2,data_field_.length);
        return frame;
    }

    // decode the data field only
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

    public static void main(String[] args){
        // create a new package
        MacPacket pack_1 = new MacPacket((byte)2,(byte)1,(byte)0,5000 );
        byte[] pack_1_str = pack_1.toArray();

    }
}
