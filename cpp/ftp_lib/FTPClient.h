
#ifndef UDP_FTPCLIENT_H
#define UDP_FTPCLIENT_H

#include <iostream>
#include <thread>
#include "TCPClient.h"

class FTPClient {
public:
    FTPClient(const std::string& ip, int port);

    ~FTPClient();

    // wait for the child to finish
    int wait();

    // send the command: user, pass, pwd
    bool cmd_user(std::string username);
    bool cmd_pass(std::string password);
    bool cmd_pwd();
    bool cmd_cwd(std::string pathname);
    bool cmd_pasv();

private:
    int receiving_and_disp();

    bool is_shutdown_;

    TCPClient * tcp_client_;

    std::thread child_;
};


#endif //UDP_FTPCLIENT_H
