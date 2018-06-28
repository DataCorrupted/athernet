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
    std::cerr << "[INFO] USER: anonymous" << std::endl;
    client->cmd_user("anonymous");
    sleep(2);
    std::cerr << "[INFO] PASS: anonymous" << std::endl;
    client->cmd_pass("anonymous");
    sleep(2);
    std::cerr << "[INFO] PWD " << std::endl;
    client->cmd_pwd();
    sleep(2);

    std::cerr << "[INFO] PASV" << std::endl;
    client->cmd_pasv();
    sleep(1);

    std::cerr << "[INFO] LIST" << std::endl;
    client->cmd_list("");
    sleep(2);

    std::cerr << "[INFO] CWD: Writeable" << std::endl;
    client->cmd_cwd("Writeable");
    sleep(2);
    std::cerr << "[INFO] PWD" << std::endl;
    client->cmd_pwd();
    sleep(2);
    std::cerr << "[INFO] PASV" << std::endl;
    client->cmd_pasv();
    sleep(1);

    std::cerr << "[INFO] RETR" << std::endl;
    client->cmd_retr("README.md");
    sleep(2);

//    std::cerr << "[INFO] LIST" << std::endl;
//    client->cmd_list("/");
//    sleep(2);
//
//    std::cerr << "[INFO] PASV" << std::endl;
//    client->cmd_pasv();
//    sleep(2);
//
//    std::cerr << "[INFO] duplicated PASV" << std::endl;
//    client->cmd_pasv();
//    sleep(2);
//
//    std::cerr << "[INFO] LIST" << std::endl;
//    client->cmd_list("/");
//    sleep(2);


    std::cerr << "[INFO] DONE" << std::endl;
//    std::cerr << "[INFO] PASV" << std::endl;
//    client->cmd_pasv();
//    sleep(2);
//
//    std::cerr << "[INFO] RETR: robots.txt" << std::endl;
//    client->cmd_retr("robots.txt");
//    sleep(2);


    sleep(20);
    client->wait();

    return 0;
}