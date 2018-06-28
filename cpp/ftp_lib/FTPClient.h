
#ifndef UDP_FTPCLIENT_H
#define UDP_FTPCLIENT_H

#include <iostream>
#include <thread>
#include <mutex>
#include <sstream>
#include <queue>
#include "ReceivedData.h"
#include "TCPClient.h"
#include "NatPacket.h"

class FTPClient {
public:
    FTPClient(const std::string& ip, unsigned int port);

    ~FTPClient();

    // wait for the child to finish
    int wait();

    // send the command: user, pass, pwd
    bool cmd_user(std::string username);
    bool cmd_pass(std::string password);
    bool cmd_pwd();
    bool cmd_cwd(std::string pathname);
    bool cmd_pasv();
    bool cmd_list(std::string pathname="");
    bool cmd_retr(std::string pathname);

    ReceivedData nat_recv();

private:
    int receiving_and_disp();
    int receiving_data_and_disp();

    // a mutex lock to prevent race condition between tcp_client and data_client
    std::mutex mutex_;

    bool is_shutdown_;

    TCPClient * control_client_;
    TCPClient * data_client_;

    std::thread control_child_;
    std::thread data_child_;

    bool data_ip_set_;
    bool data_child_initized;

    // for storing the result
    std::queue<ReceivedData> recv_packets_;

    // some helper variablers
    std::string control_ip_;
    unsigned int control_port_;
    std::string data_ip_;
    unsigned int data_port_;
};


#endif //UDP_FTPCLIENT_H
