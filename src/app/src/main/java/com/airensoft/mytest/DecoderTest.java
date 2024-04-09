package com.airensoft.mytest;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCasException;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.util.Pair;

import androidx.media3.common.MimeTypes;

import com.airensoft.mytest.utils.NalUnitUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

class DecoderTest {

    boolean completed = false;
    String codecName = "";

    MediaExtractor extractor = null;
    CodecAsyncHandler codecHandler = null;
    MediaCodec codec = null;

    public String getCodecName() {
        return codecName;
    }

    public boolean Result() {
        if(!completed) {
            return false;
        }

        return true;
    }
    public boolean DecodeOnce(Context context, int resourceId) {
        completed = false;
        extractor = new MediaExtractor();
        codecHandler = new CodecAsyncHandler();
        codec = null;

        try {
            // Load a sample file of a resource
            AssetFileDescriptor fildDesc = context.getResources().openRawResourceFd(resourceId);
            extractor.setDataSource(
                    fildDesc.getFileDescriptor(), fildDesc.getStartOffset(), fildDesc.getLength());

            // Find the video track, and create a codec.
            for (int trackIdx = 0; trackIdx < extractor.getTrackCount(); trackIdx++) {
                MediaFormat format = extractor.getTrackFormat(trackIdx);

                if (format.getString(MediaFormat.KEY_MIME).contains("video/")) {
                    Log.d(getClass().getName(), "Selected Track Idx " + trackIdx);
                    Log.d(getClass().getName(), format.toString());
                    extractor.selectTrack(trackIdx);

                    codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
                    codecName = codec.getName();
                    codecHandler.setCallBack(codec, true);
                    codec.configure(format, null /* surface */, null /* crypto */, 0);
                    codec.start();
                }
            }

            if(codec == null) {
                return false;
            }

            DecoderTest.BufferSinker sink = new DecoderTest.BufferSinker(this);
            Thread sink_thread = new Thread(sink);
            sink_thread.start();

            while(true)
            {
                ////////////////////////////////////////////////////////////////////////////////////
                // Read the encoded packet by the extractor and assign it to the input buffer of
                // the decoder
                ////////////////////////////////////////////////////////////////////////////////////
                Pair<Integer, MediaCodec.BufferInfo> elem = codecHandler.getInput();
                if(elem == null)
                {
                    continue;
                }

                int bufferIndex = elem.first;
                MediaCodec.BufferInfo bufferInfo = elem.second;
                ByteBuffer inputBuffer = codec.getInputBuffer(bufferIndex);

                int readSize = extractor.readSampleData(inputBuffer, 0);
                if( readSize > 0) {
                    codec.queueInputBuffer(bufferIndex, 0, readSize, extractor.getSampleTime(), 0);
                    Log.d(getClass().getName(),String.format("readSize : %d, timestamp:%d, flags:%s, size:%s",
                            readSize, extractor.getSampleTime(), extractor.getSampleFlags(),
                            extractor.getSampleSize()));
                } else {
                    // EOS is not processed, and the remaining part is not flushed.
                    break;
                }
                extractor.advance();
            }

            codecHandler.stop();
            sink.requestStop();
            sink_thread.join();

        } catch (Exception e) {
            return false;
        }

        codec.stop();
        codec.release();
        extractor.release();
        codecHandler.resetContext();

        codec = null;
        extractor = null;
        codecHandler = null;

        return true;
    }

    class BufferSinker implements Runnable {
        private final DecoderTest parent;
        private volatile boolean stop;

        BufferSinker(DecoderTest arg) {
            parent = arg;
            stop = false;
        }

        public void requestStop() {
            stop = true;
        }

        @Override
        public void run() {
            while (!stop) {
                ////////////////////////////////////////////////////////////////////////////////////
                // Decoded frame received. and Discard received frames.
                ////////////////////////////////////////////////////////////////////////////////////
                try {
                    Pair<Integer, MediaCodec.BufferInfo> elem = parent.codecHandler.getOutput();
                    if(elem == null)
                        break;

                    int bufferIndex = elem.first;
                    MediaCodec.BufferInfo outputBuffer = elem.second;
                    parent.codec.releaseOutputBuffer(bufferIndex, false);

                    parent.completed = true;

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            Log.i(getClass().getName(), "Sink Thread Terminated");
        }
    }
}
