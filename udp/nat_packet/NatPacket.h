//
// Created by ernest on 18-6-2.
//

#ifndef UDP_NATPACKET_H
#define UDP_NATPACKET_H

#include <string>
#include <iostream>
#include <exception>

class NatPacket {
public:
    NatPacket(std::string ip, int port, std::string content);

    NatPacket(std::string encoded_frame);

    std::string encode_frame();

    std::string get_ip();
    int get_port();
    std::string get_content();

private:

    std::string ip_;
    int port_;
    std::string content_;

};


#endif //UDP_NATPACKET_H
