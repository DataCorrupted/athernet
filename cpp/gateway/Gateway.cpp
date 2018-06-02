//
// Created by ernest on 18-6-2.
//

#include "Gateway.h"


Gateway::Gateway(): client_(NULL) {}

Gateway::~Gateway() {
    if (client_ != NULL){
        delete client_;
    }
}

bool Gateway::nat_send(std::string ip, int port, std::string content) {
    if (client_ == NULL){
        client_ = new UDPClient(ip,port);
    }
    client_->send_data(content);
}