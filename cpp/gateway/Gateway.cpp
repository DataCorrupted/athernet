//
// Created by ernest on 18-6-2.
//

#include "Gateway.h"


Gateway::Gateway(bool is_icmp, bool is_tcp): udp_client_(NULL), udp_server_(NULL), icmp_client_(NULL),
                                             is_icmp_(is_icmp), tcp_client_(NULL), tcp_server_(NULL) {
    if (is_icmp){
        // ICMP need to call init_icmp
        return;
    }
    else{
        if (!is_tcp){
            udp_server_ = new UDPServer(8889);
        }
        else{
            tcp_server_ = NULL;
        }
    }
}

Gateway::~Gateway() {
    if (udp_client_ != NULL){
        delete udp_client_;
    }

    if (udp_server_ != NULL){
        delete udp_server_;
    }

    if (tcp_client_ != NULL){
        delete tcp_client_;
    }

    if (tcp_server_ != NULL){
        delete tcp_server_;
    }

    if (icmp_client_ != NULL){
        delete icmp_client_;
    }
}

void Gateway::icmp_init(boost::asio::io_service &io_service) {
    if (!is_icmp_){
        std::cerr << "[ERROR] initialize a Non-ICMP Client as ICMP Client" <<std::endl;
        throw  std::runtime_error("Initialize a Non-ICMP Client as ICMP Client");
    }
    icmp_client_ = new ICMPClient(io_service);
}

bool Gateway::nat_send(std::string ip, int port, std::string content) {
    // TODO: check if the packet is ICMP packet
    if (port == 0){
        // deal with ICMP
        std::cerr << "[DEBUG, Gateway.cpp] Sending ICMP Packet." << std::endl;
        icmp_client_->send_data(ip,content);
        std::cerr << "[DEBUG, Gateway.cpp] ICMP Packet Sent." << std::endl;
    }
    else{
        if (!is_tcp_) {
            // deal with UDP
            // if client has not been initialized, initialize it now
            if (udp_client_ == NULL) {
                udp_client_ = new UDPClient(ip, port);
            }
            return udp_client_->send_data(content);
        }
        else{
            // deal with UDP
            // if client has not been initialized, initialize it now
            if (tcp_client_ == NULL) {
                tcp_client_ = new TCPClient(ip, port);
            }
            return tcp_client_->send_data(content);
        }
    }
}

ReceivedData Gateway::nat_recv() {
//    std::cerr << "[DEBUG, nat_recv] nat_recv got called once" << std::endl;
    if (is_icmp_) {
        return icmp_client_->recv_data();
    }
    else if (!is_tcp_){
        return udp_server_->recv_data();
    }
    else{
        if (tcp_server_ == NULL){
            std::cerr << "[DEBUG, nat_recv] server not initialized, initializing " << std::endl;
            tcp_server_ = new TCPServer(8889);
            std::cerr << "[DEBUG, nat_recv] server initialized " << std::endl;
        }
        return tcp_server_->recv_data();
    }
}
