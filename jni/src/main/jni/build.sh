#!/usr/bin/env sh

# lib
OUTPUT_LIB_NAME='libjni.so'
TARGET_LIB_NAME='liblettre-jni.so'

# arm64-v8a
echo "Building for arm64-v8a"
cargo build --profile release --target aarch64-linux-android
echo "Copying to libs/arm64-v8a"
mkdir -p ../libs/arm64-v8a
cp target/aarch64-linux-android/release/$OUTPUT_LIB_NAME ../libs/arm64-v8a/$TARGET_LIB_NAME

# x86_64
echo "Building for x86_64"
cargo build --profile release --target x86_64-linux-android
echo "Copying to libs/x86_64"
mkdir -p ../libs/x86_64
cp target/x86_64-linux-android/release/$OUTPUT_LIB_NAME ../libs/x86_64/$TARGET_LIB_NAME