//
// Created by ernest on 18-6-2.
//

/*
 * frame = ip (4 bytes) + port (2 bytes) + content
 */

#include "NatPacket.h"


NatPacket::NatPacket(std::string encoded_frame) {
    if (encoded_frame.length() < 6){
        std::cerr << "[ERROR] the length of encoded_frame: " << encoded_frame.length() << std::endl;
        throw std::runtime_error("Invalid length of encoded_frame");
    }

    // convert the ip to human-readable string
    ip_ = "";
    std::string ip_raw = std::string(encoded_frame,0,4);
    for (int i = 0; i < 4; i++){
        ip_ = ip_ + std::to_string((int)ip_raw[i] & 0xFF);
        if (i != 3){
            ip_ = ip_ + ".";
        }
    }

    port_ = (((int)encoded_frame[4] & 0xFF) << 8) + ((int)encoded_frame[5] & 0xFF);

    content_ = std::string(encoded_frame,6,encoded_frame.length()-6);
}

NatPacket::NatPacket(std::string ip, int port, std::string content) {
    ip_ = ip;
    port_ = port;
    content_ = content;
}


std::string NatPacket::encode_frame() {
    std::string out;

    // convert ip to 4-byte representation
    std::string buf;
    for (int i = 0; i < ip_.length(); i++){
        if (ip_[i] != '.'){
            buf = buf + ip_[i];
        }

        if ((ip_[i] == '.') || (i == (ip_.length() - 1))){
            out.push_back((char)(atoi(buf.c_str())));
            buf.clear();
        }
    }

    int port_high = ((port_  >> 8) & 0xFF);
    int port_low = port_ & 0xFF;

    out.push_back((char)port_high);
    out.push_back((char)port_low);

    out = out + content_;
    return out;
}

std::string NatPacket::get_content() {
    return content_;
}

int NatPacket::get_port() {
    return port_;
}

std::string NatPacket::get_ip() {
    return ip_;
}