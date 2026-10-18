cmake_minimum_required(VERSION 3.16)

# Components under test live in the firmware project, so they are shared with it
set(EXTRA_COMPONENT_DIRS ../components)

# The linux target cannot resolve every IDF component, so build only main and its dependencies
set(COMPONENTS main)

include($ENV{IDF_PATH}/tools/cmake/project.cmake)
project(vehicle-tracker-embedded-test)
