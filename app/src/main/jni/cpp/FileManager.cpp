//
// Created by skysoft on 2018/4/16.
//
#include <android/log.h>
#include <jni.h>
#include "sdcardDefragmentAlg.h"
//#include "FileManager.h"
#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

#define LOG_TAG "FileManager.cpp"

#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)



typedef struct {
    JavaVM *vm;
    JNIEnv *env;
    jobject cb;  // callback object
} context_t;

static context_t sVMContext = {NULL};

 jboolean FileManager_FH_Init(JNIEnv *env, jclass object,jstring mount_path){
     ALOGE("this is jni call1-->FileManager_FH_Init");
    char *infoPath = (char *) env->GetStringUTFChars(mount_path, 0);
     env->ReleaseStringUTFChars(mount_path, infoPath);
     return (jboolean)FH_Init(infoPath);
}

 jlong FileManager_FH_Open(JNIEnv *env, jclass object,jstring mount_path, jstring file_name, jstring folder_type){
     char *mountpath = (char *) env->GetStringUTFChars(mount_path, 0);
     env->ReleaseStringUTFChars(mount_path, mountpath);
     char *filename = (char *) env->GetStringUTFChars(file_name, 0);
     env->ReleaseStringUTFChars(file_name, filename);
     char *folderType = (char *) env->GetStringUTFChars(folder_type, 0);
     env->ReleaseStringUTFChars(folder_type, folderType);

     return (jlong)FH_Open(mountpath,filename,folderType);
 }

jboolean FileManager_FH_Close(JNIEnv *env, jclass object,jlong file_pointer){
    ALOGE("this is jni call1-->FileManager_FH_Close");
    return (jboolean)FH_Close((FILE*)file_pointer);
}

jboolean  FileManager_FH_Sync(JNIEnv *env, jclass object,jlong file_pointer){
    return (jboolean)FH_Sync((FILE*)file_pointer);
}

jboolean  FileManager_FH_Delete(JNIEnv *env, jclass object,jstring mount_path,jstring absolute_filepath){
    char *mountPath = (char *) env->GetStringUTFChars(mount_path, 0);
    env->ReleaseStringUTFChars(mount_path, mountPath);
    char *absoluteFilePath = (char *) env->GetStringUTFChars(absolute_filepath, 0);
    env->ReleaseStringUTFChars(absolute_filepath, absoluteFilePath);
    return (jboolean)FH_Delete(mountPath,absoluteFilePath);
}

jstring FileManager_FH_FindOldest(JNIEnv *env, jclass object,jstring finding_path){
    char *findingPath = (char *) env->GetStringUTFChars(finding_path, 0);
    env->ReleaseStringUTFChars(finding_path, findingPath);
    string result = FH_FindOldest(findingPath);
    return env->NewStringUTF(result.c_str());
}

jboolean FileManager_FH_lock(JNIEnv *env, jclass object,jlong file_pointer){
    return (jboolean)FH_lock((FILE*)file_pointer);
}

jboolean FileManager_FH_unlock(JNIEnv *env, jclass object,jlong file_pointer){
    return (jboolean)FH_unlock((FILE*)file_pointer);
}


//************************************************************



static void FileManager_init_native(JNIEnv *env, jclass clazz) {
    sVMContext.env = env;
}

static void FileManager_native_init(JNIEnv *env, jobject thiz) {
    if (!sVMContext.cb) {
        sVMContext.cb = env->NewGlobalRef(thiz);
    }
}


static const JNINativeMethod gMethods[] = {
        { "init_native", "()V", (void *)FileManager_init_native },
        { "FH_Init", "(Ljava/lang/String;)Z", (void *)FileManager_FH_Init },
        { "FH_Open", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)J", (void *)FileManager_FH_Open },
        { "FH_Close", "(J)Z", (void *)FileManager_FH_Close },
        { "FH_Sync", "(J)Z", (void *)FileManager_FH_Sync },
        { "FH_Delete", "(Ljava/lang/String;Ljava/lang/String;)Z", (void *)FileManager_FH_Delete },
        { "FH_FindOldest", "(Ljava/lang/String;)Ljava/lang/String;", (void *)FileManager_FH_FindOldest },
        { "FH_lock", "(J)Z", (void *)FileManager_FH_lock },
        { "FH_unlock", "(J)Z", (void *)FileManager_FH_unlock },
};

static int registerNativeMethods(JNIEnv* env, const char* className,
                                 const JNINativeMethod* methods, int numMethods)
{
    jclass clazz;
    clazz = env->FindClass(className);

    if (clazz == NULL)
    {
        return JNI_FALSE;
    }

    if (env->RegisterNatives(clazz, methods, numMethods) < 0)
    {
        return JNI_FALSE;
    }

    env->DeleteLocalRef(clazz);

    return JNI_TRUE;
}


JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        goto error;
    }

    result = registerNativeMethods(env, "com/askey/dvr/cdr7010/filemanagement/controller/FileManager", gMethods, NELEM(gMethods));
    if (result < 0) {
        goto error;
    }

    sVMContext.vm = vm;
    result = JNI_VERSION_1_4;

    error:
    return result;
}






