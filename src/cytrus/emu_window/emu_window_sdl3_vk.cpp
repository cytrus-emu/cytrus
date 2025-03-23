// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#include <cstdlib>
#include <memory>
#include <string>
#include <SDL3/SDL.h>
#include <fmt/format.h>
#include "common/logging/log.h"
#include "common/scm_rev.h"
#include "core/frontend/emu_window.h"
#include "cytrus/emu_window/emu_window_sdl3_vk.h"

#if defined(SDL_PLATFORM_WIN32)
#include <Windows.h>
#elif defined(SDL_PLATFORM_LINUX) // TODO: check if this is correct
#if defined(DISPLAY_X11)
#include <X11/Xlib.h>
#else
#include <wayland-client.h>
#endif
#elif defined(SDL_PLATFORM_ANDROID)
#include <EGL/egl.h>
#endif

class DummyContext : public Frontend::GraphicsContext {};

EmuWindow_SDL3_VK::EmuWindow_SDL3_VK(Core::System& system, bool fullscreen, bool is_secondary)
    : EmuWindow_SDL3{system, is_secondary} {
    const std::string window_title = fmt::format("Cytrus {} | {}-{}", Common::g_build_fullname,
                                                 Common::g_scm_branch, Common::g_scm_desc);
    render_window =
        SDL_CreateWindow(window_title.c_str(), Core::kScreenTopWidth,
                         Core::kScreenTopHeight + Core::kScreenBottomHeight,
                         SDL_WINDOW_OPENGL | SDL_WINDOW_RESIZABLE | SDL_WINDOW_HIGH_PIXEL_DENSITY);

    if (fullscreen) {
        Fullscreen();
        SDL_HideCursor();
    }

#if defined(SDL_PLATFORM_WIN32)
    HWND hwnd = (HWND)SDL_GetPointerProperty(SDL_GetWindowProperties(render_window),
                                             SDL_PROP_WINDOW_WIN32_HWND_POINTER, NULL);
    window_info.type = Frontend::WindowSystemType::Windows;
    window_info.render_surface = reinterpret_cast<void*>(hwnd);
#elif defined(SDL_PLATFORM_LINUX)
    if (SDL_strcmp(SDL_GetCurrentVideoDriver(), "x11") == 0) {
        Display* xdisplay = (Display*)SDL_GetPointerProperty(
            SDL_GetWindowProperties(window), SDL_PROP_WINDOW_X11_DISPLAY_POINTER, NULL);
        Window xwindow = (Window)SDL_GetNumberProperty(SDL_GetWindowProperties(window),
                                                       SDL_PROP_WINDOW_X11_WINDOW_NUMBER, 0);
        window_info.type = Frontend::WindowSystemType::X11;
        window_info.display_connection = xdisplay;
        window_info.render_surface = reinterpret_cast<void*>(xwindow);
    } else if (SDL_strcmp(SDL_GetCurrentVideoDriver(), "wayland") == 0) {
        struct wl_display* display = (struct wl_display*)SDL_GetPointerProperty(
            SDL_GetWindowProperties(window), SDL_PROP_WINDOW_WAYLAND_DISPLAY_POINTER, NULL);
        struct wl_surface* surface = (struct wl_surface*)SDL_GetPointerProperty(
            SDL_GetWindowProperties(window), SDL_PROP_WINDOW_WAYLAND_SURFACE_POINTER, NULL);
        window_info.type = Frontend::WindowSystemType::Wayland;
        window_info.display_connection = display;
        window_info.render_surface = reinterpret_cast<void*>(surface);
    }
#elif defined(SDL_PLATFORM_MACOS)
    window_info.type = Frontend::WindowSystemType::MacOS;
    window_info.render_surface = SDL_Metal_GetLayer(SDL_Metal_CreateView(render_window));
#elif defined(SDL_PLATFORM_ANDROID)
    EGLSurface surface = (EGLSurface)SDL_GetPointerProperty(
        SDL_GetWindowProperties(render_window), SDL_PROP_WINDOW_ANDROID_SURFACE_POINTER, NULL);
    window_info.type = Frontend::WindowSystemType::Android;
    window_info.render_surface = reinterpret_cast<void*>(surface);
#else
    LOG_CRITICAL(Frontend, "Window manager subsystem {} not implemented", wm.subsystem);
    std::exit(EXIT_FAILURE);
    break;
#endif

    render_window_id = SDL_GetWindowID(render_window);

    OnResize();
    OnMinimalClientAreaChangeRequest(GetActiveConfig().min_client_area_size);
    SDL_PumpEvents();
}

EmuWindow_SDL3_VK::~EmuWindow_SDL3_VK() = default;

std::unique_ptr<Frontend::GraphicsContext> EmuWindow_SDL3_VK::CreateSharedContext() const {
    return std::make_unique<DummyContext>();
}
