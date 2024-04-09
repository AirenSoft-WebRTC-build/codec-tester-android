package com.airensoft.mytest;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecInfo.EncoderCapabilities;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Pair;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.MimeTypes;

import com.airensoft.mytest.utils.SystemInfo;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MediaCodecTester";

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Define Encoding Profile Options
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private final String[] listMimeType = {
            MimeTypes.VIDEO_H264,
            MimeTypes.VIDEO_H265
    };

    private final Pair<String, Integer>[] listProfile = new Pair[] {
            new Pair(MimeTypes.VIDEO_H264, CodecProfileLevel.AVCProfileBaseline),
            new Pair(MimeTypes.VIDEO_H264, CodecProfileLevel.AVCProfileHigh),
            new Pair(MimeTypes.VIDEO_H265, CodecProfileLevel.HEVCProfileMain)
    };

    private static String getStringProfile(String mime, Integer profile) {
        if(mime.equalsIgnoreCase(MimeTypes.VIDEO_H264)) {
            switch (profile) {
                case CodecProfileLevel.AVCProfileBaseline:
                    return "Baseline";
                case CodecProfileLevel.AVCProfileMain:
                    return "Main";
                case CodecProfileLevel.AVCProfileExtended:
                    return "Extended";
                case CodecProfileLevel.AVCProfileHigh:
                    return "High";
                case CodecProfileLevel.AVCProfileHigh10:
                    return "High 10";
                case CodecProfileLevel.AVCProfileHigh422:
                    return "High 422";
                case CodecProfileLevel.AVCProfileHigh444:
                    return "High 444";
            }
        }
        else if(mime.equalsIgnoreCase(MimeTypes.VIDEO_H265)) {
            switch (profile) {
                case CodecProfileLevel.HEVCProfileMain:
                    return "Main";
                case CodecProfileLevel.HEVCProfileMain10:
                    return "Main10";
            }
        }

        return "Unknown";
    }

    private final Pair<String, Integer>[] listProfileLevel = new Pair[] {
            new Pair(MimeTypes.VIDEO_H264, CodecProfileLevel.AVCLevel31),
            new Pair(MimeTypes.VIDEO_H265, CodecProfileLevel.HEVCMainTierLevel31)
    };

    private static String getStringProfileLevel(String mime, Integer level) {
        if (mime.equalsIgnoreCase(MimeTypes.VIDEO_H264)) {
            switch (level) {
                case CodecProfileLevel.AVCLevel31:
                    return "3.1";
            }
        } else if (mime.equalsIgnoreCase(MimeTypes.VIDEO_H265)) {
            switch (level) {
                case CodecProfileLevel.HEVCMainTierLevel31:
                    return "3.1";
            }
        }

        return "Unknown";
    }

    private final String[] listResolution = {
            "1080p",
            "720p",
    };

    private static Pair<Integer, Integer> getReolustionByName(String name)
    {
        if(name.equalsIgnoreCase("1080p"))
            return new Pair(1920, 1080);
        if(name.equalsIgnoreCase("720p"))
            return new Pair(1280, 720);
        if(name.equalsIgnoreCase("540p"))
            return new Pair(960, 540);
        if(name.equalsIgnoreCase("480p"))
            return new Pair(854, 480);

        // 360p
        return new Pair(640, 360);
    }

    private final Pair<String, Integer>[] listBitrates = new Pair[] {
            new Pair("1080p", 5000000),
            new Pair("720p", 2000000),
            new Pair("540p", 1000000),
            new Pair("480p", 800000)
    };

    private final int[] listBitrateMode = {
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR,
    };

    public static String getBitrateModeToString(int bitrateMode) {
        switch (bitrateMode) {
            case MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR:
                return "CBR";
            case MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR:
                return "VBR";
            default:
                return "Unknown";
        }
    }

    private final int[] listBFrames = {
//            0,
            1,
            2,
            3,
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Define Encoding Profile Options
    ////////////////////////////////////////////////////////////////////////////////////////////////
    static class TestCaseWithResult {
        static public TestCaseWithResult Create(String mimeType, Integer profile, Integer profileLevel, String resolution, Integer bitrateMode, Integer bitrate, Integer bframes) {
            TestCaseWithResult cp = new TestCaseWithResult();
            cp.mimeType = mimeType;
            cp.profile = profile;
            cp.profileLevel = profileLevel;
            cp.resolution = resolution;
            cp.bitrateMode = bitrateMode;
            cp.bitrate = bitrate;
            cp.bframes = bframes;
            cp.profileStr = getStringProfile(cp.mimeType, (Integer) cp.profile);
            cp.profileLevelStr = getStringProfileLevel(cp.mimeType, (Integer) cp.profileLevel);
            cp.width = getReolustionByName(cp.resolution).first;
            cp.height = getReolustionByName(cp.resolution).second;
            cp.bitrateModeStr = getBitrateModeToString(cp.bitrateMode);
            cp.result = false;

            return cp;
        }
        String mimeType;

        Integer profile;
        String profileStr;

        Integer profileLevel;
        String profileLevelStr;

        Integer width;
        Integer height;
        String resolution;

        Integer bitrateMode;
        String bitrateModeStr;

        Integer bitrate;

        Integer bframes;

        boolean result;

        boolean execution;

        String codec;

        Integer resourceId;

        String toStringParams() {
            return String.format("- mime(%s), pf(%s,%s), res(%s), bits(%s/%d), bf(%d)",
                    mimeType,
                    profileStr,
                    profileLevelStr,
                    resolution,
                    bitrateModeStr,
                    bitrate,
                    bframes);
        }

        String toStringResult() {
            return String.format(" ,Codec(%s)\n > Execution(%s) , BFrames(%s)",
                    codec,
                    execution?"true":"false",
                    result?"Support":"No Support");
        }

        MediaFormat ToMediaFormat() {
            return EncoderTest.getMediaFormat(
                    mimeType,
                    profile,
                    profileLevel,
                    width,
                    height,
                    bitrateMode,
                    bitrate,
                    bframes
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SystemInfo si = new SystemInfo(this);
        PrintLog("[ System Information ]");
        PrintLog(String.format("- Model: %s", si.getModel()));
        PrintLog(String.format("- Brand: %s", si.getBrand()));
        PrintLog(String.format("- Hardware: %s", si.getHardware()));
        PrintLog(String.format("- Host: %s", si.getHost()));
        PrintLog(String.format("- Manufacturer: %s", si.getSocManufacturer()));
        PrintLog(String.format("- SOC: %s", si.getSocModel()));
        PrintLog(String.format("- ABIS: %s", si.getABIS()));
        PrintLog(String.format("- OS Version: %s", si.getOSVersion()));
        PrintLog(String.format("- SDK Version: %d", si.getSDKVersion()));
        PrintLog(String.format("- Kernel: %s", si.getKernal()));
        PrintLog(String.format("- Memory: %dMB", si.getMemory()));

        new Thread(this::TestAll).start();
//        new Thread(this::DecoderTest).start();
    }

    public void TestAll() {
        // 디코딩 & 인코딩 테스트 수행
        List<TestCaseWithResult> decode_results = DecoderTest();
        List<TestCaseWithResult> encode_results = EncoderTest();
//        List<TestCaseWithResult> decode_results = new ArrayList<>();
//        List<TestCaseWithResult> encode_results = new ArrayList<>();

        PrintLog("");
        PrintLog("[ Complete ]");

        JSONObject result = JsonUtil.ReportToJson(new SystemInfo(this), decode_results, encode_results);


        // 엘라스틱 서치에 등록
        ReportToCollector reporter = new ReportToCollector();
        PrintLog("- Preparing to send the report.");
        reporter.POST(result);
        PrintLog("- Reporting has been completed.");
        PrintLog("");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test for Decoding
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public List<TestCaseWithResult> DecoderTest() {
        PrintLog("");
        PrintLog("[UnitTest for Decoder]");
        PrintLog("- Create an decoding test case...");

        List<TestCaseWithResult> testCases = new ArrayList<>();
        {
            TestCaseWithResult cp = TestCaseWithResult.Create(MimeTypes.VIDEO_H264, CodecProfileLevel.AVCProfileBaseline, CodecProfileLevel.AVCLevel31, "1080p", EncoderCapabilities.BITRATE_MODE_CBR, 2000000, 2);
            cp.resourceId = R.raw.avc_hp31_bf2_1080p_2m;
            testCases.add(cp);
        }
        {
            TestCaseWithResult cp = TestCaseWithResult.Create(MimeTypes.VIDEO_H265, CodecProfileLevel.HEVCProfileMain, CodecProfileLevel.HEVCMainTierLevel31, "1080p", EncoderCapabilities.BITRATE_MODE_CBR, 2000000, 2);
            cp.resourceId = R.raw.hevc_main31_bf2_1080p_2m;
            testCases.add(cp);
        }

        PrintLog("- Start the decoding test.");

        int index = 0;
        for(TestCaseWithResult testCase : testCases) {
            PrintLog(" #" + index +"\n" + testCase.toStringParams());

            DecoderTest tester = new DecoderTest();
            // 수랭 여부
            testCase.execution = tester.DecodeOnce(this, testCase.resourceId);
            testCase.codec = tester.getCodecName();
            // 지원 여부
            testCase.result = tester.Result();

            PrintLog(testCase.toStringResult());

            index++;
        }

        PrintLog("- Decoding test completed.");

        return testCases;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Test for Encoding
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public List<TestCaseWithResult> EncoderTest() {
        PrintLog("");
        PrintLog("[UnitTest for Encoder]");
        PrintLog("- Create an Encoding test case...");

        List<TestCaseWithResult> testCases = new ArrayList<>();

        // 테스트 케이스 생성
        for (String aMimeType : listMimeType)
        {
            for (Pair aProfile : listProfile)
            {
                if(aProfile.first.equals(aMimeType) == false)
                    continue;

                for (Pair aProfileLevel : listProfileLevel)
                {
                    if (aProfileLevel.first.equals(aMimeType) == false)
                        continue;

                    for (String aResolution : listResolution)
                    {
                        for (int aBitrateMode : listBitrateMode)
                        {
                            for (Pair aBitrate : listBitrates)
                            {
                                if (aBitrate.first.equals(aResolution) == false)
                                    continue;

                                for (int aBFrames : listBFrames) {
                                    TestCaseWithResult cp = new TestCaseWithResult();
                                    cp.mimeType = aMimeType;
                                    cp.profile = (Integer) aProfile.second;
                                    cp.profileStr = getStringProfile(cp.mimeType, (Integer)cp.profile );
                                    cp.profileLevel = (Integer) aProfileLevel.second;
                                    cp.profileLevelStr = getStringProfileLevel(cp.mimeType, (Integer) cp.profileLevel );
                                    cp.resolution = aResolution;
                                    cp.width = getReolustionByName(cp.resolution).first;
                                    cp.height = getReolustionByName(cp.resolution).second;
                                    cp.bitrateMode = (Integer)aBitrateMode;
                                    cp.bitrateModeStr = getBitrateModeToString(cp.bitrateMode);
                                    cp.bitrate = (Integer) aBitrate.second;
                                    cp.bframes = aBFrames;
                                    cp.result = false;

                                    testCases.add(cp);
                                }
                            }
                        }
                    }
                }
            }
        }

        PrintLog("- Start the encoding test.");

        // 테스트
        int index = 0;
        for(TestCaseWithResult testCase : testCases) {
            PrintLog(" #" + index +"\n" + testCase.toStringParams());

            EncoderTest tester = new EncoderTest();

            // 결과
            testCase.execution = tester.EncodeOnce(0, testCase.ToMediaFormat(), 60);
            testCase.codec = tester.getCodecName();
            testCase.result = tester.Result();

//            PrintLog(testCase.ToMediaFormat().toString());
            PrintLog(testCase.toStringResult());
            index++;
        }

        PrintLog("- Encoding test completed.");

        return testCases;
    }

    public synchronized void PrintLog(String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.textView);
                if(tv != null) {
                    tv.append(message);
                    tv.append("\n");
                }

                ScrollView sv = (ScrollView) findViewById(R.id.scrollView);
                sv.fullScroll(TextView.FOCUS_DOWN);
            }
        });
    }

}