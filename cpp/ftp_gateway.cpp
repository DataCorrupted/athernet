#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <iostream>
#include <cassert>
#include <unistd.h>
#include "Gateway.h"
#include "FTPClient.h"
#include <thread>
#include <sstream>

using namespace std;


FTPClient * ftp_client_ = NULL;

int getUnsignedByte(){
    int tmp;
    cin >> tmp;
    return tmp & 0xff;
}

void send_ftp_cmd(const std::string& content){
    std::istringstream iss(content);
    std::string cmd;
    iss >> cmd;

    if (cmd == "USER"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling USER" << std::endl;
        std::string username;
        iss >> username;
        ftp_client_->cmd_user(username);
    }
    else if (cmd == "PASS"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling PASS" << std::endl;
        std::string password;
        iss >> password;
        ftp_client_->cmd_pass(password);
    }
    else if (cmd == "PWD"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling PWD" << std::endl;
        ftp_client_->cmd_pwd();
    }
    else if (cmd == "CWD"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling CWD" << std::endl;
        std::string pathname;
        iss >> pathname;
        ftp_client_->cmd_cwd(pathname);
    }
    else if (cmd == "PASV"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling PASV" << std::endl;
        ftp_client_->cmd_pasv();
    }
    else if (cmd == "LIST"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling LIST" << std::endl;
        std::string pathname;
        iss >> pathname;
        ftp_client_->cmd_list(pathname);
    }
    else if (cmd == "RETR"){
        std::cerr << "[INFO, ftp_gateway.cpp] calling RETR" << std::endl;
        std::string pathname;
        iss >> pathname;
        ftp_client_->cmd_retr(pathname);
    }
    else{
        std::cerr << "[ERROR, ftp_gateway.cpp] Invalid Command. " << std::endl;
    }


    // more function to support
}

void send(){
    // Receive a pack from athernet.
    int len = (getUnsignedByte() << 8) + getUnsignedByte();
    int data[len];
    for (int i=0; i<len; i++){
        data[i] = getUnsignedByte();
    }
    if (len > 100 || len <= 0) { return; }
    // convert int to std::string
    char buf[len];
    for (int i = 0; i < len; i++){
        buf[i] = (char)data[i];
    }
    std::string encoded_frame = std::string(buf,len);

    std::cerr << "[DEBUG, gateway] received frame of length: " << len << std::endl;

    // Make a UDP Packet.
    // IP: data[0].data[1].data[2].data[3]
    // Port: data[4] << 8 + data[5]
    // Data: data[6:]
    NatPacket nat_pack(encoded_frame);

    // setup FTP connection if needed
    if (ftp_client_ == NULL) {
        std::cerr << "[INFO] estiblish connection with " << nat_pack.get_ip() << ":" << nat_pack.get_port() << std::endl;
        ftp_client_ = new FTPClient(nat_pack.get_ip(), nat_pack.get_port());
    }

    // Call all the FTP functions.
    send_ftp_cmd(nat_pack.get_content());

    sleep(0.25);
}

void receive(){
    while(ftp_client_ == NULL){
        sleep(0.2);
    }

    ReceivedData received = ftp_client_->nat_recv();
    NatPacket nat_pack(
        received.get_src_ip(), received.get_src_port(), received.get_content());

    int len = 4 + 2 + nat_pack.get_content().size();
    
    std::cerr 
        << "Received a NAT packet from " 
        << received.get_src_ip() << ":" << received.get_src_port() 
        << " with length: " << len << endl;
    cout << len << " " ;
    string data = nat_pack.encode_frame();
    for (int i=0; i<len; i++){
        cout << (unsigned int) (data[i] & 0xff) << " ";
    }
    cout << endl;
}

void infSend(){
    while(true){ send(); }
}

void infRecv(){
    while(true){ receive(); }
}

int main(int argc, char *argv[]){
    ftp_client_ = NULL;

    std::thread send_thread(infSend);
    std::thread recv_thread(infRecv);

    send_thread.join();
    recv_thread.join();

    if (ftp_client_ != NULL) {
        delete ftp_client_;
    }
    return 0;
}