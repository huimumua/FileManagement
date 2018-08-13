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

jboolean FileManager_FH_ValidFormat(JNIEnv *env, jclass object,jstring mount_path){
    char *infoPath = (char *) env->GetStringUTFChars(mount_path, 0);
    jboolean result = (jboolean)FH_ValidFormat(infoPath);
    env->ReleaseStringUTFChars(mount_path, infoPath);
    return result;
}

jint FileManager_FH_Init(JNIEnv *env, jclass object,jstring mount_path){
    char *infoPath = (char *) env->GetStringUTFChars(mount_path, 0);
     return FH_Init(infoPath);
}

jstring FileManager_FH_Open(JNIEnv *env, jclass object, jstring file_name, jint type){
    char *filename = (char *) env->GetStringUTFChars(file_name, 0);
    string result = FH_Open(filename,(eFolderType)type);
    return env->NewStringUTF(result.c_str());
//     char* str = FH_Open(filename,(eFolderType)type);
//    if(str !=NULL ){
//        env->ReleaseStringUTFChars(file_name, filename);
//        return env->NewStringUTF(str);
//    }
//    env->ReleaseStringUTFChars(file_name, filename);
//    return NULL;
 }

jboolean FileManager_FH_Close(JNIEnv *env, jclass object){
    return (jboolean)FH_Close();
}

void  FileManager_FH_Sync(JNIEnv *env, jclass object){
    return FH_Sync();
}

jboolean  FileManager_FH_Delete(JNIEnv *env, jclass object,jstring absolute_filepath,jint cameraType){
    char *absoluteFilePath = (char *) env->GetStringUTFChars(absolute_filepath, 0);
    jboolean result = (jboolean)FH_Delete(absoluteFilePath,(eCameraType)cameraType);
    env->ReleaseStringUTFChars(absolute_filepath, absoluteFilePath);
    return result;
}

jstring FileManager_FH_FindOldest(JNIEnv *env, jclass object,jint type,jint cameraType){
    string result = FH_FindOldest((eFolderType)type,(eCameraType)cameraType);
    return env->NewStringUTF(result.c_str());
}

jint FileManager_FH_CheckFolderStatus(JNIEnv *env, jclass object,jint type){
    jint  numb = FH_CheckFolderStatus((eFolderType)type);
    ALOGE("this is jni call1-->FileManager_FH_CheckFolderStatus= %d" , numb);
//    return FH_FolderCanUseFilenumber((eFolderType)type);
    return numb;
}

jint FileManager_FH_GetSDCardInfo(JNIEnv *env, jclass object,jint type,jint numType){
    jint  numb = FH_GetSDCardInfo((eFolderType)type,(eGetNum)numType);
    ALOGE("this is jni call1-->FileManager_FH_GetSDCardInfo= %d" , numb);
//    return FH_FolderCanUseFilenumber((eFolderType)type);
    return numb;
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
        { "FH_ValidFormat", "(Ljava/lang/String;)Z", (void *)FileManager_FH_ValidFormat },
        { "FH_Init", "(Ljava/lang/String;)I", (void *)FileManager_FH_Init },
        { "FH_Open", "(Ljava/lang/String;I)Ljava/lang/String;", (void *)FileManager_FH_Open },
        { "FH_Close", "()Z", (void *)FileManager_FH_Close },
        { "FH_Sync", "()V", (void *)FileManager_FH_Sync },
        { "FH_Delete", "(Ljava/lang/String;I)Z", (void *)FileManager_FH_Delete },
        { "FH_FindOldest", "(II)Ljava/lang/String;", (void *)FileManager_FH_FindOldest },
        { "FH_CheckFolderStatus", "(I)I", (void *)FileManager_FH_CheckFolderStatus },
        { "FH_lock", "(J)Z", (void *)FileManager_FH_lock },
        { "FH_unlock", "(J)Z", (void *)FileManager_FH_unlock },
        { "FH_GetSDCardInfo", "(II)I", (void *)FileManager_FH_GetSDCardInfo },

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





