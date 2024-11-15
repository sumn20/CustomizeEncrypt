#include <android/log.h>
#include <jni.h>
#include <string>

#include "TXLiteAVEncodedDataProcessingListener.h"


#define  LOG(...)  __android_log_print(ANDROID_LOG_DEBUG,"TEST",__VA_ARGS__)
using namespace liteav;

class JniEncodedDataProcessingListener : public ITXLiteAVEncodedDataProcessingListener {
public:
    JniEncodedDataProcessingListener(JNIEnv *env, jobject jListener) {
        m_jvm = nullptr;
        m_jListener = nullptr;

        env->GetJavaVM(&m_jvm);
        m_jListener = env->NewGlobalRef(jListener);

        jclass jListenerClass = env->GetObjectClass(jListener);
        m_jDidEncodeVideoMethod = env->GetMethodID(jListenerClass, "didEncodeVideo", "([BI)[B");
        m_jWillDecodeVideoMethod = env->GetMethodID(jListenerClass, "willDecodeVideo",
                                                    "(Ljava/lang/String;[BI)[B");
        m_jDidEncodeAudioMethod = env->GetMethodID(jListenerClass, "didEncodeAudio", "([BI)[B");
        m_jWillDecodeAudioMethod = env->GetMethodID(jListenerClass, "willDecodeAudio",
                                                    "(Ljava/lang/String;[BI)[B");
    }

    virtual ~JniEncodedDataProcessingListener() {
        if (m_jvm) {
            JNIEnv *env;
            m_jvm->AttachCurrentThread(&env, nullptr);
            env->DeleteGlobalRef(m_jListener);
            m_jvm->DetachCurrentThread();
        }
    }

    bool didEncodeVideo(TXLiteAVEncodedData &videoData) override {
        if (!videoData.processedData) {
            return false;
        }
        JNIEnv *env;
        m_jvm->AttachCurrentThread(&env, nullptr);

        jbyteArray jVideoData = env->NewByteArray(videoData.originData->size());
        env->SetByteArrayRegion(jVideoData, 0, videoData.originData->size(),
                                (jbyte *) videoData.originData->cdata());

        jbyteArray jProcessedVideoData = (jbyteArray) env->CallObjectMethod(m_jListener,
                                                                            m_jDidEncodeVideoMethod,
                                                                            jVideoData);

        jsize processedDataLength = env->GetArrayLength(jProcessedVideoData);
        jbyte *processedDataBytes = env->GetByteArrayElements(jProcessedVideoData, nullptr);
       
        videoData.processedData->EnsureCapacity(processedDataLength);
        memcpy(videoData.processedData->data(), processedDataBytes, processedDataLength);
        
        videoData.processedData->SetSize(processedDataLength);

        env->ReleaseByteArrayElements(jProcessedVideoData, processedDataBytes, 0);
        m_jvm->DetachCurrentThread();

        return true;
    }

    bool willDecodeVideo(TXLiteAVEncodedData &videoData) override {
        if (!videoData.processedData) {
            return false;
        }
        JNIEnv *env;
        m_jvm->AttachCurrentThread(&env, nullptr);

        jbyteArray jVideoData = env->NewByteArray(videoData.originData->size());
        env->SetByteArrayRegion(jVideoData, 0, videoData.originData->size(),
                                (jbyte *) videoData.originData->cdata());

        jstring jUserId = videoData.userId ? env->NewStringUTF(videoData.userId) : nullptr;

        jbyteArray jProcessedVideoData = (jbyteArray) env->CallObjectMethod(m_jListener,
                                                                            m_jWillDecodeVideoMethod,
                                                                            jUserId, jVideoData);

        jsize processedDataLength = env->GetArrayLength(jProcessedVideoData);
        jbyte *processedDataBytes = env->GetByteArrayElements(jProcessedVideoData, nullptr);

       
        videoData.processedData->EnsureCapacity(processedDataLength);
        memcpy(videoData.processedData->data(), processedDataBytes, processedDataLength);
        
        videoData.processedData->SetSize(processedDataLength);

        env->ReleaseByteArrayElements(jProcessedVideoData, processedDataBytes, 0);
        m_jvm->DetachCurrentThread();

        return true;
    }

    bool didEncodeAudio(TXLiteAVEncodedData &audioData) override {
        if (!audioData.processedData) {
            return false;
        }
        JNIEnv *env;
        m_jvm->AttachCurrentThread(&env, nullptr);

        jbyteArray jAudioData = env->NewByteArray(audioData.originData->size());
        env->SetByteArrayRegion(jAudioData, 0, audioData.originData->size(),
                                (jbyte *) audioData.originData->cdata());

        jbyteArray jProcessedAudioData = (jbyteArray) env->CallObjectMethod(m_jListener,
                                                                            m_jDidEncodeAudioMethod,
                                                                            jAudioData);

        jsize processedDataLength = env->GetArrayLength(jProcessedAudioData);
        jbyte *processedDataBytes = env->GetByteArrayElements(jProcessedAudioData, nullptr);

       
        audioData.processedData->EnsureCapacity(processedDataLength);
        memcpy(audioData.processedData->data(), processedDataBytes, processedDataLength);
        
        audioData.processedData->SetSize(processedDataLength);


        env->ReleaseByteArrayElements(jProcessedAudioData, processedDataBytes, 0);
        m_jvm->DetachCurrentThread();

        return true;
    }

    bool willDecodeAudio(TXLiteAVEncodedData &audioData) override {
        if (!audioData.processedData) {
            return false;
        }
        JNIEnv *env;
        m_jvm->AttachCurrentThread(&env, nullptr);

        jbyteArray jAudioData = env->NewByteArray(audioData.originData->size());
        env->SetByteArrayRegion(jAudioData, 0, audioData.originData->size(),
                                (jbyte *) audioData.originData->cdata());

        jstring jUserId = audioData.userId ? env->NewStringUTF(audioData.userId) : nullptr;

        jbyteArray jProcessedAudioData = (jbyteArray) env->CallObjectMethod(m_jListener,
                                                                            m_jWillDecodeAudioMethod,
                                                                            jUserId, jAudioData);

        jsize processedDataLength = env->GetArrayLength(jProcessedAudioData);
        jbyte *processedDataBytes = env->GetByteArrayElements(jProcessedAudioData, nullptr);

       
        audioData.processedData->EnsureCapacity(processedDataLength);
        memcpy(audioData.processedData->data(), processedDataBytes, processedDataLength);
        
        audioData.processedData->SetSize(processedDataLength);


        env->ReleaseByteArrayElements(jProcessedAudioData, processedDataBytes, 0);
        m_jvm->DetachCurrentThread();

        return true;
    }

private:
    JavaVM *m_jvm;
    jobject m_jListener;
    jmethodID m_jDidEncodeVideoMethod;
    jmethodID m_jWillDecodeVideoMethod;
    jmethodID m_jDidEncodeAudioMethod;
    jmethodID m_jWillDecodeAudioMethod;
};


extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_customizeencrypt_TRTCCustomerCrypt_nativeGetEncodedDataProcessingListener(
        JNIEnv *env, jobject thiz, jobject encoded_data_processing_listener) {
    JniEncodedDataProcessingListener *jniListener = new JniEncodedDataProcessingListener(env,
                                                                                         encoded_data_processing_listener);
    return (jlong) jniListener;
}