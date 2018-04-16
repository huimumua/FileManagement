#include <jni.h>
#include "sdcardDefragmentAlg.h"

/*extern "C"
JNIEXPORT jstring JNICALL*/

struct file_struct FH_Event   = {0.2, 100*MEGABYTE, 0,0};
struct file_struct FH_Manual  = {0.1, 100*MEGABYTE, 0,0};
struct file_struct FH_Normal  = {0.2, 100*MEGABYTE, 0,0};
struct file_struct FH_Parking = {0.3, 100*MEGABYTE, 0,0};
struct file_struct FH_Picture = {0.1, 100*MEGABYTE, 0,0};
struct file_struct FH_System  = {0.1, 10*MEGABYTE, 0,0};

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

		file_number++;
	}
	closedir(dp);

	return file_number;

}

string SDA_get_first_filename(char* file_path, const char* file_extension){

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


int SDA_create_file(char* folder_path, char* filename){
	int fd;

	char file_folder_and_filename[100];
	snprintf(file_folder_and_filename, sizeof(file_folder_and_filename), "%s/%s", folder_path, filename);

	fd = open(file_folder_and_filename, O_RDWR | O_CREAT, 0644);
	if(fd == -1){
		return -1;
	}
		
	return fd;
}

int SDA_write_table_in_config(char* mount_path){
	char table_config_path[100];
	snprintf(table_config_path, sizeof(table_config_path), "%s/table.config", mount_path);
	FILE* fp = fopen(table_config_path, "w");
	if(fp == NULL){
		cout << "open failed" << endl;
		return -1;
	}

	cout << FH_Event.every_block_space << endl;

	fwrite(&FH_Event,   sizeof(struct file_struct), 1, fp);
	fwrite(&FH_Manual,  sizeof(struct file_struct), 1, fp);
	fwrite(&FH_Normal,  sizeof(struct file_struct), 1, fp);
	fwrite(&FH_Parking, sizeof(struct file_struct), 1, fp);
	fwrite(&FH_Picture, sizeof(struct file_struct), 1, fp);
	fwrite(&FH_System,  sizeof(struct file_struct), 1, fp);

	if(fwrite != 0){
		fclose(fp);
		return 0;
	}else{
		cout << "failed" << endl;
		fclose(fp);
		return -1;
	}
}

int SDA_read_table_file_num_from_config(char* mount_path, char* folderType){
	int table_count = 0;
	if(strncmp(folderType, "Event", strlen(folderType)) == 0){
		table_count = 1;
	}else if(strncmp(folderType, "Manual", strlen(folderType)) == 0){
		table_count = 2;
	}else if(strncmp(folderType, "Normal", strlen(folderType)) == 0){
		table_count = 3;
	}else if(strncmp(folderType, "Parking", strlen(folderType)) == 0){
		table_count = 4;
	}else if(strncmp(folderType, "Picture", strlen(folderType)) == 0){
		table_count = 5;
	}else if(strncmp(folderType, "System", strlen(folderType)) == 0){
		table_count = 6;
	}else{
		return 0;
	}

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
		count++;
		if(count == table_count){
			fileNum = read_table.file_num;
			break;
		}
		
	}

	fclose(fp);
	return fileNum;
}

// true = 1, false = 0;
bool FH_Init(char* mount_path){

	int rc;
	char mount_type[] = "exfat";
	char mount_command[80];

	/* If sdcard not clear, return false */
	int fileNumber = SDA_get_path_file_num(mount_path);
	if(fileNumber != 0){
		cout << "sdcard not clear. " <<fileNumber << endl;
		return false;
	}

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

	uint64_t event_avail_size = mount_path_avail_size  * FH_Event.percent;
	uint64_t manual_avail_size = mount_path_avail_size * FH_Manual.percent;
	uint64_t normal_avail_size = mount_path_avail_size * FH_Normal.percent;
	uint64_t parking_avail_size = mount_path_avail_size* FH_Parking.percent;
	uint64_t picture_avail_size = mount_path_avail_size* FH_Picture.percent;
	uint64_t system_avail_size = mount_path_avail_size * FH_System.percent;

	/* Calculation every struct file_num */
	FH_Event.file_num = event_avail_size/FH_Event.every_block_space;
	FH_Manual.file_num = manual_avail_size/FH_Manual.every_block_space;
	FH_Normal.file_num = normal_avail_size/FH_Normal.every_block_space;
	FH_Parking.file_num = parking_avail_size/FH_Parking.every_block_space;
	FH_Picture.file_num = picture_avail_size/FH_Picture.every_block_space;

	uint64_t remaing_space = mount_path_avail_size - (FH_Event.every_block_space*FH_Event.file_num) - (FH_Manual.every_block_space*FH_Manual.file_num) -
		(FH_Normal.every_block_space*FH_Normal.file_num) - (FH_Parking.every_block_space*FH_Parking.file_num) - (FH_Picture.every_block_space*FH_Picture.file_num);
	// cout << "remaining" << remaing_space << endl;
	FH_System.file_num = remaing_space/FH_System.every_block_space;

/* If file_num > max_file_num, use max_file num*/
	// if(FH_Event.file_num > FH_Event.max_file_num){
	// 	FH_Event.file_num = FH_Event.max_file_num;
	// }
	// if(FH_Manual.file_num > FH_Manual.max_file_num){
	// 	FH_Manual.file_num = FH_Manual.max_file_num;
	// }
	// if(FH_Normal.file_num > FH_Normal.max_file_num){
	// 	FH_Normal.file_num = FH_Normal.max_file_num;
	// }
	// if(FH_Parking.file_num > FH_Parking.max_file_num){
	// 	FH_Parking.file_num = FH_Parking.max_file_num;
	// }
	// if(FH_Picture.file_num > FH_Picture.max_file_num){
	// 	FH_Picture.file_num = FH_Picture.max_file_num;
	// }
	// if(FH_System.file_num > FH_System.max_file_num){
	// 	FH_System.file_num = FH_System.max_file_num;
	// }

	/* write file_struct in config file */
	SDA_write_table_in_config(mount_path);

	char event_path[100];
	sprintf(event_path, "%s%s", mount_path,"/Event");
	mkdir(event_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
	char manual_path[100];
	sprintf(manual_path, "%s%s", mount_path,"/Manual");
	mkdir(manual_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
	char normal_path[100];
	sprintf(normal_path, "%s%s", mount_path,"/Normal");
	mkdir(normal_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
	char parking_path[100];
	sprintf(parking_path, "%s%s", mount_path,"/Parking");
	mkdir(parking_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
	char picture_path[100];
	sprintf(picture_path, "%s%s", mount_path,"/Picture");
	mkdir(picture_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
	char system_path[100];
	sprintf(system_path, "%s%s", mount_path,"/System");
	mkdir(system_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
	char free_path[100];
	sprintf(free_path, "%s%s", mount_path,"/System/Free");
	mkdir(free_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);

	return true;
}


int Open(char* mount_path, char* filename, char *folderType){

	char config_file_path[100];
	snprintf(config_file_path, sizeof(config_file_path), "%s/Table.config", mount_path);
	int fd = open(config_file_path, O_RDWR);
	if(fd == -1){
		return -1;
	}
	close(fd);

	const char event_extension[]   = ".eve";
	const char manual_extension[]  = ".man";
	const char normal_extension[]  = ".nor";
	const char parking_extension[] = ".par";
	const char picture_extension[] = ".pic";
	const char system_extension[]  = ".sys";

	char free_path[100];
	sprintf(free_path, "%s%s", mount_path,"/System/Free");
	char folder_path[100];
	snprintf(folder_path, sizeof(folder_path), "%s/%s", mount_path, folderType);

	string first_filename;

	if(strncmp(folderType, "Event", strlen(folderType)) == 0){

		int event_max_file = SDA_read_table_file_num_from_config(mount_path, folderType);
		
		// if free folder have .eve extension, rename .eve file to purpose filename
		first_filename = SDA_get_first_filename(free_path, event_extension);
		if(first_filename.length() != 0){
			char first_eve_path[100];
			snprintf(first_eve_path, sizeof(first_eve_path), "%s/%s", free_path, first_filename.c_str());

			char purpose_path[100];
			snprintf(purpose_path, sizeof(purpose_path), "%s/%s/%s", mount_path, folderType, filename);

			rename(first_eve_path, purpose_path);
			fd = open(purpose_path, O_RDWR);
			return fd;

		// if no .eve extension & event folder file number != Event.file_num
		}else if(SDA_get_path_file_num(folder_path) < event_max_file){

			fd = SDA_create_file(folder_path, filename);
			return fd;
		}else{

			cout << "file was full, please delete some file." << endl;
			return -1;
		}

	}else if(strncmp(folderType, "Manual", strlen(folderType)) == 0){
		
		int manual_max_file = SDA_read_table_file_num_from_config(mount_path, folderType);
		
		first_filename = SDA_get_first_filename(free_path, manual_extension);
		if(first_filename.length() != 0){
			char first_man_path[100];
			snprintf(first_man_path, sizeof(first_man_path), "%s/%s", free_path, first_filename.c_str());

			char purpose_path[100];
			snprintf(purpose_path, sizeof(purpose_path), "%s/%s/%s", mount_path, folderType, filename);

			rename(first_man_path, purpose_path);
			fd = open(purpose_path, O_RDWR);
			return fd;
		}else if(SDA_get_path_file_num(folder_path) < FH_Manual.file_num){

			fd = SDA_create_file(folder_path, filename);
			return fd;
		}else{

			cout << "file was full, please delete some file." << endl;
			return -1;
		}

	}else if(strncmp(folderType, "Normal", strlen(folderType)) == 0){

		int normal_max_file = SDA_read_table_file_num_from_config(mount_path, folderType);
		
		first_filename = SDA_get_first_filename(free_path, normal_extension);
		if(first_filename.length() != 0){
			char first_nor_path[100];
			snprintf(first_nor_path, sizeof(first_nor_path), "%s/%s", free_path, first_filename.c_str());

			char purpose_path[100];
			snprintf(purpose_path, sizeof(purpose_path), "%s/%s/%s", mount_path, folderType, filename);

			rename(first_nor_path, purpose_path);
			fd = open(purpose_path, O_RDWR);
			return fd;
		}else if(SDA_get_path_file_num(folder_path) < normal_max_file){

			fd = SDA_create_file(folder_path, filename);
			return fd;
		}else{

			cout << "file was full, please delete some file." << endl;
			return -1;
		}

	}else if(strncmp(folderType, "Parking", strlen(folderType)) == 0){

		int parking_max_file = SDA_read_table_file_num_from_config(mount_path, folderType);
		
		first_filename = SDA_get_first_filename(free_path, parking_extension);
		if(first_filename.length() != 0){
			char first_par_path[100];
			snprintf(first_par_path, sizeof(first_par_path), "%s/%s", free_path, first_filename.c_str());

			char purpose_path[100];
			snprintf(purpose_path, sizeof(purpose_path), "%s/%s/%s", mount_path, folderType, filename);

			rename(first_par_path, purpose_path);
			fd = open(purpose_path, O_RDWR);
			return fd;
		}else if(SDA_get_path_file_num(folder_path) < parking_max_file){

			fd = SDA_create_file(folder_path, filename);
			return fd;
		}else{

			cout << "file was full, please delete some file." << endl;
			return -1;
		}

	}else if(strncmp(folderType, "Picture", strlen(folderType)) == 0){

		int picture_max_file = SDA_read_table_file_num_from_config(mount_path, folderType);
		
		first_filename = SDA_get_first_filename(free_path, picture_extension);
		if(first_filename.length() != 0){
			char first_pic_path[100];
			snprintf(first_pic_path, sizeof(first_pic_path), "%s/%s", free_path, first_filename.c_str());

			char purpose_path[100];
			snprintf(purpose_path, sizeof(purpose_path), "%s/%s/%s", mount_path, folderType, filename);

			rename(first_pic_path, purpose_path);
			fd = open(purpose_path, O_RDWR);
			return fd;
		}else if(SDA_get_path_file_num(folder_path) < picture_max_file){

			fd = SDA_create_file(folder_path, filename);
			return fd;
		}else{

			cout << "file was full, please delete some file." << endl;
			return -1;
		}

	}else if(strncmp(folderType, "System", strlen(folderType)) == 0){

		int system_max_file = SDA_read_table_file_num_from_config(mount_path, folderType);
		
		first_filename = SDA_get_first_filename(free_path, system_extension);
		if(first_filename.length() != 0){
			char first_sys_path[100];
			snprintf(first_sys_path, sizeof(first_sys_path), "%s/%s", free_path, first_filename.c_str());

			char purpose_path[100];
			snprintf(purpose_path, sizeof(purpose_path), "%s/%s/%s", mount_path, folderType, filename);

			rename(first_sys_path, purpose_path);
			fd = open(purpose_path, O_RDWR);
			return fd;
		}else if(SDA_get_path_file_num(folder_path) < system_max_file){

			fd = SDA_create_file(folder_path, filename);
			return fd;
		}else{

			cout << "file was full, please delete some file." << endl;
			return -1;
		}

	}

	return -1;
}


FILE* FH_Open(char* mount_path, char* filename, char *folderType){
	int fd;
	FILE* fp = NULL;
	fd = Open(mount_path, filename, folderType);
	if(fd == -1){
		return NULL;
	}

	fp = fdopen(fd, "w");
	return (fp != NULL ? fp : NULL);
}

bool FH_Close(FILE* fp){
	if(fp == NULL){
		return false;
	}

	bool rc = false;
	rc = fclose(fp);
	return (rc == 0 ? true : false);
}

// fsync:
// true = 1, false = 0;
bool FH_Sync(FILE* fp){
	if(fp  == NULL){
		return false;
	}

	int fd;
	fd = fileno(fp);

	bool rc = false;
	rc = fsync(fd); 
	return (rc == 0 ? true : false);
}



// true = 1, false = 0;
bool FH_Delete(char* mount_path, const char* absolute_filepath){

	int fd = open(absolute_filepath, O_RDWR);
	if(fd == -1){
		return false;
	}
	close(fd);

	int rc = -1;

	const char event_extension[]   = ".eve";
	const char manual_extension[]  = ".man";
	const char normal_extension[]  = ".nor";
	const char parking_extension[] = ".par";
	const char picture_extension[] = ".pic";
	const char system_extension[]  = ".sys";

	char free_path[100];
	snprintf(free_path, sizeof(free_path), "%s/System/Free", mount_path);

	string filename = absolute_filepath;
	string last_filename;

	/* If fine "Event" string, get last (number).eve in Free folder,
	  then rename absolute_filepath with (number+1).eve in Free folder */
	if(filename.find("Event") != -1){

		last_filename = SDA_get_last_filename(free_path, event_extension);
		int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
		char new_last_file[100] = {0};
		snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, event_extension);
		// cout << new_last_file << endl;	
		rc = rename(absolute_filepath, new_last_file);
		return (rc == 0 ? true : false);

	}else if(filename.find("Manual") != -1){

		last_filename = SDA_get_last_filename(free_path, manual_extension);
		// cout << "last filename: " << last_filename << endl;
		int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
		char new_last_file[100] = {0};
		snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, manual_extension);

		rc = rename(absolute_filepath, new_last_file);
		return (rc == 0 ? true : false);

	}else if(filename.find("Normal") != -1){

		cout << "in here" << endl;
		last_filename = SDA_get_last_filename(free_path, normal_extension);
		int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
		cout << "number_filename: " << number_filename << endl;

		char new_last_file[100] = {0};
		snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, normal_extension);
		rc = rename(absolute_filepath, new_last_file);
		return (rc == 0 ? true : false);

	}else if(filename.find("Parking") != -1){

		last_filename = SDA_get_last_filename(free_path, parking_extension);
		int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
		char new_last_file[100] = {0};
		snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, parking_extension);
		rc = rename(absolute_filepath, new_last_file);
		return (rc == 0 ? true : false);

	}else if(filename.find("Picture") != -1){

		last_filename = SDA_get_last_filename(free_path, picture_extension);
		int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
		char new_last_file[100] = {0};
		snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, picture_extension);
		rc = rename(absolute_filepath, new_last_file);
		return (rc == 0 ? true : false);

	}else if(filename.find("System") != -1){

		last_filename = SDA_get_last_filename(free_path, system_extension);
		int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
		char new_last_file[100] = {0};
		snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, system_extension);
		rc = rename(absolute_filepath, new_last_file);
		return (rc == 0 ? true : false);

	}
}


string FH_FindOldest(char* finding_path){

	DIR *dp = opendir(finding_path); 
	struct dirent *dirp;

	struct stat attrib;
	struct tm* clock;
	char file_timestramp[100] = {0};

	map<string, string> fileTable;

	
	if (dp == NULL){
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
		strftime(file_timestramp, sizeof(file_timestramp), "%Y%m%d%H%M%S", localtime(&attrib.st_mtime));

		if(inPath_filename.compare(".") != 0 && (inPath_filename.compare("..") != 0)){
			// fileTable[time] = name;
			fileTable.insert(pair<string, string>(file_timestramp, inPath_filename));
		}
	}
	
	closedir(dp);

	if(fileTable.empty()){
		cout << "No file in path." << endl;
		return "";
	}

	// default Map is sort (small to big)
	for(map<string, string>::iterator it  = fileTable.begin(); it != fileTable.end(); it++){
		cout<<it->first<<" : "<<it->second<<endl;
		struct stat buf;
		string filename_inMap = it->second;
		char oldest_file_path[100];
		snprintf(oldest_file_path, sizeof(oldest_file_path), "%s/%s", finding_path, filename_inMap.c_str());

		return string(oldest_file_path);
	}
}

//
// true = 1, false = 0;
bool FH_lock(FILE* fp){
	// if(fd == -1){
	// 	return false;
	// }
	return true;
}

//
// true = 1, false = 0
bool FH_unlock(FILE* fp){
	// if(fd == -1){
	// 	return false;
	// }
	return true;
}
