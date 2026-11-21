#!/usr/bin/env bash
# Uji NeonAmp di emulator Android: buka app, putar video online, putar audio, unduh video.
set -u
PKG=com.permata.neonamp
APK=${1:-NeonAmp.apk}
mkdir -p shots
shot() { adb exec-out screencap -p > "shots/$1.png" || true; }
test_intent() { adb shell am start -n $PKG/.MainActivity -a $PKG.TEST "$@" >/dev/null; }

adb install -r "$APK"
adb shell pm grant $PKG android.permission.POST_NOTIFICATIONS 2>/dev/null || true
adb logcat -c
adb shell am start -n $PKG/.MainActivity
sleep 10; shot 01-home

echo "=== TEST 1: video online (YouTube, merged video+audio stream) ==="
test_intent --es url "https://www.youtube.com/watch?v=dQw4w9WgXcQ" --ez video true
sleep 30; shot 02-video-online
sleep 15; shot 02b-video-subtitle

echo "=== TEST 2: audio online ==="
test_intent --es url "https://www.youtube.com/watch?v=kJQP7kiw5Fk" --ez video false
sleep 30; shot 03-audio-online

echo "=== TEST 3: download video offline ==="
test_intent --es url "https://www.youtube.com/watch?v=dQw4w9WgXcQ" --es download video
sleep 75; shot 04-after-download
adb shell run-as $PKG ls -la files/video 2>/dev/null || adb shell ls -la /data/data/$PKG/files/video 2>/dev/null || true

adb logcat -d > logcat.txt
echo "=== NeonAmp log ==="
grep -E "NeonAmpTest" logcat.txt | tail -80
echo "=== ExoPlayer errors ==="
grep -E "ExoPlayerImplInternal|Playback error|MediaCodecRenderer" logcat.txt | grep -iE "error|exception" | head -30
echo "=== Crashes ==="
grep -A25 "FATAL EXCEPTION" logcat.txt | head -60 || true
echo "=== SUMMARY ==="
grep -q "video=[1-9][0-9]*x[1-9]" logcat.txt && echo "PASS video online: frames decoded" || echo "FAIL video online"
grep -qE "cap=[^-]" logcat.txt && echo "PASS subtitles shown on video" || echo "FAIL subtitles"
grep -q "download ok VIDEO" logcat.txt && echo "PASS video download" || echo "FAIL video download"
grep -q "FATAL EXCEPTION" logcat.txt && echo "FAIL crash detected" || echo "PASS no crash"
exit 0
