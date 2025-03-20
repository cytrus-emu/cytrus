set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${PROJECT_SOURCE_DIR}/CMakeModules)

add_executable(cytrus
    cytrus.cpp
    cytrus.rc
    config.cpp
    config.h
    default_ini.h
    emu_window/emu_window_sdl2.cpp
    emu_window/emu_window_sdl2.h
    precompiled_headers.h
    resource.h
)

if (ENABLE_SOFTWARE_RENDERER)
    target_sources(cytrus PRIVATE
        emu_window/emu_window_sdl2_sw.cpp
        emu_window/emu_window_sdl2_sw.h
    )
endif()
if (ENABLE_OPENGL)
    target_sources(cytrus PRIVATE
        emu_window/emu_window_sdl2_gl.cpp
        emu_window/emu_window_sdl2_gl.h
    )
endif()
if (ENABLE_VULKAN)
    target_sources(cytrus PRIVATE
        emu_window/emu_window_sdl2_vk.cpp
        emu_window/emu_window_sdl2_vk.h
    )
endif()

create_target_directory_groups(cytrus)

target_link_libraries(cytrus PRIVATE cytrus_common cytrus_core input_common network)
target_link_libraries(cytrus PRIVATE inih)
if (MSVC)
    target_link_libraries(cytrus PRIVATE getopt)
endif()
target_link_libraries(cytrus PRIVATE ${PLATFORM_LIBRARIES} SDL3::SDL3 Threads::Threads)

if (ENABLE_OPENGL)
    target_link_libraries(cytrus PRIVATE glad)
endif()

if(UNIX AND NOT APPLE)
    install(TARGETS cytrus RUNTIME DESTINATION "${CMAKE_INSTALL_PREFIX}/bin")
endif()

if (CYTRUS_USE_PRECOMPILED_HEADERS)
    target_precompile_headers(cytrus PRIVATE precompiled_headers.h)
endif()

# Bundle in-place on MSVC so dependencies can be resolved by builds.
if (MSVC)
    include(BundleTarget)
    bundle_target_in_place(cytrus)
endif()
