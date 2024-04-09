package com.airensoft.mytest.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class SystemInfo {
    private Context context;

    public SystemInfo(Context c)
    {
        context = c;
    }

    public String getModel() {
        return android.os.Build.MODEL;
    }

    public String getBrand() {
        return Build.BRAND;
    }

    public String getHardware()  {
        return Build.HARDWARE;
    }

    public String getHost()  {
        return Build.HOST;
    }

    public String getSocManufacturer()  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return Build.SOC_MANUFACTURER;
        }
        else
        {
            return Build.MANUFACTURER;
        }
    }

    public String getSocModel()  {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return Build.SOC_MODEL;
        }
        return getCPUInfo();
    }

    private String getCPUInfo() {
        try {
            FileReader fileReader = new FileReader("/proc/cpuinfo");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Hardware")) {
                    // "Hardware" 항목에서 SOC 모델명 추출
                    String[] parts = line.split(":\\s+", 2);
                    if (parts.length >= 2) {
                        return parts[1];
                    }
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            // Nothing
        }
        return "Unknown"; // SOC 모델명을 찾지 못한 경우
    }

    public String getABIS()  {
        return  Arrays.toString(Build.SUPPORTED_ABIS);
    }

    public String getOSVersion()  {
        return  Build.VERSION.RELEASE;
    }

    public int getSDKVersion()  {
        return  Build.VERSION.SDK_INT;
    }

    public String getKernal()  {
        return System.getProperty("os.version");
    }

    public long getMemory() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.totalMem / 1000000;
    }

}
