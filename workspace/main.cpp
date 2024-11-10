#include <iostream>
#include <cstdio>
#include <memory>
#include <array>

std::string exec(const char* cmd) {
    std::array<char, 128> buffer;
    std::string result;

    // Open pipe to read the output of the command
    std::unique_ptr<FILE, decltype(&pclose)> pipe(popen(cmd, "r"), pclose);
    if (!pipe) {
        throw std::runtime_error("popen() failed!");
    }

    // Read the output from the command
    while (fgets(buffer.data(), buffer.size(), pipe.get()) != nullptr) {
        result += buffer.data();
    }

    return result;
}

int main() {
    std::string user = exec("whoami");
    std::cout << "Current user: " << user;

    return 0;
}