package com.example.customizeencrypt;

import android.util.Log;

import java.nio.charset.StandardCharsets;

/**
 * @Author Harry
 * @ClassName EncodedDataProcessingListenerImpl
 * @date 2024/8/14 11:57
 * @Description TODO
 * @Version 1.0
 */
public class EncodedDataProcessingListenerImpl implements EncodedDataProcessingListener {
    private String mEncryptKey;

    public EncodedDataProcessingListenerImpl(String encryptKey) {
        Log.d("TEST", "EncodedDataProcessingListenerImpl:" + encryptKey);
        mEncryptKey = encryptKey;
    }

    //视频编码加密
    @Override
    public byte[] didEncodeVideo(byte[] originData, int streamType) {
      //  Log.d("TEST", "java didEncodeVideo size:" + originData.length);
        // 处理 videoData
        return xorData(originData, mEncryptKey.getBytes(StandardCharsets.UTF_8));
    }

    //视频解码解密
    @Override
    public byte[] willDecodeVideo(String userId, byte[] originData, int streamType) {
        // 处理 videoData
        return xorData(originData, mEncryptKey.getBytes(StandardCharsets.UTF_8));
    }

    //音频编码加密
    @Override
    public byte[] didEncodeAudio(byte[] originData, int streamType) {
        // 处理 audioData
        return xorData(originData, mEncryptKey.getBytes(StandardCharsets.UTF_8));
    }

    //音频解码解密
    @Override
    public byte[] willDecodeAudio(String userId, byte[] originData, int streamType) {
        // 处理 audioData
        return xorData(originData, mEncryptKey.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] xorData(byte[] srcData, byte[] key) {
        int keySize = key.length;
        int dataSize = srcData.length;
        byte[] dstData = new byte[dataSize];
        for (int i = 0; i < dataSize; ++i) {
            dstData[i] = (byte) (srcData[i] ^ key[i % keySize]);
        }
        return dstData;
    }

}