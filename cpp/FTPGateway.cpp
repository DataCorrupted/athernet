#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <iostream>
#include <cassert>
#include <unistd.h>
#include "Gateway.h"
#include <thread>

using namespace std;

Gateway* gateway;
boost::asio::io_service io_service;

int getUnsignedByte(){
    int tmp;
    cin >> tmp;
    return tmp & 0xff;
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

    // Call all the FTP functions.
    //
    //
    //

    sleep(0.25);
}

void receive(){
    ReceivedData received = gateway->nat_recv();
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
    gateway = new Gateway(true, false);

    std::thread send_thread(infSend);
    std::thread recv_thread(infRecv);

    send_thread.join();
    recv_thread.join();

    delete gateway;
    return 0;
}