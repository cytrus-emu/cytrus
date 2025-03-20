// Copyright 2016 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#include <string>
#include <vector>
#include <SDL3/SDL.h>
#include "audio_core/audio_types.h"
#include "audio_core/sdl2_sink.h"
#include "common/assert.h"
#include "common/logging/log.h"

namespace AudioCore {

struct SDL2Sink::Impl {
    unsigned int sample_rate = 0;

    SDL_AudioStream* stream = nullptr;

    std::function<void(s16*, std::size_t)> cb;

    static void Callback(void* userdata, SDL_AudioStream* stream, int additional_amount,
                         int total_amount);
};

SDL2Sink::SDL2Sink(std::string device_name) : impl(std::make_unique<Impl>()) {
    if (SDL_Init(SDL_INIT_AUDIO) == false) {
        LOG_CRITICAL(Audio_Sink, "SDL_Init(SDL_INIT_AUDIO) failed with: {}", SDL_GetError());
        impl->stream = nullptr;
        return;
    }

    SDL_AudioSpec desired_audiospec;
    SDL_zero(desired_audiospec);
    desired_audiospec.format = SDL_AUDIO_S16;
    desired_audiospec.channels = 2;
    desired_audiospec.freq = native_sample_rate;
    //desired_audiospec.samples = 512;
    //desired_audiospec.userdata = impl.get();
    //desired_audiospec.callback = &Impl::Callback;

    SDL_AudioSpec obtained_audiospec;
    SDL_zero(obtained_audiospec);

    const char* device = nullptr;
    if (device_name != auto_device_name && !device_name.empty()) {
        device = device_name.c_str();
    }

    impl->stream = SDL_OpenAudioDeviceStream(SDL_AUDIO_DEVICE_DEFAULT_PLAYBACK, &desired_audiospec,
                                       &Impl::Callback, impl.get());
    if (!impl->stream) {
        LOG_CRITICAL(Audio_Sink, "SDL_OpenAUdioDeviceStream failed");
        return;
    }

    impl->sample_rate = obtained_audiospec.freq;

    // SDL2 audio devices start out paused, unpause it:
    SDL_ResumeAudioStreamDevice(impl->stream);
}

SDL2Sink::~SDL2Sink() {
    if (!impl->stream)
        return;

    SDL_DestroyAudioStream(impl->stream);
}

unsigned int SDL2Sink::GetNativeSampleRate() const {
    if (!impl->stream)
        return native_sample_rate;

    return impl->sample_rate;
}

void SDL2Sink::SetCallback(std::function<void(s16*, std::size_t)> cb) {
    impl->cb = cb;
}

void SDL2Sink::Impl::Callback(void* userdata, SDL_AudioStream* stream, int additional_amount,
                              int total_amount) {
    Impl* impl = reinterpret_cast<Impl*>(userdata);
    if (!impl || !impl->cb)
        return;

    const std::size_t num_frames = total_amount / (2 * sizeof(s16));

    impl->cb(reinterpret_cast<s16*>(stream), num_frames);
}

std::vector<std::string> ListSDL2SinkDevices() {
    if (SDL_InitSubSystem(SDL_INIT_AUDIO) == false) {
        LOG_CRITICAL(Audio_Sink, "SDL_InitSubSystem failed with: {}", SDL_GetError());
        return {};
    }

    std::vector<std::string> device_list;
    int count = 0;
    auto devices = SDL_GetAudioPlaybackDevices(&count);
    for (int i = 0; i < count; ++i) {
        device_list.push_back(SDL_GetAudioDeviceName(devices[i]));
    }

    SDL_QuitSubSystem(SDL_INIT_AUDIO);

    return device_list;
}

} // namespace AudioCore
