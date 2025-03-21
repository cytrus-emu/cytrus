// Copyright 2016 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#include <algorithm>
#include <cstdlib>
#include <string>
#define SDL_MAIN_HANDLED
#include <SDL3/SDL.h>
#include <SDL3/SDL_main.h>
#include "common/logging/log.h"
#include "common/scm_rev.h"
#include "core/core.h"
#include "cytrus/emu_window/emu_window_sdl3.h"
#include "input_common/keyboard.h"
#include "input_common/main.h"
#include "input_common/motion_emu.h"
#include "network/network.h"

void EmuWindow_SDL3::OnMouseMotion(float x, float y) {
    TouchMoved((unsigned)std::max(x, 0.f), (unsigned)std::max(y, 0.f));
    InputCommon::GetMotionEmu()->Tilt((int)x, (int)y);
}

void EmuWindow_SDL3::OnMouseButton(u32 button, bool state, float x, float y) {
    if (button == SDL_BUTTON_LEFT) {
        if (state == true) {
            TouchPressed((unsigned)std::max(x, 0.f), (unsigned)std::max(y, 0.f));
        } else {
            TouchReleased();
        }
    } else if (button == SDL_BUTTON_RIGHT) {
        if (state == true) {
            InputCommon::GetMotionEmu()->BeginTilt((int)x, (int)y);
        } else {
            InputCommon::GetMotionEmu()->EndTilt();
        }
    }
}

std::pair<unsigned, unsigned> EmuWindow_SDL3::TouchToPixelPos(float touch_x, float touch_y) const {
    int w, h;
    SDL_GetWindowSize(render_window, &w, &h);

    touch_x *= w;
    touch_y *= h;

    return {static_cast<unsigned>(std::max(std::round(touch_x), 0.0f)),
            static_cast<unsigned>(std::max(std::round(touch_y), 0.0f))};
}

void EmuWindow_SDL3::OnFingerDown(float x, float y) {
    // TODO(NeatNit): keep track of multitouch using the fingerID and a dictionary of some kind
    // This isn't critical because the best we can do when we have that is to average them, like the
    // 3DS does

    const auto [px, py] = TouchToPixelPos(x, y);
    TouchPressed(px, py);
}

void EmuWindow_SDL3::OnFingerMotion(float x, float y) {
    const auto [px, py] = TouchToPixelPos(x, y);
    TouchMoved(px, py);
}

void EmuWindow_SDL3::OnFingerUp() {
    TouchReleased();
}

void EmuWindow_SDL3::OnKeyEvent(int key, bool state) {
    if (state == true) {
        InputCommon::GetKeyboard()->PressKey(key);
    } else if (state == true) {
        InputCommon::GetKeyboard()->ReleaseKey(key);
    }
}

bool EmuWindow_SDL3::IsOpen() const {
    return is_open;
}

void EmuWindow_SDL3::RequestClose() {
    is_open = false;
}

void EmuWindow_SDL3::OnResize() {
    int width, height;
    SDL_GetWindowSizeInPixels(render_window, &width, &height);
    UpdateCurrentFramebufferLayout(width, height);
}

void EmuWindow_SDL3::Fullscreen() {
    if (SDL_SetWindowFullscreen(render_window, SDL_WINDOW_FULLSCREEN) == 0) {
        return;
    }

    LOG_ERROR(Frontend, "Fullscreening failed: {}", SDL_GetError());

    // Try a different fullscreening method
    LOG_INFO(Frontend, "Attempting to use borderless fullscreen...");
    if (SDL_SetWindowFullscreen(render_window,
                                SDL_GetWindowFullscreenMode(render_window) != nullptr) == 0) {
        return;
    }

    LOG_ERROR(Frontend, "Borderless fullscreening failed: {}", SDL_GetError());

    // Fallback algorithm: Maximise window.
    // Works on all systems (unless something is seriously wrong), so no fallback for this one.
    LOG_INFO(Frontend, "Falling back on a maximised window...");
    SDL_MaximizeWindow(render_window);
}

EmuWindow_SDL3::EmuWindow_SDL3(Core::System& system_, bool is_secondary)
    : EmuWindow(is_secondary), system(system_) {}

EmuWindow_SDL3::~EmuWindow_SDL3() {
    SDL_Quit();
}

void EmuWindow_SDL3::InitializeSDL3() {
    if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_GAMEPAD) == false) {
        LOG_CRITICAL(Frontend, "Failed to initialize SDL3: {}! Exiting...", SDL_GetError());
        exit(1);
    }

    InputCommon::Init();
    Network::Init();

    SDL_SetMainReady();
}

u32 EmuWindow_SDL3::GetEventWindowId(const SDL_Event& event) const {
    switch (event.type) {
    case SDL_EVENT_WINDOW_SHOWN:
    case SDL_EVENT_WINDOW_HIDDEN:
    case SDL_EVENT_WINDOW_EXPOSED:
    case SDL_EVENT_WINDOW_MOVED:
    case SDL_EVENT_WINDOW_RESIZED:
    case SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED:
    case SDL_EVENT_WINDOW_METAL_VIEW_RESIZED:
    case SDL_EVENT_WINDOW_MINIMIZED:
    case SDL_EVENT_WINDOW_MAXIMIZED:
    case SDL_EVENT_WINDOW_RESTORED:
    case SDL_EVENT_WINDOW_MOUSE_ENTER:
    case SDL_EVENT_WINDOW_MOUSE_LEAVE:
    case SDL_EVENT_WINDOW_FOCUS_GAINED:
    case SDL_EVENT_WINDOW_FOCUS_LOST:
    case SDL_EVENT_WINDOW_CLOSE_REQUESTED:
    case SDL_EVENT_WINDOW_HIT_TEST:
    case SDL_EVENT_WINDOW_ICCPROF_CHANGED:
    case SDL_EVENT_WINDOW_DISPLAY_CHANGED:
    case SDL_EVENT_WINDOW_DISPLAY_SCALE_CHANGED:
    case SDL_EVENT_WINDOW_SAFE_AREA_CHANGED:
    case SDL_EVENT_WINDOW_OCCLUDED:
    case SDL_EVENT_WINDOW_ENTER_FULLSCREEN:
    case SDL_EVENT_WINDOW_LEAVE_FULLSCREEN:
    case SDL_EVENT_WINDOW_DESTROYED:
    case SDL_EVENT_WINDOW_HDR_STATE_CHANGED:
        return event.window.windowID;
    case SDL_EVENT_KEY_DOWN:
    case SDL_EVENT_KEY_UP:
        return event.key.windowID;
    case SDL_EVENT_MOUSE_MOTION:
        return event.motion.windowID;
    case SDL_EVENT_MOUSE_BUTTON_DOWN:
    case SDL_EVENT_MOUSE_BUTTON_UP:
        return event.button.windowID;
    case SDL_EVENT_MOUSE_WHEEL:
        return event.wheel.windowID;
    case SDL_EVENT_FINGER_DOWN:
    case SDL_EVENT_FINGER_MOTION:
    case SDL_EVENT_FINGER_UP:
        return event.tfinger.windowID;
    case SDL_EVENT_TEXT_EDITING:
        return event.edit.windowID;
    case SDL_EVENT_TEXT_EDITING_CANDIDATES:
        return event.edit_candidates.windowID;
    case SDL_EVENT_TEXT_INPUT:
        return event.text.windowID;
    case SDL_EVENT_DROP_BEGIN:
    case SDL_EVENT_DROP_FILE:
    case SDL_EVENT_DROP_TEXT:
    case SDL_EVENT_DROP_COMPLETE:
        return event.drop.windowID;
    case SDL_EVENT_USER:
        return event.user.windowID;
    default:
        // Event is not for any particular window, so we can just pretend it's for this one.
        return render_window_id;
    }
}

void EmuWindow_SDL3::PollEvents() {
    SDL_Event event;
    std::vector<SDL_Event> other_window_events;

    // SDL_PollEvent returns 0 when there are no more events in the event queue
    while (SDL_PollEvent(&event)) {
        if (GetEventWindowId(event) != render_window_id) {
            other_window_events.push_back(event);
            continue;
        }

        switch (event.type) {
        case SDL_EVENT_WINDOW_PIXEL_SIZE_CHANGED:
        case SDL_EVENT_WINDOW_RESIZED:
        case SDL_EVENT_WINDOW_MAXIMIZED:
        case SDL_EVENT_WINDOW_RESTORED:
        case SDL_EVENT_WINDOW_MINIMIZED:
            OnResize();
            break;
        case SDL_EVENT_WINDOW_CLOSE_REQUESTED:
            RequestClose();
            break;
        case SDL_EVENT_KEY_DOWN:
        case SDL_EVENT_KEY_UP:
            OnKeyEvent(static_cast<int>(event.key.scancode), event.key.down);
            break;
        case SDL_EVENT_MOUSE_MOTION:
            // ignore if it came from touch
            if (event.button.which != SDL_TOUCH_MOUSEID)
                OnMouseMotion(event.motion.x, event.motion.y);
            break;
        case SDL_EVENT_MOUSE_BUTTON_DOWN:
        case SDL_EVENT_MOUSE_BUTTON_UP:
            // ignore if it came from touch
            if (event.button.which != SDL_TOUCH_MOUSEID) {
                OnMouseButton(event.button.button, event.button.down, event.button.x,
                              event.button.y);
            }
            break;
        case SDL_EVENT_FINGER_DOWN:
            OnFingerDown(event.tfinger.x, event.tfinger.y);
            break;
        case SDL_EVENT_FINGER_MOTION:
            OnFingerMotion(event.tfinger.x, event.tfinger.y);
            break;
        case SDL_EVENT_FINGER_UP:
            OnFingerUp();
            break;
        case SDL_EVENT_QUIT:
            RequestClose();
            break;
        default:
            break;
        }
    }
    for (auto& e : other_window_events) {
        // This is a somewhat hacky workaround to re-emit window events meant for another window
        // since SDL_PollEvent() is global but we poll events per window.
        SDL_PushEvent(&e);
    }
    if (!is_secondary) {
        UpdateFramerateCounter();
    }
}

void EmuWindow_SDL3::OnMinimalClientAreaChangeRequest(std::pair<u32, u32> minimal_size) {
    SDL_SetWindowMinimumSize(render_window, minimal_size.first, minimal_size.second);
}

void EmuWindow_SDL3::UpdateFramerateCounter() {
    const u64 current_time = SDL_GetTicks();
    if (current_time > last_time + 2000) {
        const auto results = system.GetAndResetPerfStats();
        const auto title =
            fmt::format("Cytrus {} | {}-{} | FPS: {:.0f} ({:.0f}%)", Common::g_build_fullname,
                        Common::g_scm_branch, Common::g_scm_desc, results.game_fps,
                        results.emulation_speed * 100.0f);
        SDL_SetWindowTitle(render_window, title.c_str());
        last_time = current_time;
    }
}
