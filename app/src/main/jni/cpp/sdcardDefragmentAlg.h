
#ifndef _SDCARDDEFRAGMENTALG_H_
#define _SDCARDDEFRAGMENTALG_H_

#define ALOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

#include <stdio.h>
#include <string.h> // memset
#include <stdint.h> // uint64_t
#include <dirent.h> // get folder file
#include <sys/stat.h>
#include <sys/mount.h> // mount
#include <sys/statvfs.h> // Init()
#include <fcntl.h> // open
#include <unistd.h> // fsync
#include <pthread.h> // mutex
#include <inttypes.h> // PRIu64

#include <algorithm>
#include <iostream>
#include <string>
#include <map>
#include <vector>

using namespace std;

#define TABLE_SIZE 8
#define MAINFOLDER_SIZE  5

#define NORULE_SIZE 128

#define CAMERA_ONE_FILE_MINI_LENGTH 16
#define CAMERA_ONE_FILE_MAX_LENGTH  17
#define CAMERA_TWO_FILE_MINI_LENGTH 18
#define CAMERA_TWO_FILE_MAX_LENGTH  19

#define MONTH_LIMIT  12
#define DAYS_LIMIT   31
#define HOUR_LIMIT   23
#define MINUTE_LIMIT 59
#define SECOND_LIMIT 59

#define SUCCESS (0)
#define SDCARD_PATH_ERROR (-2)
#define OPEN_FOLDER_ERROR (-3)
#define FOLDER_SPACE_OVER_LIMIT (-4)
#define EXIST_FILE_NUM_OVER_LIMIT (-5)
#define NO_SPACE_NO_NUMBER_TO_RECYCLE (-6)
#define TABLE_VERSION_TOO_OLD (-7)
#define TABLE_VERSION_CANNOT_RECOGNIZE (-8)
#define TABLE_READ_ERROR (-9)
#define SDCARD_DETECT_SIZE_ERROR (-10)
#define SDCARD_NOT_SUPPORT (-11)
#define QUEUE_FREE_ERROR (-12)

#define KILOBYTE (1 << 10)
#define MEGABYTE (1 << 20)
#define GIGABYTE (1 << 30)

enum eCameraType{
	e_CameraOne=1,
	e_CameraTwo
};

enum eFolderType{
	e_Event=0,
	e_Normal,
	e_Picture,
	e_System,
	e_HASH_EVENT,
	e_HASH_NORMAL,
	e_NMEA_EVENT,
	e_NMEA_NORMAL
};

enum eGetNum{
	e_getLimitNum=0,
	e_getCurrentNum
};

//Purpose: Check config file exist or not
//
//Input:  mount path
//Output: bool, true = 1, false = 0;
bool FH_ValidFormat(char* mount_path);

//
// Purpose: 1.Create Event,Manual,Normal,Parking,Picture,System folder
//          2.Use SDCARD space to calculate every folder can use file number
//          3.every file struct save in "Table.config"
// Input:  mount path
// Output: bool, true = 1, false = 0;
// ** If SDCARD not clear, return false **
int FH_Init(char* mount_path);

//
// Purpose: 1.Choice enum folderType to openfile
//          2.Get file_num from "Table.config"
//          3.If "Free" folder have extension for folderType, use it to open file.
// Input:  filename,
//         folderType: eunm eFolderType
// Output: FILE Pointer
// ** If file number > Table.config file_num, return NULL; **
string FH_Open(char* filename, eFolderType folderType);

//
// not use
// Return ture
bool FH_Close(void);

//
// not use
// Return ture
void FH_Sync(void);

//
// Purpose: 1.Compare absolute_filepath, if have folderType String, rename file to Free folder
//          2.The file will be change to (number) + folderType extension
// Input:  Delete file absolute path
// Output: bool, true = 1, false = 0;
bool FH_Delete(const char* absolute_filepath, eCameraType cameraType);

//
// Purpose: Finding the path oldest file ,and return absolute_filepath string
// Input:  enum eFolderType
// Output: oldest_filepath, ""
// ** Oldest file, means the file which is earliest modification time **
string FH_FindOldest(eFolderType folderType, eCameraType cameraType);

//
// Purpose:
// Input: enum eFolderType
// Output: The number of videos that can be recorded
// ** if number < 2, return 0.
int FH_CanUseFilenumber(eFolderType folderType);

//
// Purpose: before FH_Open, Check sdcard and folder status.
// Input: enum eFolderType
// Output: (Error situation)(look #define)
//         SDCARD_PATH_ERROR
//         EXIST_FILE_NUM_OVER_LIMIT
//         NO_SPACE_NO_NUMBER_TO_RECYCLE
//         OPEN_FOLDER_ERROR
//         FOLDER_SPACE_OVER_LIMIT
int FH_CheckFolderStatus(eFolderType folderType);


int FH_GetSDCardInfo(eFolderType folderType, eGetNum getNumOpt);

int FH_GetFolderCameraTypeNumber(eFolderType folderType, eCameraType cameraType);

//
// not use
// Return ture
bool FH_lock(FILE* fp);

//
// not use
// Return ture
bool FH_unlock(FILE* fp);

#endif /* _SDCARDDEFRAGMENTALG_H_ */
