//
// Created by skysoft on 2018/4/16.
//
#include <android/log.h>
#include <jni.h>
#include "sdcardDefragmentAlg.h"
#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

#define LOG_TAG "FileManager.cpp"

typedef struct {
    JavaVM *vm;
    JNIEnv *env;
    jobject cb;  // callback object
} context_t;

static context_t sVMContext = {NULL};

jboolean FileManager_FH_Init(JNIEnv *env, jclass object,jstring mount_path){
     ALOGE("this is jni call1-->FileManager_FH_Init");
    char *infoPath = (char *) env->GetStringUTFChars(mount_path, 0);
    jboolean result = (jboolean)FH_Init(infoPath);
     env->ReleaseStringUTFChars(mount_path, infoPath);
     return result;
}

jstring FileManager_FH_Open(JNIEnv *env, jclass object, jstring file_name, jint type){
     ALOGE("this is jni call1-->FileManager_FH_Open");
     char *filename = (char *) env->GetStringUTFChars(file_name, 0);

    ALOGE("this is jni call1-->FileManager_FH_Open filename %s",filename);
    ALOGE("this is jni call1-->FileManager_FH_Open type %d",type);
    ALOGE("this is jni call1-->FileManager_FH_Open (eFolderType)type %d",(eFolderType)type);
     char* str = FH_Open(filename,(eFolderType)type);
    if(str !=NULL ){
        ALOGE("this is jni call1-->FileManager_FH_Open_1 %s",str);
        env->ReleaseStringUTFChars(file_name, filename);
//        return env->NewString((const jchar *)str, sizeof(str));
        return env->NewStringUTF(str);
    }
    env->ReleaseStringUTFChars(file_name, filename);
    return NULL;
 }

jboolean FileManager_FH_Close(JNIEnv *env, jclass object){
    ALOGE("this is jni call1-->FileManager_FH_Close");
    return (jboolean)FH_Close();
}

jboolean  FileManager_FH_Sync(JNIEnv *env, jclass object){
    ALOGE("this is jni call1-->FileManager_FH_Sync");
    return (jboolean)FH_Sync();
}

jboolean  FileManager_FH_Delete(JNIEnv *env, jclass object,jstring absolute_filepath){
    ALOGE("this is jni call1-->FileManager_FH_Delete");
    char *absoluteFilePath = (char *) env->GetStringUTFChars(absolute_filepath, 0);
    jboolean result = (jboolean)FH_Delete(absoluteFilePath);
    env->ReleaseStringUTFChars(absolute_filepath, absoluteFilePath);
    return result;
}

jstring FileManager_FH_FindOldest(JNIEnv *env, jclass object,jint type){
    ALOGE("this is jni call1-->FileManager_FH_FindOldest");
    string result = FH_FindOldest((eFolderType)type);
    return env->NewStringUTF(result.c_str());
}

jboolean FileManager_FH_lock(JNIEnv *env, jclass object,jlong file_pointer){
    ALOGE("this is jni call1-->FileManager_FH_lock");
    return (jboolean)FH_lock((FILE*)file_pointer);
}

jboolean FileManager_FH_unlock(JNIEnv *env, jclass object,jlong file_pointer){
    ALOGE("this is jni call1-->FileManager_FH_unlock");
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
        { "FH_Open", "(Ljava/lang/String;I)Ljava/lang/String;", (void *)FileManager_FH_Open },
        { "FH_Close", "()Z", (void *)FileManager_FH_Close },
        { "FH_Sync", "()Z", (void *)FileManager_FH_Sync },
        { "FH_Delete", "(Ljava/lang/String;)Z", (void *)FileManager_FH_Delete },
        { "FH_FindOldest", "(I)Ljava/lang/String;", (void *)FileManager_FH_FindOldest },
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





