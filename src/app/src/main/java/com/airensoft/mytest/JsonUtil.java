package com.airensoft.mytest;

import android.media.MediaCodecInfo;
import android.os.Build;

import androidx.media3.common.MimeTypes;

import com.airensoft.mytest.utils.SystemInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class JsonUtil {
    static public JSONObject SystemInfoToJson(SystemInfo info)
    {
        JSONObject obj = new JSONObject();

        try {
            obj.put("model", info.getModel());
            obj.put("brand", info.getBrand());
            obj.put("hardware", info.getHardware());
            obj.put("host", info.getHost());
            obj.put("soc_manufacturer", info.getSocManufacturer());
            obj.put("soc_model", info.getSocModel());
            obj.put("abis", info.getABIS());
            obj.put("os_version", info.getOSVersion());
            obj.put("sdk_version", info.getSDKVersion());
            obj.put("kernel", info.getKernal());
            obj.put("memory", info.getMemory());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return obj;
    }

    static public JSONObject TestCaseToJson(MainActivity.TestCaseWithResult info)
    {
        JSONObject obj = new JSONObject();

        try {
            obj.put("mime", info.mimeType);
            obj.put("codec", info.codec);
            obj.put("profile", info.profileStr);
            obj.put("level", info.profileLevelStr);
            obj.put("size", info.resolution);
            obj.put("bitrate_mode", info.bitrateModeStr);
            obj.put("bitrate", info.bitrate);
            obj.put("bframes", info.bframes);
            obj.put("execution", info.execution);
            obj.put("result", info.result);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return obj;
    }

    static public JSONObject ReportToJson(SystemInfo info, List<MainActivity.TestCaseWithResult> decode_results, List<MainActivity.TestCaseWithResult> encode_results)
    {
        JSONObject root = SystemInfoToJson(info);

        boolean avc_dec_bframes_support = false;
        boolean hevc_dec_bframes_support= false;
        boolean avc_enc_vbr_bfraems_support= false;
        boolean avc_enc_cbr_bfraems_support= false;
        boolean hevc_enc_vbr_bframes_support= false;
        boolean hevc_enc_cbr_bframes_support= false;
        String avc_dec_name = "-";
        String hevc_dec_name = "-";
        String avc_enc_name = "-";
        String hevc_enc_name = "-";

        JSONArray arrDecodeResults = new JSONArray();
        for (MainActivity.TestCaseWithResult r : decode_results) {
            JSONObject obj = TestCaseToJson(r);
            arrDecodeResults.put(obj);

            // 테스트 결과 중 1개라도 지원된다면 지원으로 판단
            if(r.mimeType.equals(MimeTypes.VIDEO_H264) && r.result)
            {
                avc_dec_bframes_support = true;
            }
            if(r.mimeType.equals(MimeTypes.VIDEO_H265) && r.result)
            {
                hevc_dec_bframes_support = true;
            }
            if(r.mimeType.equals(MimeTypes.VIDEO_H264))
            {
                avc_dec_name = r.codec;
            }
            if(r.mimeType.equals(MimeTypes.VIDEO_H265))
            {
                hevc_dec_name = r.codec;
            }
        }

        JSONArray arrEncodeResults = new JSONArray();
        for (MainActivity.TestCaseWithResult r : encode_results) {
            JSONObject obj = TestCaseToJson(r);
            arrEncodeResults.put(obj);

            if(r.mimeType.equals(MimeTypes.VIDEO_H264) && r.result)
            {
                if(r.bitrateMode == MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR) {
                    avc_enc_cbr_bfraems_support = true;
                }
                if(r.bitrateMode == MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR) {
                    avc_enc_vbr_bfraems_support = true;
                }
            }
            if(r.mimeType.equals(MimeTypes.VIDEO_H265) && r.result)
            {
                if(r.bitrateMode == MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR) {
                    hevc_enc_cbr_bframes_support = true;
                }
                if(r.bitrateMode == MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR) {
                    hevc_enc_vbr_bframes_support = true;
                }
            }
            if(r.mimeType.equals(MimeTypes.VIDEO_H264))
            {
                avc_enc_name = r.codec;
            }
            if(r.mimeType.equals(MimeTypes.VIDEO_H265))
            {
                hevc_enc_name = r.codec;
            }
        }

        try {
            root.put("decodeResults", arrDecodeResults);
            root.put("encodeResults", arrEncodeResults);
            root.put("avc_dec_support", avc_dec_bframes_support);
            root.put("hevc_dec_support", hevc_dec_bframes_support);
            root.put("avc_enc_vbr_support", avc_enc_vbr_bfraems_support);
            root.put("avc_enc_cbr_support", avc_enc_cbr_bfraems_support);
            root.put("hevc_enc_vbr_support", hevc_enc_vbr_bframes_support);
            root.put("hevc_enc_cbr_support", hevc_enc_cbr_bframes_support);

            root.put("avc_dec", avc_dec_name);
            root.put("hevc_dec", hevc_dec_name);
            root.put("avc_enc", avc_enc_name);
            root.put("hevc_enc", hevc_enc_name);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return root;
    }



}
