#ifndef UDP_RECEIVEDDATA_H
#define UDP_RECEIVEDDATA_H

#endif //UDP_RECEIVEDDATA_H


class ReceivedData{
public:
    ReceivedData(){
        src_ip_ = "";
        src_port_ = -1;
        content_ = "";
    }

    void set_src_ip(std::string src_ip){src_ip_ = src_ip;}
    void set_src_port(int src_port){src_port_ = src_port;}
    void set_content(std::string content){content_ = content;}
    std::string get_src_ip(){return src_ip_;}
    int get_src_port(){return src_port_;}
    std::string get_content(){ return content_;}

private:
    std::string src_ip_;
    int src_port_;
    std::string content_;
};