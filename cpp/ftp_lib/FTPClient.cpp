//
// Created by ernest on 18-6-27.
//
#include "FTPClient.h"

FTPClient::FTPClient(const std::string &ip, int port):is_shutdown_(false),control_client_(NULL),data_client_(NULL) {
    control_client_ = new TCPClient(ip,port);
    control_child_ = std::thread(&FTPClient::receiving_and_disp,this);
}

FTPClient::~FTPClient() {
    if (data_client_ != NULL){
        delete data_client_;
    }

    delete control_client_;
}



bool FTPClient::cmd_user(std::string username) {
    std::string content = "USER ";
    content = content + username + "\n";
    if (control_client_->send_data(content)){
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
    if (control_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_pwd: failed" << std::endl;
        return false;
    }
}

bool FTPClient::cmd_cwd(std::string pathname) {
    std::string content = "CWD ";
    content = content + pathname + "\n";
    if (control_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_cwd: failed" << std::endl;
        return false;
    }
}

bool FTPClient::cmd_pasv() {
    std::string content = "PASV \n";
    if (control_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_pasv: failed" << std::endl;
        return false;
    }
}

bool FTPClient::cmd_pass(std::string password) {
    std::string content = "PASS ";
    content = content + password + "\n";
    if (control_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_pass: failed" << std::endl;
        return false;
    }
}


bool FTPClient::cmd_list(std::string pathname) {
    std::string content = "LIST ";
    content = content + pathname + "\n";
    if (control_client_->send_data(content)){
        return true;
    }
    else{
        std::cerr << "[INFO, FTPClient.cpp] cmd_list: failed" << std::endl;
        return false;
    }
}



int FTPClient::wait() {
    if (data_client_ != NULL){
        data_child_.join();
    }

    control_child_.join();
}

int FTPClient::receiving_and_disp(){
    while(!is_shutdown_){
        std::string recv_reply = control_client_->recv_data().get_content();
        std::cerr << "[INFO, FTPClient.cpp] control received: " << recv_reply << std::endl;

        mutex_.lock();
        // handling reply for special commands
        // get reply_code
        std::istringstream iss(recv_reply);
        int reply_code ;
        iss >> reply_code;


        // handle PASV
        if (reply_code == 227){
            // get the passive port from the server
            unsigned long ip_1, ip_2, ip_3, ip_4;
            unsigned long port_high, port_low, port;
            if (!sscanf(recv_reply.c_str(), "227 Entering Passive Mode (%lu,%lu,%lu,%lu,%lu,%lu).",
                   &ip_1, &ip_2, &ip_3, &ip_4, &port_high, &port_low)){
                std::cerr << "[ERROR, FTPClient.cpp] invalid reply received when handling PASV reply." << std::endl;
            } else{
                // assemble the port and ip
                port = (port_high << 8) + port_low;
                std::cerr << "[INFO, FTPClient.cpp] passive port: " << port << std::endl;
                std::string ip = std::to_string(ip_1) + "." + std::to_string(ip_2) + "."
                                 + std::to_string(ip_3) + "." + std::to_string(ip_4);
                std::cerr << "[INFO, FTPClient.cpp] passive ip: " << ip << std::endl;

                // connect to the passive port
                data_client_ = new TCPClient(ip,port);
                data_child_ = std::thread(&FTPClient::receiving_data_and_disp, this);

//                // send PORT
//                std::string est_content = "PORT ";
//                est_content = est_content +std::to_string(ip_1) + "," + std::to_string(ip_2)
//                              + "," + std::to_string(ip_3) + "," + std::to_string(ip_4) + "," + std::to_string(port_high)
//                              + "," + std::to_string(port_low) + "\n";
//                std::cerr << "[DEBUG, FTPClient.cpp] est_content: " << est_content << std::endl;
//                if(!data_client_->send_data(est_content)){
//                    std::cerr << "[ERROR, FTPClient.cpp] error encountered in PORT (PASV). " << std::endl;
//                } else{
//                    std::cerr << "[INFO, FTPClient.cpp] PORT (PASV) sent. " << std::endl;
//                }
            }
        }

        mutex_.unlock();

    }
    return 0;
}


int FTPClient::receiving_data_and_disp() {
    while (!is_shutdown_){
        std::string recv_reply = data_client_->recv_data().get_content();

        mutex_.lock();
        std::cerr << "[INFO, FTPClient.cpp] data received: " << recv_reply << std::endl;

        mutex_.unlock();
    }
}