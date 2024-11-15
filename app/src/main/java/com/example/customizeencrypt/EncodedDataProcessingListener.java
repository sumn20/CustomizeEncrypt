package com.example.customizeencrypt;

/**
 * @Author Harry
 * @ClassName EncodedDataProcessingListener
 * @date 2024/8/14 11:57
 * @Description TODO
 * @Version 1.0
 */
public interface EncodedDataProcessingListener {
    byte[] didEncodeVideo(byte[] videoData, int streamType);
    byte[] willDecodeVideo(String userId, byte[] videoData, int streamType);
    byte[] didEncodeAudio(byte[] audioData, int streamType);
    byte[] willDecodeAudio(String userId, byte[] audioData, int streamType);
}
