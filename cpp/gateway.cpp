#include <sys/wait.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <iostream>
#include <cassert>
#include <unistd.h>

using namespace std;	
int main(int argc, char *argv[]){
	string str;
	cout << argc << endl;
	cerr << argv[0];
	cin >> str;
	cerr << "I am " << argv[0] << ". I received str from " << str << endl;
	return 0;
}