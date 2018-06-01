#include <iostream>
#include "UDPServer.h"

int main() {
    // create a new UDP server
    UDPServer server(8888);

    // receive 10 frames
    for (int i = 0; i < 10; i++){
        ReceivedData recv_data = server.recv_data();
        printf("[INFO] Received data. src_ip = %s, src_port = %d, dest_port = %d, content: %s\n",
               recv_data.get_src_ip().c_str(), recv_data.get_src_port(), 8888, recv_data.get_content().c_str());
    }
    return 0;
}