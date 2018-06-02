//
// Created by ernest on 18-6-2.
//

#include "Gateway.h"


Gateway::Gateway(bool is_icmp): client_(NULL), server_(NULL), icmp_client_(NULL), is_icmp_(is_icmp) {
    if (!is_icmp){
        // initialize a UDP server on port 8889 (if it is not icmp_server)
        server_ = new UDPServer(8889);
    }
}

Gateway::~Gateway() {
    if (client_ != NULL){
        delete client_;
    }

    if (server_ != NULL){
        delete server_;
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

    // if client has not been initialized, initialize it now
    if (client_ == NULL){
        client_ = new UDPClient(ip,port);
    }
    return client_->send_data(content);
}

ReceivedData Gateway::nat_recv() {
//    std::cerr << "[DEBUG, nat_recv] nat_recv got called once" << std::endl;
    if (!is_icmp_) {
        return server_->recv_data();
    }
    else{
        return icmp_client_->recv_data();
    }
}
