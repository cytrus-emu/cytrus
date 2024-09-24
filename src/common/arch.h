// Copyright 2023 Citra Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

#pragma once

#include <boost/predef.h>

#define CYTRUS_ARCH(NAME) (CYTRUS_ARCH_##NAME)

#define CYTRUS_ARCH_x86_64 BOOST_ARCH_X86_64
#define CYTRUS_ARCH_arm64                                                                          \
    (BOOST_ARCH_ARM >= BOOST_VERSION_NUMBER(8, 0, 0) && BOOST_ARCH_WORD_BITS == 64)
