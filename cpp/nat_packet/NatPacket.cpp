//
// Created by ernest on 18-6-2.
//

/*
 * frame = ip (4 bytes) + port (2 bytes) + content
 */

#include "NatPacket.h"



NatPacket::NatPacket(std::string encoded_frame) {
    if (encoded_frame.length() < 6){
        stderr << "[ERROR] the length of encoded_frame: " << encoded_frame.length() << std::endl;
        throw std::runtime_error("Invalid length of encoded_frame");
    }

    ip_ = std::string(encoded_frame,0,4);

    port_ = ((int)encoded_frame[4] << 8) + (int)encoded_frame[5];

    content_ = std::string(encoded_frame,6,encoded_frame.length()-6);
}

NatPacket::NatPacket(std::string ip, int port, std::string content) {
    ip_ = ip;
    port_ = port;
    content_ = content;
}


std::string NatPacket::encode_frame() {
    std::string out;
    out = out + ip_;

    int port_high = ((port_  >> 8) & 0xFF);
    int port_low = port_ & 0xFF;

    out = out + (char)port_high;
    out = out + (char)port_low;

    out = out + content_;
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