package com.airensoft.mytest;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.util.Pair;

import androidx.media3.common.MimeTypes;

import com.airensoft.mytest.utils.NalUnitUtil;

import java.nio.ByteBuffer;

public class EncoderTest {

    static public MediaFormat getMediaFormat(String mimeType, int profile, int profileLevel,
                                             int width, int height,
                                             int bitrateMode, int bitrate, int bframes) {
        MediaFormat format = new MediaFormat();
        format.setString(MediaFormat.KEY_MIME, mimeType);
        format.setInteger(MediaFormat.KEY_PROFILE, profile);
        format.setInteger(MediaFormat.KEY_LEVEL, profileLevel);
        format.setInteger(MediaFormat.KEY_WIDTH, width);
        format.setInteger(MediaFormat.KEY_HEIGHT, height);
        format.setInteger(MediaFormat.KEY_BITRATE_MODE, bitrateMode);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        format.setInteger(MediaFormat.KEY_MAX_B_FRAMES, bframes);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setFloat(MediaFormat.KEY_FRAME_RATE, 30.0f);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        return format;
    }

    boolean completed;

    String mimeType;
    int maxBFframes;
    float frameRate;

    // 결과
    boolean hasBFrames;

    String codecName;

    public String getCodecName() {
        return codecName;
    }

    public boolean Result() {
        if (!completed) {
            return false;
        }

        if (maxBFframes > 0) {
            return hasBFrames;
        }

        // hasBframe == 0
        if (hasBFrames)
            return false;

        return true;
    }

    MediaCodec codec = null;
    CodecAsyncHandler codecHandler = null;
    CodecInputSurface inputSurface = null;

    public boolean EncodeOnce(int id, MediaFormat format, int maxEncodeFrames) {

        completed = false;
        hasBFrames = false;

        Log.d(getClass().getName(), "[" + id + "] EncodeOnce. format: " + format.toString());


        try {
            mimeType = format.getString(MediaFormat.KEY_MIME);
            maxBFframes = format.getInteger(MediaFormat.KEY_MAX_B_FRAMES);
            frameRate = format.getFloat(MediaFormat.KEY_FRAME_RATE);

            codec = MediaCodec.createEncoderByType(mimeType);
            codecName = codec.getName();
            codecHandler = new CodecAsyncHandler();
            codecHandler.setCallBack(codec, true);
            codec.configure(format, null /* surface */, null /* crypto */, MediaCodec.CONFIGURE_FLAG_ENCODE);

            inputSurface = new CodecInputSurface(codec.createInputSurface());
            inputSurface.makeCurrent();

            codec.start();

            BufferSinker sink = new BufferSinker(this);
            Thread sink_thread = new Thread(sink);
            sink_thread.start();

            for (int frame_num = 0; frame_num < maxEncodeFrames; frame_num++) {
                // Draw the Random image to the Input Surface
                inputSurface.drawRandomeImageToSurface(frame_num, format.getInteger(MediaFormat.KEY_WIDTH), format.getInteger(MediaFormat.KEY_HEIGHT));
                inputSurface.setPresentationTime(frame_num * (long) (1000000000 / format.getFloat(MediaFormat.KEY_FRAME_RATE)));
                inputSurface.swapBuffers();

                // 1배속 인코딩 속도 유지
                Thread.sleep((long) (1000.0f / frameRate / 2));
            }

            codecHandler.stop();
            sink.requestStop();
            sink_thread.join();
        } catch (Exception e) {
            Log.d(getClass().getName(), e.toString());
            return false;
        }

        codec.stop();
        codec.release();

        codec = null;
        codecHandler = null;
        inputSurface = null;
        completed = true;
        return true;
    }

    class BufferSinker implements Runnable {
        private final EncoderTest parent;
        private volatile boolean stop;

        BufferSinker(EncoderTest arg) {
            parent = arg;
            stop = false;
        }

        public void requestStop() {
            stop = true;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    Pair<Integer, MediaCodec.BufferInfo> elem = parent.codecHandler.getOutput();
                    if (elem == null) {
                        continue;
                    }

                    int bufferIndex = elem.first;
                    MediaCodec.BufferInfo info = elem.second;

                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                    if (info.size > 0) {
                        ByteBuffer buf = codec.getOutputBuffer(bufferIndex);
                        if (buf != null) {
                            int frameType = 0;
                            if (mimeType.equalsIgnoreCase(MimeTypes.VIDEO_H264))
                                frameType = NalUnitUtil.getStandardizedFrameTypesFromAVC(buf);
                            else if (mimeType.equalsIgnoreCase(MimeTypes.VIDEO_H265))
                                frameType = NalUnitUtil.getStandardizedFrameTypesFromHEVC(buf);

                            Log.d(getClass().getName(), "Encoded frame." +
                                    " mm: " + codec.getOutputFormat().getString(MediaFormat.KEY_MIME) +
                                    " id: " + bufferIndex +
                                    " fg: " + info.flags +
                                    " sz: " + info.size +
                                    " ts: " + info.presentationTimeUs +
                                    " ft:" + NalUnitUtil.frameTypeToString(frameType));

                            // 만약, 하나라도 BFRAME이 만들어졌다면 BFRAME을 지원한다고 판단한다
                            if (frameType == NalUnitUtil.FRAME_TYPE_B) {
                                hasBFrames = true;
                            }
                        }
                        codec.releaseOutputBuffer(bufferIndex, false);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            Log.i(getClass().getName(), "Sink Thread Terminated");
        }
    }
}
