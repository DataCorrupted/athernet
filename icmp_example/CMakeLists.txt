cmake_minimum_required(VERSION 2.8 FATAL_ERROR)
project(icmp_example)

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread")

find_package(Boost 1.40.0 REQUIRED system)
include_directories(${Boost_INCLUDE_DIRS})

add_executable(icmp_example main.cpp )
target_link_libraries(icmp_example ${Boost_LIBRARIES} ${Boost_FILESYSTEM_LIBRARY} ${Boost_SYSTEM_LIBRARY})