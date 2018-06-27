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

#define KILOBYTE (1 << 10)
#define MEGABYTE (1 << 20)

enum eFolderType{
	e_Event=0,
	e_Normal,
	e_Camera2,
	e_Picture,
	e_System,
	e_NMEA_EVENT,
	e_NMEA_NORMAL,
	e_NMEA_CAMERA2
};

struct file_struct{
	char folder_type[20];
	char folder_extension[7];
	float percent;
	uint64_t every_block_space;
	uint64_t avail_space;
	int max_file_num;
	int file_num;
	int exist_flag;

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
bool FH_Init(char* mount_path);

//
// Purpose: 1.Choice enum folderType to openfile
//          2.Get file_num from "Table.config"
//          3.If "Free" folder have extension for folderType, use it to open file.
// Input:  filename,    
//         folderType: eunm eFolderType
// Output: FILE Pointer
// ** If file number > Table.config file_num, return NULL; **
char* FH_Open(char* filename, eFolderType folderType);

//
// not use
// Return ture
bool FH_Close(void);

//
// not use
// Return ture
bool FH_Sync(void);

//
// Purpose: 1.Compare absolute_filepath, if have folderType String, rename file to Free folder
//          2.The file will be change to (number) + folderType extension
// Input:  Delete file absolute path
// Output: bool, true = 1, false = 0;
bool FH_Delete(const char* absolute_filepath);

//
// Purpose: Finding the path oldest file ,and return absolute_filepath string
// Input:  enum eFolderType
// Output: oldest_filepath, ""
// ** Oldest file, means the file which is earliest modification time **
string FH_FindOldest(eFolderType folderType);

//
// Purpose: 
// Input: enum eFolderType
// Output: The number of videos that can be recorded
// ** if number < 2, return 0. 
int FH_CanUseFilenumber(eFolderType folderType);

//
// not use
// Return ture
bool FH_lock(FILE* fp);

//
// not use
// Return ture
bool FH_unlock(FILE* fp);

#endif /* _SDCARDDEFRAGMENTALG_H_ */