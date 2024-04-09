package com.airensoft.mytest;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.util.Pair;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CodecAsyncHandler extends MediaCodec.Callback {
    private static final String TAG = CodecAsyncHandler.class.getSimpleName();
    private final Lock mLock = new ReentrantLock();
    private final Condition mCondition = mLock.newCondition();
    private final LinkedList<Pair<Integer, MediaCodec.BufferInfo>> mCbInputQueue;
    private final LinkedList<Pair<Integer, MediaCodec.BufferInfo>> mCbOutputQueue;
    private volatile boolean mSignalledError;

    private volatile boolean mStop;

    CodecAsyncHandler() {
        mCbInputQueue = new LinkedList<>();
        mCbOutputQueue = new LinkedList<>();
        mSignalledError = false;
        mStop = false;
    }

    void clearQueues() {
        mLock.lock();
        mCbInputQueue.clear();
        mCbOutputQueue.clear();
        mLock.unlock();
    }

    void resetContext() {
        clearQueues();
        mSignalledError = false;
    }

    void stop() {
        mLock.lock();
        mStop = true;
        mCondition.signalAll();
        mLock.unlock();
    }

    @Override
    public void onInputBufferAvailable(MediaCodec codec, int bufferIndex) {
//        Log.i(TAG, "onInputBufferAvailable[" + bufferIndex + "]");
        mLock.lock();
        mCbInputQueue.add(new Pair<>(bufferIndex, (MediaCodec.BufferInfo) null));
        mCondition.signalAll();
        mLock.unlock();
    }

    @Override
    public void onOutputBufferAvailable(MediaCodec codec, int bufferIndex, MediaCodec.BufferInfo info) {
//        Log.i(TAG, "onOutputBufferAvailable[" + bufferIndex + "]");
        mLock.lock();
        mCbOutputQueue.add(new Pair<>(bufferIndex, info));
        mCondition.signalAll();
        mLock.unlock();
    }

    @Override
    public void onError(MediaCodec codec, MediaCodec.CodecException e) {
        mLock.lock();
        mSignalledError = true;
        mCondition.signalAll();
        mLock.unlock();
        Log.e(TAG, "Received media codec error : " + e.getMessage());
    }

    @Override
    public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
        Log.i(TAG, "Output format changed: " + format.toString());
    }

    void setCallBack(MediaCodec codec, boolean isCodecInAsyncMode) {
        if (isCodecInAsyncMode) {
            codec.setCallback(this);
        }
    }

    public int getOutputSize() {
        mLock.lock();
        int size = mCbOutputQueue.size();
        mLock.unlock();
        return size;
    }

    public int getInputSize() {
        mLock.lock();
        int size = mCbInputQueue.size();
        mLock.unlock();
        return size;
    }

    Pair<Integer, MediaCodec.BufferInfo> getOutput() throws InterruptedException {
        Pair<Integer, MediaCodec.BufferInfo> element = null;
        mLock.lock();
        while (!hasSeenError() && !mStop) {
            if (mCbOutputQueue.isEmpty()) {
                mCondition.await();
            } else {
                element = mCbOutputQueue.remove(0);
                break;
            }
        }
        mLock.unlock();
        return element;
    }

    Pair<Integer, MediaCodec.BufferInfo> getInput() throws InterruptedException {
        Pair<Integer, MediaCodec.BufferInfo> element = null;
        mLock.lock();
        while (!hasSeenError() && !mStop) {
            if (mCbInputQueue.isEmpty()) {
                mCondition.await();
            } else {
                element = mCbInputQueue.remove(0);
                break;
            }
        }
        mLock.unlock();
        return element;
    }

    boolean hasSeenError() {
        return mSignalledError;
    }
}
