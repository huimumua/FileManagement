#ifndef _SDCARDDEFRAGMENTALG_H_
#define _SDCARDDEFRAGMENTALG_H_

#include <stdio.h>
#include <string.h> // memset
#include <stdint.h> // uint64_t
#include <dirent.h> // get folder file
#include <sys/stat.h>
#include <sys/mount.h> // mount
#include <sys/statvfs.h> // Init()
#include <fcntl.h> // open
#include <unistd.h> //fsync

#include <algorithm>
#include <iostream>
#include <string>
#include <map>
#include <vector>

using namespace std;

#define KILOBYTE (1 << 10)
#define MEGABYTE (1 << 20)

struct file_struct{
	float percent;
	uint64_t every_block_space;
	int max_file_num;
	int file_num;
};

//
// Purpose: 1.Create Event,Manual,Normal,Parking,Picture,System folder
//          2.Use SDCARD space to calculate every folder can use file
//          3.every file struct save in "Table.config"
// Input:  mount path
// Output: bool, true = 1, false = 0;
// ** If SDCARD not clear, return false **
bool FH_Init(char* mount_path);

//
// Purpose: 1.Choice folderType to openfile
//          2.Get file_num from "Table.config"
//          3.If "Free" folder have extension for folderType, use it to open file.
// Input:  mount path, open filename,    
//         folderType: Event, Manual, Normal, Parking, Picture, System
// Output: FILE Pointer
// ** If file number > folderType file_num, return NULL; **
FILE* FH_Open(char* mount_path, char* filename, char *folderType);

//
// Purpose: Close opened file
// Input: Opened FILE Pointer
// Output: bool, true = 1, false = 0;
bool FH_Close(FILE* fp);

//
// Purpose: Move the data from cache to disc
// Input:  Opened FILE Pointer
// Output: bool, true = 1, false = 0;
bool FH_Sync(FILE* fp);

//
// Purpose: 1.Compare absolute_filepath, if have folderType String, rename file to Free folder
//          2.The file will be change to (number) + folderType extension
// Input:  mount path, Delete file absolute path
// Output: bool, true = 1, false = 0;
bool FH_Delete(char* mount_path, const char* absolute_filepath);

//
// Purpose: Finding the path oldest file ,and return absolute_filepath string
// Input:  finding folder path
// Output: oldest_filepath, ""
// ** Oldest file, means the file which is earliest modification time **
string FH_FindOldest(char* finding_path);

//
// not implement
// Return ture
bool FH_lock(FILE* fp);

//
// not implement
// Return ture
bool FH_unlock(FILE* fp);

#endif /* _SDCARDDEFRAGMENTALG_H_ */