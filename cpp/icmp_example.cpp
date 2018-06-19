//
// Created by ernest on 18-6-3.
//

#include <boost/asio.hpp>

#include "icmp_header.hpp"
#include "ipv4_header.hpp"

#include "ICMPClient.h"
#include "Gateway.h"

int main(int argc, const char * argv[]){
//    boost::asio::io_service io_service;
//    ICMPClient client(io_service);
//
//    client.send_data("220.181.111.188","Hello World");
//    ReceivedData recv_data = client.recv_data();
//    std::cerr << "[INFO] src_ip: " << recv_data.get_src_ip() << std::endl;
//    std::cerr << "[INFO] src_port: " << recv_data.get_src_port() << std::endl;
//    std::cerr << "[INFO] content: " << recv_data.get_content() << std::endl;

    Gateway gateway_(true, false);
    boost::asio::io_service io_service;
    gateway_.icmp_init(io_service);
    gateway_.nat_send("220.181.111.188",0,"Hello World");
    ReceivedData recv_data = gateway_.nat_recv();

    return 0;
}