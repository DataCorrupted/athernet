#include <iostream>
#include "TCPServer.h"

int main(int argc, const char * argv[]) {
    if (argc != 2){
        std::cerr << "[ERROR] invalid usage" << std::endl;
        return 1;
    }

    int server_port = atoi(argv[1]);

    // create a new TCP server
    TCPServer server(server_port);
    std::cout << "[INFO] Server started" << std::endl;

    // receive 10 frames
    for (int i = 0; i < 10; i++){
        ReceivedData recv_data = server.recv_data();
        printf("[INFO] Received data. src_ip = %s, src_port = %d, dest_port = %d, content: %s\n",
               recv_data.get_src_ip().c_str(), recv_data.get_src_port(), server_port, recv_data.get_content().c_str());
    }
    return 0;
}