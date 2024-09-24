set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${PROJECT_SOURCE_DIR}/CMakeModules)

add_executable(cytrus-room
    precompiled_headers.h
    cytrus-room.cpp
    cytrus-room.rc
)

create_target_directory_groups(cytrus-room)

target_link_libraries(cytrus-room PRIVATE cytrus_common network)
if (ENABLE_WEB_SERVICE)
    target_link_libraries(cytrus-room PRIVATE web_service)
endif()

target_link_libraries(cytrus-room PRIVATE cryptopp)
if (MSVC)
    target_link_libraries(cytrus-room PRIVATE getopt)
endif()
target_link_libraries(cytrus-room PRIVATE ${PLATFORM_LIBRARIES} Threads::Threads)

if(UNIX AND NOT APPLE)
    install(TARGETS cytrus-room RUNTIME DESTINATION "${CMAKE_INSTALL_PREFIX}/bin")
endif()

if (CYTRUS_USE_PRECOMPILED_HEADERS)
    target_precompile_headers(cytrus-room PRIVATE precompiled_headers.h)
endif()

# Bundle in-place on MSVC so dependencies can be resolved by builds.
if (MSVC)
    include(BundleTarget)
    bundle_target_in_place(cytrus-room)
endif()
