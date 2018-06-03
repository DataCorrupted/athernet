#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <iostream>
#include <cassert>
#include <unistd.h>
#include "Gateway.h"

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
    std::cerr << nat_pack.get_ip() << std::endl;

    std::cerr << "[DEBUG, gateway] decode NAT compelted" << std::endl;

    // Send the UDP Packet.
    gateway->nat_send(nat_pack.get_ip(), nat_pack.get_port(), nat_pack.get_content());
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

int main(int argc, char *argv[]){
    if (argc < 2) {
        std::cerr << "Please provide enough args." << endl;        
    } 

    bool ping = std::string(argv[1]) == "ping";
    bool is_tcp = ((argc >=3) && (std::string(argv[2]) == "tcp"));
    gateway = new Gateway(ping, is_tcp);

    if (ping){
        std::cerr << "Using ICMP to ping." << endl;
        gateway->icmp_init(io_service);
    }
    if (is_tcp){
        std::cerr << "Using TCP." << endl;
    }
    

    if (std::string(argv[1]) == "toAthernet"){
        while (1){ receive(); }
    } else if (std::string(argv[1]) == "toInternet"){
        while (1){ send(); }        
    } else if (ping) {
        while (1){ 
            send();
            receive();
        }
    } else {
        std::cerr << "Unrecognized args.\n";
    }

    delete gateway;
    return 0;
}