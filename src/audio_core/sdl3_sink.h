// Copyright 2016 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#pragma once

#include <cstddef>
#include <memory>
#include "audio_core/sink.h"

namespace AudioCore {

class SDL3Sink final : public Sink {
public:
    explicit SDL3Sink(std::string device_id);
    ~SDL3Sink() override;

    unsigned int GetNativeSampleRate() const override;

    void SetCallback(std::function<void(s16*, std::size_t)> cb) override;

private:
    struct Impl;
    std::unique_ptr<Impl> impl;
};

std::vector<std::string> ListSDL3SinkDevices();

} // namespace AudioCore
