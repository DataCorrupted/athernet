/*
 * Read in file and send to node2
 * Send out txt through UDP
 * argv[1]: the file path
 * argv[2]: server_ip
 * argv[3]: server_port
 */

#include <iostream>
#include <fstream>
#include "UDPClient.h"


int main(int argc, const char * argv[]){
    if (argc != 4){
        std::cerr << "[ERROR] invalid usage." << std::endl;
        return 1;
    }

    std::ifstream input_stream(argv[1]);

    // initialize UDP client
    std::string server_ip = std::string(argv[2]);
    int server_port = atoi(argv[3]);
    std::cerr << "[INFO] server_ip: " << server_ip << std::endl;
    std::cerr << "[INFO] server_port: " << server_port << std::endl;

    UDPClient client(server_ip, server_port);

    while(!input_stream.eof()){
        std::string line;
        std::getline (input_stream,line);

        // send out the data
        client.send_data(line);
        std::cerr << "[INFO] data sent" << std::endl;
    }

    return 0;
}