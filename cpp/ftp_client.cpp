#include <iostream>
#include <thread>
#include "FTPClient.h"

// 163.22.12.51 21


int main(int argc, const char * argv[]) {
    if (argc != 3){
        std::cerr << "[ERROR] invalid usage" << std::endl;
        return 1;
    }

    std::string server_ip = std::string(argv[1]);
    int server_port = atoi(argv[2]);

    auto client = new FTPClient(server_ip,server_port);

    // wait for one second for connection establish
    sleep(1);
    client->cmd_user("anonymous");
    sleep(2);
    client->cmd_pass("anonymous");
    sleep(2);
    client->cmd_pwd();

    client->wait();

    return 0;
}