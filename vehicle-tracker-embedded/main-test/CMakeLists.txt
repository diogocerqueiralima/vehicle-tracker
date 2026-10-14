cmake_minimum_required(VERSION 3.16)

# Components under test live in the firmware project, so they are shared with it
set(EXTRA_COMPONENT_DIRS ../components)

# The linux target cannot resolve every IDF component, so build only main and its dependencies
set(COMPONENTS main)

# mbedTLS PSA references symbols from archives the linker reads later, so wrap the components in a link group
# that lets the linker resolve them in any order
set(__PROJECT_GROUP_LINK_COMPONENTS ON)

include($ENV{IDF_PATH}/tools/cmake/project.cmake)
project(vehicle-tracker-embedded-test)
