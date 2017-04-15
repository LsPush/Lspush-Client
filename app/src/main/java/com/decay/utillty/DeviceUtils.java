/*
 * Copyright 2017 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.decay.utillty;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.UUID;

public class DeviceUtils {
    private DeviceUtils() {}

    public static boolean isSimulator() {
        return isVBox() || Build.FINGERPRINT.contains("generic");
    }

    public static boolean isVBox() {
        Log.i("device", Build.FINGERPRINT);
        return Build.FINGERPRINT.contains("vbox");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static long getTotalMemory(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem;
    }

    public static boolean isLowRamDevice(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 属于小内存设备
            return activityManager.isLowRamDevice();
        }
        return false;
        //else { // 当前可用内存较少
        //    ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //    activityManager.getMemoryInfo(memoryInfo);
        //    return memoryInfo.lowMemory;
        //}
    }

    /**
     * <a href="http://blog.csdn.net/nugongahou110/article/details/47003257">不需要任何权限获得Android设备的唯一ID</a>
     */
    @SuppressWarnings("deprecation")
    public static String getUniquePseudoID() {
        String serial;

        String deviceSerialId = "35" +
            Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
            Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
            Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
            Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
            Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
            Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
            Build.USER.length() % 10; //13 位

        try {
            serial = Build.class.getField("SERIAL").get(null).toString();
            // API >= 9 使用 serial 号
            return new UUID(deviceSerialId.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial 需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(deviceSerialId.hashCode(), serial.hashCode()).toString();
    }
}
