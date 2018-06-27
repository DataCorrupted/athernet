//
// Created by ernest on 18-6-27.
//
#include "FTPClient.h"

FTPClient::FTPClient(const std::string &ip, int port):tcp_client_(NULL), is_shutdown_(false) {
    tcp_client_ = new TCPClient(ip,port);
    child_ = std::thread(&FTPClient::receiving_and_disp,this);
}

FTPClient::~FTPClient() {
    delete tcp_client_;
}



bool FTPClient::cmd_user(std::string username) {
    std::string content = "USER ";
    content = content + username + "\n";
    if (tcp_client_->send_data(content)){
        std::cerr << "[INFO, FTPClient.cpp] cmd USER sent" << std::endl;
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_user: failed" << std::endl;
        return false;
    }
}

bool FTPClient::cmd_pwd() {
    std::string content = "PWD \n";
    if (tcp_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_pwd: failed" << std::endl;
        return false;
    }
}

bool FTPClient::cmd_pass(std::string password) {
    std::string content = "PASS ";
    content = content + password + "\n";
    if (tcp_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_pass: failed" << std::endl;
        return false;
    }
}



int FTPClient::wait() {
    child_.join();
}

int FTPClient::receiving_and_disp(){
    while(!is_shutdown_){
        std::string recv_reply = tcp_client_->recv_data().get_content();
        std::cerr << "[INFO] received: " << recv_reply << std::endl;
    }
    return 0;
}