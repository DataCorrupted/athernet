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

Gateway gateway;

int getUnsignedByte(){
    return (int)(getchar() & 0xff);
}
void send(){
    // Receive a pack from athernet.
    int len = getUnsignedByte() << 8 + getUnsignedByte();
    int data[len];
    for (int i=0; i<len; i++){
        data[len] = getUnsignedByte();
    }

    // convert int to std::string
    char buf[len];
    for (int i = 0; i < len; i++){
        buf[i] = (char)data[i];
    }
    std::string encoded_frame = std::string(buf,len);

    // Make a UDP Packet.
    // IP: data[0].data[1].data[2].data[3]
    // Port: data[4] << 8 + data[5]
    // Data: data[6:]
    NatPacket nat_pack(encoded_frame);


    // Send the UDP Packet.
    gateway.nat_send(nat_pack.get_ip(), nat_pack.get_port(), nat_pack.get_content());
}

int main(int argc, char *argv[]){
    while (1){
        send();
    }

    return 0;
}