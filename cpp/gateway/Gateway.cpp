//
// Created by ernest on 18-6-2.
//

#include "Gateway.h"


Gateway::Gateway(): client_(NULL), server_(NULL) {
    // initialize a UDP server on port 8889
    server_ = new UDPServer(8889);
}

Gateway::~Gateway() {
    if (client_ != NULL){
        delete client_;
    }

    if (server_ != NULL){
        delete server_;
    }
}

bool Gateway::nat_send(std::string ip, int port, std::string content) {
    if (client_ == NULL){
        client_ = new UDPClient(ip,port);
    }
    client_->send_data(content);
}

ReceivedData Gateway::nat_recv() {
//    std::cerr << "[DEBUG, nat_recv] nat_recv got called once" << std::endl;
    return server_->recv_data();
}
