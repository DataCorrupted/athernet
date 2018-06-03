cmake_minimum_required(VERSION 2.8)
project(udp)

set(CMAKE_CXX_STANDARD 11)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -Wall -pthread")

find_package(Boost 1.40.0 REQUIRED system)
include_directories(${Boost_INCLUDE_DIRS})


file(GLOB UDP_SRC udp_lib/*(.h|.cpp))
file(GLOB NAT_SRC nat_packet/*(.h|.cpp))
file(GLOB GATE_SRC gateway/*(.h|.cpp))
file(GLOB ICMP_SRC icmp_client/*(.h|.cpp))
file(GLOB COMMON_SRC common_lib/*(.h|.cpp))


include_directories(udp_lib)
include_directories(nat_packet)
include_directories(gateway)
include_directories(icmp_client)
include_directories(common_lib)


add_executable(udp_client udp_client.cpp ${UDP_SRC} ${NAT_SRC} ${COMMON_SRC})
add_executable(udp_server udp_server.cpp ${UDP_SRC} ${NAT_SRC} ${COMMON_SRC})
add_executable(nat_client nat_client.cpp ${UDP_SRC} ${COMMON_SRC})

add_executable(gateway gateway.cpp ${UDP_SRC} ${NAT_SRC} ${GATE_SRC} ${COMMON_SRC} ${ICMP_SRC})
target_link_libraries(gateway ${Boost_LIBRARIES} ${Boost_FILESYSTEM_LIBRARY} ${Boost_SYSTEM_LIBRARY})


add_executable(icmp_example icmp_example.cpp ${ICMP_SRC} ${COMMON_SRC})
target_link_libraries(icmp_example ${Boost_LIBRARIES} ${Boost_FILESYSTEM_LIBRARY} ${Boost_SYSTEM_LIBRARY})

#------------------- For test only----------------------
add_executable(nat_test test/nat_test.cpp ${NAT_SRC})