cmake_minimum_required(VERSION 2.8)
project(udp)

set(CMAKE_CXX_STANDARD 11)

file(GLOB UDP_SRC udp_lib/*(.h|.cpp))


include_directories(udp_lib)


add_executable(udp_client udp_client.cpp ${UDP_SRC} udp_lib/UDPClient.cpp udp_lib/UDPClient.h)
add_executable(udp_server udp_server.cpp ${UDP_SRC})