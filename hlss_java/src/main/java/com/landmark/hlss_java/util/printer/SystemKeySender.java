package com.landmark.hlss_java.util.printer;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

public class SystemKeySender {
    // 按鍵釋放的底層數值（Windows API 標準值，固定不變）
    private static final int KEY_UP_FLAG = 0x0002;

    // 模擬按鍵按下
    public static void keyDown(int keyCode) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wVk = new WinDef.WORD(keyCode);
        input.input.ki.dwFlags = new WinDef.DWORD(0); // 0 表示按下
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }

    // 模擬按鍵釋放（使用固定數值，兼容所有 JNA 版本）
    public static void keyUp(int keyCode) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType("ki");
        input.input.ki.wVk = new WinDef.WORD(keyCode);
        input.input.ki.dwFlags = new WinDef.DWORD(KEY_UP_FLAG); // 直接用數值 0x0002
        User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
    }

    // 模擬組合鍵（Ctrl+P 等）
    public static void sendCombination(int key1, int key2) {
        keyDown(key1);
        keyDown(key2);
        keyUp(key2);
        keyUp(key1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}