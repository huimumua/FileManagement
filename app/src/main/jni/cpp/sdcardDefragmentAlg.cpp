#include "sdcardDefragmentAlg.h"


pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;

struct file_struct FH_Table[6] = {{"EVENT", ".eve", 0.2, 100*MEGABYTE, 0, 0, 0, 0}, // event
								{"MANUAL",  ".man", 0.1, 100*MEGABYTE, 0, 0, 0, 0}, // manual
								{"NORMAL",  ".nor", 0.2, 100*MEGABYTE, 0, 0, 0, 0}, // normal
								{"PARKING", ".par", 0.3, 100*MEGABYTE, 0, 0, 0, 0}, // parking
								{"PICTURE", ".pic", 0.1, 100*MEGABYTE, 0, 0, 0, 0}, // picture
								{"SYSTEM",  ".sys", 0.1, 10*MEGABYTE, 0, 0, 0, 0}}; // system

char g_mount_path[100] = {0};

int SDA_get_path_file_num(char* path){

	DIR *dp = opendir(path);
	struct dirent *dirp;

	if (dp == NULL){
		return -1;
	}

	int file_number = 0;

	while ((dirp = readdir(dp)) != NULL) {

		string filterFile = dirp->d_name;

		if((filterFile.compare(".") == 0) || (filterFile.compare("..") == 0)){
			continue;
		}

		if(filterFile[0] == '.'){
			continue;
		}
		
		file_number++;
	}
	closedir(dp);

	return file_number;
}

string SDA_get_first_filename(char* file_path, char* file_extension){

	vector<int> files = vector<int>();

	DIR *dp = opendir(file_path);
	struct dirent *dirp;

	if (dp == NULL){
		cout << "Error opening " << endl;
		return "";
	}

	while ((dirp = readdir(dp)) != NULL) {

		string filterFile = dirp->d_name;
		if(filterFile.find(file_extension) != -1){
			files.push_back(atoi(filterFile.substr(0, filterFile.find(".")).c_str()));
		}
	}

	closedir(dp);

	if(files.empty() == true){
		return "";
	}

	sort (files.begin(), files.end());

	int first_file_number = files.front();
	char first_filename[100];
	snprintf(first_filename, sizeof(first_filename), "%d%s", first_file_number, file_extension);

	return string(first_filename);
}

string SDA_get_last_filename(char* file_path, const char* file_extension){

	vector<int> files = vector<int>();

	DIR *dp = opendir(file_path);
	struct dirent *dirp;

	if (dp == NULL){
		cout << "Error opening " << endl;
		return "";
	}

	while ((dirp = readdir(dp)) != NULL) {

		string filterFile = dirp->d_name;
		if(filterFile.find(file_extension) != -1){
			files.push_back(atoi(filterFile.substr(0, filterFile.find(".")).c_str()));
		}
	}

	closedir(dp);

	if(files.empty() == true){
		return "";
	}

	sort (files.begin(), files.end());

	int last_file_number = files.back();
	char last_filename[100];
	snprintf(last_filename, sizeof(last_filename), "%d%s", last_file_number, file_extension);

	return string(last_filename);
}

int SDA_write_table_in_config(char* mount_path){
	int i;
	char table_config_path[100];
	snprintf(table_config_path, sizeof(table_config_path), "%s/table.config", mount_path);
	FILE* fp = fopen(table_config_path, "w");
	if(fp == NULL){
		cout << "open failed" << endl;
		return -1;
	}

	// cout << FH_Table[0].every_block_space << endl;

	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		fwrite(&FH_Table[i], sizeof(struct file_struct), 1, fp);
	}

	if(fwrite != 0){
		fclose(fp);
		return 0;
	}else{
		cout << "failed" << endl;
		fclose(fp);
		return -1;
	}
}

int SDA_read_table_file_num_from_config(char* mount_path, eFolderType folderType){

	char table_config_path[100];
	snprintf(table_config_path, sizeof(table_config_path), "%s/table.config", mount_path);
	FILE* fp = fopen(table_config_path, "r");
	if(fp == NULL){
		return -1;
	}

	int count = 0;
	int fileNum = 0;
	struct file_struct read_table;

	while(fread(&read_table, sizeof(file_struct), 1, fp)){
		
		if(count == folderType){
			fileNum = read_table.file_num;
			break;
		}
		count++;

	}

	fclose(fp);
	return fileNum;
}

int SDA_file_exists(char* filename)
{
        struct stat buf;
        int i = stat(filename, &buf);
	/* find file */
        if (i == 0)
        {
			return 1;
        }
        return -1;
}

int SDA_scan_sdcard_folder_exist(char* mount_path){

	int i;
	int rc;
	DIR *dp = opendir(mount_path);
	struct dirent *dirp;

	if (dp == NULL){
		return -1;
	}

	/* Clear exist_flag */
	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		FH_Table[i].exist_flag = 0;
	}

	while ((dirp = readdir(dp)) != NULL) {

		string filterFile = dirp->d_name;

		if((filterFile.compare(".") == 0) || (filterFile.compare("..") == 0)){
			continue;
		}

		for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
			if(filterFile.compare(FH_Table[i].folder_type) == 0){
				FH_Table[i].exist_flag = 1;
			}
		}
	}

	closedir(dp);
	return 0;
}

void SDA_get_structure_value_from_config(char* mount_path){

	int i = 0;
	char table_config_path[100];
	snprintf(table_config_path, sizeof(table_config_path), "%s/table.config", mount_path);
	FILE* fp = fopen(table_config_path, "r");

	struct file_struct read_table;

	while(fread(&read_table, sizeof(file_struct), 1, fp)){
		FH_Table[i].percent           = read_table.percent;
		FH_Table[i].every_block_space = read_table.every_block_space;
		FH_Table[i].avail_space       = read_table.avail_space;
		FH_Table[i].max_file_num      = read_table.max_file_num;
		FH_Table[i].file_num          = read_table.file_num;
		FH_Table[i].exist_flag        = read_table.exist_flag;

		i++;
	}

	fclose(fp);
}

// true = 1, false = 0;
bool FH_Init(char* mount_path){

	pthread_mutex_lock(&g_mutex);
	int i;
	int rc;

	if(mount_path == NULL){
		pthread_mutex_unlock(&g_mutex);
		return false;
	}else{
		strncpy(g_mount_path, mount_path, strlen(mount_path));
	}

	/* If mount_path have "table.config", return true */
	char config_file_path[100];
	snprintf(config_file_path, sizeof(config_file_path), "%s/table.config", mount_path);
	rc = SDA_file_exists(config_file_path);
	if(rc == 1){
		SDA_get_structure_value_from_config(mount_path);
	}

	/* If sdcard not clear, return false */
	// int fileNumber = SDA_get_path_file_num(mount_path);
	// if(fileNumber != 0){
	// 	cout << "Error: SDCARD not clear... Exist file number: " <<fileNumber << endl;
	// 	return false;
	// }

	struct statvfs buf;

	uint64_t mount_path_avail_size;

	if (statvfs(mount_path, &buf) == -1)
		perror("statvfs() error");
	else {
		printf("each block is %ld bytes big\n", buf.f_bsize);
		printf("there are %ld blocks available out of a total of %ld\n",
		buf.f_bavail, buf.f_blocks);
		printf("in bytes, that's %.ld bytes free out of a total of %.ld\n",
		((uint64_t)buf.f_bavail * buf.f_bsize),
		((uint64_t)buf.f_blocks * buf.f_bsize));

		mount_path_avail_size = ((uint64_t)buf.f_bavail * buf.f_bsize);
	}

	/* Scan which folder not exist */
	rc = SDA_scan_sdcard_folder_exist(mount_path);
	if(rc == -1){
		cout << "Scan SDCARD failed." << endl;
		pthread_mutex_unlock(&g_mutex);
		return false;
	}

	int count = 0;
	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		if(FH_Table[i].exist_flag == 0){
			count++;
		}
	}

	/* if exist_flag == 0, means folder not exist */
	float percent_add = 0;
	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		if(FH_Table[i].exist_flag == 0){
			percent_add += FH_Table[i].percent;
		}
	}

	/* Calucate folder avail space */
	if(count > 1){
		int recount = 0;
		uint64_t remaing_space = 0;
		for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
			/* if exist_flag == 0, means folder not exist */
			if(FH_Table[i].exist_flag == 0){
				recount++;
				if(recount == count){
					FH_Table[i].avail_space = mount_path_avail_size - remaing_space;
					break;
				}
				FH_Table[i].avail_space = (mount_path_avail_size/(percent_add*10))*(FH_Table[i].percent*10);
				remaing_space += FH_Table[i].avail_space;
			}
		}
	}else if (count == 1){
		for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
			if(FH_Table[i].exist_flag == 0){
				FH_Table[i].avail_space = mount_path_avail_size;
			}
		}
	}

	/* Calucate available file number */
	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		if(FH_Table[i].exist_flag == 0){
			FH_Table[i].file_num = FH_Table[i].avail_space/FH_Table[i].every_block_space;
		}
	}

	/* Create folder in SDCARD */
	char create_folder_path[100];
	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		if(FH_Table[i].exist_flag == 0){
			sprintf(create_folder_path, "%s/%s", mount_path, FH_Table[i].folder_type);
			mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
			memset(create_folder_path, 0, sizeof(create_folder_path));

			if(FH_Table[i].folder_type == "SYSTEM"){
				sprintf(create_folder_path, "%s/%s/FREE", mount_path, FH_Table[i].folder_type);
				mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
			}	
		}	
	}

	/* Check if SYSTEM exist SYSTEM/FREE not exist, create FREE*/
	if(FH_Table[5].exist_flag == 1){
		char free_folder_path[100];
		sprintf(free_folder_path, "%s/SYSTEM/FREE", mount_path);
		rc = SDA_file_exists(free_folder_path);
		if(rc != 1){
			cout << "Create FREE" << endl;
			mkdir(free_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
		}
	}

	/* Debug usage: print Structure data */
	// for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
	// 	cout << FH_Table[i].folder_type << " " 
	// 	<< FH_Table[i].folder_extension << " " << FH_Table[i].percent << " " 
	// 	<< FH_Table[i].every_block_space << " " << FH_Table[i].avail_space << " "
	// 	<< FH_Table[i].max_file_num << " " << FH_Table[i].file_num << " "
	// 	<< FH_Table[i].exist_flag << endl;
	// }



	/* JVC Define */
	/* If file_num > max_file_num, use max_file num */
	// for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
	// 	if(FH_Table[i].file_num > FH_Table[i].max_file_num){
	// 		FH_Table[i].file_num = FH_Table[i].max_file_num;
	// 	}
	// }

	/* write file_struct in config file */
	SDA_write_table_in_config(mount_path);

	pthread_mutex_unlock(&g_mutex);
	return true;
}


char* FH_Open(char* filename, eFolderType folderType){

	pthread_mutex_lock(&g_mutex);

	if(strlen(g_mount_path) == 0){
		pthread_mutex_unlock(&g_mutex);
		return NULL;
	}

	int rc;
	char config_file_path[100];
	snprintf(config_file_path, sizeof(config_file_path), "%s/table.config", g_mount_path);
	rc = SDA_file_exists(config_file_path);
	if(rc != 1){
		pthread_mutex_unlock(&g_mutex);
		return NULL;
	}

	char free_path[100];
	sprintf(free_path, "%s%s", g_mount_path,"/SYSTEM/FREE");
	char folder_path[100];
	
	string first_filename;

	static char purpose_path[100];

	snprintf(folder_path, sizeof(folder_path), "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

	int max_file_number = SDA_read_table_file_num_from_config(g_mount_path, folderType);

	// if free folder have .eve extension, rename .eve file to purpose filename
	first_filename = SDA_get_first_filename(free_path, FH_Table[folderType].folder_extension);
	if(first_filename.length() != 0){
		char first_path[100];
		snprintf(first_path, sizeof(first_path), "%s/%s", free_path, first_filename.c_str());

		snprintf(purpose_path, sizeof(purpose_path), "%s/%s", folder_path, filename);

		rename(first_path, purpose_path);

		pthread_mutex_unlock(&g_mutex);
		return purpose_path;

	// if no .eve extension in System/Free & folder file number < Event.file_num
	}else if(SDA_get_path_file_num(folder_path) < max_file_number){
		snprintf(purpose_path, sizeof(purpose_path), "%s/%s", folder_path, filename);

		pthread_mutex_unlock(&g_mutex);	
		return purpose_path;
	}else{
		cout << "file was full, please delete some file." << endl;

		pthread_mutex_unlock(&g_mutex);
		return NULL;
	}
}

// 
// true = 1, false = 0;
bool FH_Close(void){

	pthread_mutex_lock(&g_mutex);
	pthread_mutex_unlock(&g_mutex);
	return true;
}

// 
// true = 1, false = 0;
bool FH_Sync(void){

	pthread_mutex_lock(&g_mutex);
	pthread_mutex_unlock(&g_mutex);
	return true;
}


// 
// true = 1, false = 0;
bool FH_Delete(const char* absolute_filepath){

	pthread_mutex_lock(&g_mutex);

	int fd = open(absolute_filepath, O_RDWR);
	if(fd == -1){
		pthread_mutex_unlock(&g_mutex);
		return false;
	}
	close(fd);

	int i;
	int rc = -1;

	char free_path[100];
	snprintf(free_path, sizeof(free_path), "%s/SYSTEM/FREE", g_mount_path);

	string filename = absolute_filepath;
	string last_filename;

	/* If fine "Event" string, get last (number).eve in Free folder,
	  then rename absolute_filepath with (number+1).eve in Free folder */
	for(i=0; i<sizeof(FH_Table)/sizeof(file_struct); i++){
		if(filename.find(FH_Table[i].folder_type) != -1){
			// cout << "find FH_Table: " << FH_Table[i].folder_type << endl;
			last_filename = SDA_get_last_filename(free_path, FH_Table[i].folder_extension);
			int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
			char new_last_file[100] = {0};
			snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, FH_Table[i].folder_extension);
			// cout << "new last_filename: " << new_last_file << endl;	
			rc = rename(absolute_filepath, new_last_file);

			pthread_mutex_unlock(&g_mutex);
			return (rc == 0 ? true : false);
		}
	}
}


string FH_FindOldest(eFolderType folderType){

	pthread_mutex_lock(&g_mutex);

	char finding_path[128];
	snprintf(finding_path, sizeof(finding_path), "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

	DIR *dp = opendir(finding_path); 
	struct dirent *dirp;

	struct stat attrib;
	struct tm* clock;
	char file_timestramp[100] = {0};

	map<string, string> fileTable;

	
	if (dp == NULL){
		pthread_mutex_unlock(&g_mutex);
		return "";
	}

	/* get finding_path "file name" and "file timestamp", store in Map */
	while (dirp = readdir(dp)){
		// puts(dirp->d_name);
		string inPath_filename = dirp->d_name;
		// cout << inPath_filename << ":" << inPath_filename.length() << endl;
		char path_and_filename[100];
		snprintf(path_and_filename, sizeof(path_and_filename), "%s/%s", finding_path, inPath_filename.c_str());

		stat(path_and_filename, &attrib);
		strftime(file_timestramp, sizeof(file_timestramp), "%Y%m%d%H%M%S", localtime((const time_t *)&attrib.st_mtime));

		if(inPath_filename.compare("FREE") == 0){
			continue;
		}
		if(inPath_filename.compare(".") != 0 && (inPath_filename.compare("..") != 0)){
			// fileTable[time] = name;
			fileTable.insert(pair<string, string>(file_timestramp, inPath_filename));
		}
	}
	
	closedir(dp);

	if(fileTable.empty()){
		cout << "No file in path." << endl;

		pthread_mutex_unlock(&g_mutex);
		return "";
	}

	// default Map is sort (small to big)
	for(map<string, string>::iterator it  = fileTable.begin(); it != fileTable.end(); it++){
		// cout<<it->first<<" : "<<it->second<<endl;
		struct stat buf;
		string filename_inMap = it->second;
		char oldest_file_path[100];
		snprintf(oldest_file_path, sizeof(oldest_file_path), "%s/%s", finding_path, filename_inMap.c_str());

		pthread_mutex_unlock(&g_mutex);
		return string(oldest_file_path);
	}
}

//
// true = 1, false = 0;
bool FH_lock(FILE* fp){
	
	pthread_mutex_lock(&g_mutex);
	pthread_mutex_unlock(&g_mutex);
	return true;
}

//
// true = 1, false = 0
bool FH_unlock(FILE* fp){
	
	pthread_mutex_lock(&g_mutex);
	pthread_mutex_unlock(&g_mutex);
	return true;
}
