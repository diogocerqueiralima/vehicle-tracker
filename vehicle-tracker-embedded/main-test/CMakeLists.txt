cmake_minimum_required(VERSION 3.16)

# The code uses C23 features (e.g. nullptr), so do not rely on the compiler's default standard
set(CMAKE_C_STANDARD 23)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS ON)

# Components under test live in the firmware project, so they are shared with it
set(EXTRA_COMPONENT_DIRS ../components)

# The linux target cannot resolve every IDF component, so build only main and its dependencies
set(COMPONENTS main)

include($ENV{IDF_PATH}/tools/cmake/project.cmake)
project(vehicle-tracker-embedded-test)
