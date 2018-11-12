#include <jni.h>
#include <string>

#include "com_example_channy_jnitest_MainActivity.h"

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_channy_jnitest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C"
JNIEXPORT jint
JNICALL
Java_com_example_channy_jnitest_MainActivity_integerFromJNI(JNIEnv *env, jobject, jint a, jint b) {
    return a + b;
}