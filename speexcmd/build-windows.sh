#!/bin/sh
i686-w64-mingw32-gcc -o speexcmd.exe cmd-speexdsp.c -lspeexdsp -I ../../speexdsp/include -L ../../speexdsp/libspeexdsp/.libs
