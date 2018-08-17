#include <android/log.h>
#include "sdcardDefragmentAlg.h"
#include <sys/file.h>
#include <queue>

#define CFG_NAME "table.config"
const char TABLE_VERSION = 0x05;

#define LOG_TAG "sdcardDefragmentAlg.cpp"

#if 1
#define  MUTEX_LOCK    pthread_mutex_lock
#define  MUTEX_UNLOCK  pthread_mutex_unlock
#else
#define  MUTEX_LOCK(...)    file_lock()
#define  MUTEX_UNLOCK(...)  file_unlock()
#endif
#define FILE_LOCK "/sdcard/.sddefrag.lock"
static FILE* f = NULL;
int file_lock()
{
    if ( NULL == f )
    {
        f = fopen(FILE_LOCK, "w+");
    }
    if ( NULL == f )
    {
        ALOGE("this is jni call-> open /sdcard/.sddefrag.lock error. In func: %s, line:%d \n", __func__, __LINE__);
                return -1;
    }
    if(0 == flock(fileno(f), LOCK_EX))
    {
        ALOGE("this is jni call-> Lock success. In func: %s, line:%d \n", __func__, __LINE__);
        return 0;
    }
    ALOGE("this is jni call-> return 1. In func: %s, line:%d \n", __func__, __LINE__);
    return 1;
}

int file_unlock()
{
    if ( NULL == f )
    {
        ALOGE("this is jni call-> Not open /sdcard/.sddefrag.lock yet, error. In func: %s, line:%d \n", __func__, __LINE__);
        return -1;
    }
    if(0 == flock(fileno(f), LOCK_UN))
    {
        ALOGE("this is jni call-> Un-Lock success. In func: %s, line:%d \n", __func__, __LINE__);
        return 0;
    }
    return 1;
}

pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;

enum e_filename_format_error_code
{
    CAMERA_ONE_FORMAT=1,
    CAMERA_TWO_FORMAT,
    ERROR_FILENAME_FORMAT=-1
};

struct file_struct{
    int max_file_num;
    int file_num;
    int exist_flag;
    float percent;
    uint64_t every_block_space;
    uint64_t avail_space;
    char folder_type[32];
    char cam1_extension[32];
    char cam2_extension[32];
};

struct file_struct FH_Table[TABLE_SIZE] = {{0, 0, 0, 0.07, 20*MEGABYTE, 0, "EVENT",   ".1eve", ".2eve"}, // event
                                           {0, 0, 0, 0.82, 76*MEGABYTE, 0, "NORMAL",  ".1nor", ".2nor"}, // normal
                                           {0, 0, 0, 0.01,  1*MEGABYTE, 0, "PICTURE", ".1pic", ".2pic"}, // picture
                                           {0, 0, 0, 0.1,  76*MEGABYTE, 0, "SYSTEM",  ".1sys", ".2sys"}, // system
                                           {0, 0, 1, 0,    25*KILOBYTE, 0, "HASH_EVENT", ".1ehash", ".2ehash"},
                                           {0, 0, 1, 0,    25*KILOBYTE, 0, "HASH_NORMAL",".1nhash", ".2nhash"},
                                           {0, 0, 1, 0,   100*KILOBYTE, 0, "SYSTEM/NMEA/EVENT", ".1neve", ".2neve"},
                                           {0, 0, 1, 0,   100*KILOBYTE, 0, "SYSTEM/NMEA/NORMAL",".1nnor", ".1nnor"}};

queue<string> event_camera_one_queue;
queue<string> event_camera_two_queue;
queue<string> normal_camera_one_queue;
queue<string> normal_camera_two_queue;
queue<string> picture_camera_one_queue;
queue<string> picture_camera_two_queue;
queue<string> hash_event_camera_one_queue;
queue<string> hash_event_camera_two_queue;
queue<string> hash_normal_camera_one_queue;
queue<string> hash_normal_camera_two_queue;
queue<string> nmea_event_camera_one_queue;
queue<string> nmea_event_camera_two_queue;
queue<string> nmea_normal_camera_one_queue;
queue<string> nmea_normal_camera_two_queue;

char g_mount_path[NORULE_SIZE] = "\0";

int detect_filename_format(string str){
    if(str.length() < CAMERA_ONE_FILE_MINI_LENGTH || str.length() > CAMERA_TWO_FILE_MAX_LENGTH){
        return ERROR_FILENAME_FORMAT;
    }

    if((str.substr(12,2).compare("_2")) == 0){
        if(str.length() < CAMERA_TWO_FILE_MINI_LENGTH){
            return ERROR_FILENAME_FORMAT;
        }
        if(atoi(str.substr(2,2).c_str()) > MONTH_LIMIT){
            return ERROR_FILENAME_FORMAT;
        }
        // if filename != Days formant
        if(atoi(str.substr(4,2).c_str()) > DAYS_LIMIT){
            return ERROR_FILENAME_FORMAT;
        }
        // if filename != Hour formant
        if(atoi(str.substr(6,2).c_str()) > HOUR_LIMIT){
            return ERROR_FILENAME_FORMAT;
        }
        // if filename != Minute formant
        if(atoi(str.substr(8,2).c_str()) > MINUTE_LIMIT){
            return ERROR_FILENAME_FORMAT;
        }
        // if filename != second formant
        if(atoi(str.substr(10,2).c_str()) > SECOND_LIMIT){
            return ERROR_FILENAME_FORMAT;
        }
        return CAMERA_TWO_FORMAT;
    }

    if(str.length() > CAMERA_ONE_FILE_MAX_LENGTH){
        return ERROR_FILENAME_FORMAT;
    }
    if(atoi(str.substr(2,2).c_str()) > MONTH_LIMIT){
        return ERROR_FILENAME_FORMAT;
    }

    // if filename != Days formant
    if(atoi(str.substr(4,2).c_str()) > DAYS_LIMIT){
        return ERROR_FILENAME_FORMAT;
    }
    // if filename != Hour formant
    if(atoi(str.substr(6,2).c_str()) > HOUR_LIMIT){
        return ERROR_FILENAME_FORMAT;
    }

    // if filename != Minute formant
    if(atoi(str.substr(8,2).c_str()) > MINUTE_LIMIT){
        return ERROR_FILENAME_FORMAT;
    }

    // if filename != second formant
    if(atoi(str.substr(10,2).c_str()) > SECOND_LIMIT){
        return ERROR_FILENAME_FORMAT;
    }

    return CAMERA_ONE_FORMAT;
}

int SDA_get_recoder_file_num(char* path){
    ALOGE("this is jni call-> path = %s. In func: %s, line:%d \n", path, __func__, __LINE__);
    DIR *dp = opendir(path);
    struct dirent *dirp;

    if (dp == NULL){
        return -1;
    }

    int file_number = 0;

    while ((dirp = readdir(dp)) != NULL) {

        string filterFile = dirp->d_name;

        int rc = detect_filename_format(filterFile);
        if(rc == -1){
            continue;
        }

        file_number++;
    }
    closedir(dp);

    // cout << "filenumber= " << file_number << endl;
    ALOGE("this is jni call-> path = %s, file_number = %d. In func: %s, line:%d \n", path, file_number, __func__, __LINE__);
    return file_number;
}

string SDA_get_first_filename(const char* file_path, char* file_extension){

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
    char first_filename[NORULE_SIZE];
    snprintf(first_filename, NORULE_SIZE, "%d%s", first_file_number, file_extension);

    return string(first_filename);
}

string SDA_get_last_filename(const char* file_path, const char* file_extension){

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
    char last_filename[NORULE_SIZE];
    snprintf(last_filename, NORULE_SIZE, "%d%s", last_file_number, file_extension);

    return string(last_filename);
}

int SDA_get_free_extension_filenumber(eFolderType folderType){

    int extension_number = 0;

    char file_path[NORULE_SIZE];
    snprintf(file_path, NORULE_SIZE, "%s/SYSTEM/FREE", g_mount_path);

    // cout << "file_path: " << file_path << endl;

    DIR *dp = opendir(file_path);
    struct dirent *dirp;

    if (dp == NULL){
        cout << "Error opening " << endl;
        return -1;
    }

    while ((dirp = readdir(dp)) != NULL) {

        string filterFile = dirp->d_name;
        if(filterFile.find(FH_Table[folderType].cam1_extension) != -1){
            extension_number++;
        }
        if(filterFile.find(FH_Table[folderType].cam2_extension) != -1){
            extension_number++;
        }
    }
    // cout << "extension_number: " << extension_number << endl;

    closedir(dp);
    return extension_number;
}

int SDA_write_table_in_config(char* mount_path){
    int i;
    char table_config_path[NORULE_SIZE];
    snprintf(table_config_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);
    FILE* fp = fopen(table_config_path, "w");
    if(fp == NULL){
        cout << "open failed" << endl;
        return -1;
    }

    // cout << FH_Table[0].every_block_space << endl;
    fwrite(&TABLE_VERSION, 1 , 1, fp);
    for(i=0; i<TABLE_SIZE; i++){
        fwrite(&FH_Table[i], sizeof(struct file_struct), 1, fp);
    }
    fclose(fp);
    return 0;
}

void clear_queue(std::queue<string> &q){
    ALOGE("this is jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    queue<string> empty;
    swap(q, empty);
    ALOGE("this is jni call-> Out func: %s, line:%d \n", __func__, __LINE__);
    return;
}

int SDA_file_exists(char* filename)
{
    struct stat buf;
    int i = stat(filename, &buf);
    /* find file */
    if (i == 0)
    {
        return 0;
    }
    return -1;
}

int SDA_scan_sdcard_folder_exist(char* mount_path){

    int i;
    DIR *dp = opendir(mount_path);
    struct dirent *dirp;

    if (dp == NULL){
        return -1;
    }

    /* Clear exist_flag */
    for(i=0; i<MAINFOLDER_SIZE; i++){
        FH_Table[i].exist_flag = 0;
    }

    while ((dirp = readdir(dp)) != NULL) {

        string filterFile = dirp->d_name;

        if((filterFile.compare(".") == 0) || (filterFile.compare("..") == 0)){
            continue;
        }

        for(i=0; i<TABLE_SIZE; i++){
            if(filterFile.compare(FH_Table[i].folder_type) == 0){
                FH_Table[i].exist_flag = 1;
            }
        }
    }

    closedir(dp);
    return 0;
}

int SDA_get_structure_value_from_config(char* mount_path){
    ALOGE("this is jni call-> mount_path = %s. In func: %s, line:%d \n", mount_path, __func__, __LINE__);
    int i = 0;
    char table_config_path[NORULE_SIZE];
    snprintf(table_config_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);
    FILE* fp = fopen(table_config_path, "rb");

    struct file_struct read_table;

    char ver;
    int retlen = fread(&ver, 1, 1, fp);
    if ( retlen == 1 )
    {
        ALOGE("this is jni call-> retlen == 1 func: %s, line:%d \n", __func__, __LINE__);
        if ( ver == TABLE_VERSION )
        {
            for(i=0; i<TABLE_SIZE; i++) {
                retlen = fread(&read_table, sizeof(struct file_struct), 1, fp);
                if (retlen == 1)
                {
                    ALOGE("this is jni call-> retlen == sizeof(file_struct) %ld == 1 func: %s, line:%d \n",
                          sizeof(struct file_struct), __func__, __LINE__);
                    FH_Table[i].percent = read_table.percent;
                    FH_Table[i].every_block_space = read_table.every_block_space;
                    FH_Table[i].avail_space = read_table.avail_space;
                    FH_Table[i].max_file_num = read_table.max_file_num;
                    FH_Table[i].file_num = read_table.file_num;
                    FH_Table[i].exist_flag = read_table.exist_flag;
                }
            }
            retlen = fread(&read_table, sizeof(struct file_struct), 1, fp);
            if ( retlen == 0 )
            {
                retlen = feof(fp);
                ALOGE("this is jni call-> retlen = %d func: %s, line:%d \n", retlen, __func__,
                      __LINE__);
                if (retlen == 1) {
                    ALOGE("this is jni call-> read func: %s, line:%d \n", __func__, __LINE__);
                    fclose(fp);
                    return 0;
                }
            }
        }
    }

    ALOGE("this is jni call-> Out func: %s, line:%d \n", __func__, __LINE__);
    fclose(fp);
    return -1;
}

int set_max_file_num(uint64_t sdcard_size){
    ALOGE("this jni call-> sdcard_size = %" PRIu64 " In func: %s, line:%d \n", sdcard_size, __func__, __LINE__);
    int sd_size = sdcard_size/GIGABYTE;
    if(sd_size <= 0){
        ALOGE("this is jni call-> sdcard size = 0, sdcard_size = %" PRIu64 " Out func: %s, line:%d \n", sdcard_size, __func__, __LINE__);
        return SDCARD_DETECT_SIZE_ERROR;
    }
    if(sd_size < 4){
        FH_Table[e_Event].max_file_num = 10;
        FH_Table[e_Normal].max_file_num = 40;
        FH_Table[e_Picture].max_file_num = 30;
        ALOGE("this is jni call-> sd_size = %d, close to 4G SDCARD. Out func: %s, line:%d \n", sd_size, __func__, __LINE__);
        return SUCCESS;
    }else
    if(sd_size < 8){
        FH_Table[e_Event].max_file_num = 20;
        FH_Table[e_Normal].max_file_num = 80;
        FH_Table[e_Picture].max_file_num = 60;
        ALOGE("this is jni call-> sd_size = %d, close to 8G SDCARD. Out func: %s, line:%d \n", sd_size, __func__, __LINE__);
        return SUCCESS;
    }else
    if(sd_size < 16){
        FH_Table[e_Event].max_file_num = 40;
        FH_Table[e_Normal].max_file_num = 160;
        FH_Table[e_Picture].max_file_num = 120;
        ALOGE("this is jni call-> sd_size = %d, close to 16G SDCARD. Out func: %s, line:%d \n", sd_size, __func__, __LINE__);
        return SUCCESS;
    }else
    if(sd_size < 32){
        FH_Table[e_Event].max_file_num = 80;
        FH_Table[e_Normal].max_file_num = 320;
        FH_Table[e_Picture].max_file_num = 240;
        ALOGE("this is jni call-> sd_size = %d, close to 32G SDCARD. Out func: %s, line:%d \n", sd_size, __func__, __LINE__);
        return SUCCESS;
    }else
    if(sd_size < 64){
        FH_Table[e_Event].max_file_num = 160;
        FH_Table[e_Normal].max_file_num = 640;
        FH_Table[e_Picture].max_file_num = 480;
        ALOGE("this is jni call-> sd_size = %d, close to 64G SDCARD. Out func: %s, line:%d \n", sd_size, __func__, __LINE__);
        return SUCCESS;
    }else
    if(sd_size < 128){
        FH_Table[e_Event].max_file_num = 320;
        FH_Table[e_Normal].max_file_num = 1280;
        FH_Table[e_Picture].max_file_num = 960;
        ALOGE("this is jni call-> sd_size = %d, close to 128G SDCARD. Out func: %s, line:%d \n", sd_size, __func__, __LINE__);
        return SUCCESS;
    }
    ALOGE("this is jni call-> size bigger than 128G. not Support. Out func: %s, line:%d \n", __func__, __LINE__);
    return SDCARD_SIZE_NOT_SUPPORT;
}

void check_queue_status(int old_date_flag, queue<string>& folder_files_queue){
    ALOGE("this is jni call-> old_date_flag = %d. In func: %s, line:%d \n", old_date_flag, __func__, __LINE__);
    if(old_date_flag == 1){

        while(true){
            string queue_pop = folder_files_queue.front();
            if(atoi(queue_pop.substr(0,2).c_str()) >= 70){
                break;
            }

            folder_files_queue.pop();
            folder_files_queue.push(queue_pop);
        }
        ALOGE("this is jni call-> folder_files_queue.front() = %s, folder_files_queue.back() = %s. In func: %s, line:%d \n", folder_files_queue.front().c_str(), folder_files_queue.back().c_str(), __func__, __LINE__);
    }
    ALOGE("this is jni call-> old_date_flag = %d. Out func: %s, line:%d \n", old_date_flag, __func__, __LINE__);
    return;
}

int storage_file_in_queue(eFolderType folderType, queue<string>& camera_one_queue, queue<string>& camera_two_queue){
    char file_path[NORULE_SIZE];
    snprintf(file_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

    vector<string> files = vector<string>();

    DIR *dp = opendir(file_path);
    struct dirent *dirp;

    int camera_one_old_date_flag = 0;
    int camera_two_old_date_flag = 0;

    if (dp == NULL){
        cout << "Error opening " << endl;
        return 0;
    }

    while ((dirp = readdir(dp)) != NULL) {

        string inPath_filename = dirp->d_name;
        // cout << inPath_filename << endl;
        int rc = detect_filename_format(inPath_filename);
        if(rc == -1){
            continue;
        }
        files.push_back(inPath_filename);
    }

    closedir(dp);

    if(files.empty() == true){
        return 0;
    }

    sort (files.begin(), files.end());

    ALOGE("this is jni call-> vector sort finish, start print \n");

    for (vector<string>::const_iterator i = files.begin(); i != files.end(); ++i){
        cout << *i << ' ' << endl;
        string vector_str = *i;

        int rc = detect_filename_format(vector_str);
        if(rc == CAMERA_ONE_FORMAT){
            camera_one_queue.push(*i);
            ALOGE("this is jni call-> camera one format. %s,  \n", vector_str.c_str());
            if(atoi(vector_str.substr(0,2).c_str()) >= 70){
                ALOGE("this is jni call-> camera one format. old date flag.\n");
                camera_one_old_date_flag = 1;
            }
        }
        if(rc == CAMERA_TWO_FORMAT){
            camera_two_queue.push(*i);
            ALOGE("this is jni call-> camera two format. %s,  \n", vector_str.c_str());
            if(atoi(vector_str.substr(0,2).c_str()) >= 70){
                ALOGE("this is jni call-> camera two format. old date flag.\n");
                camera_two_old_date_flag = 1;
            }
        }
    }


    string first_file_name = files.front();

    cout << "first_filename = " << first_file_name << endl;

    check_queue_status(camera_one_old_date_flag, camera_one_queue);
    check_queue_status(camera_two_old_date_flag, camera_two_queue);

    ALOGE("this is jni call-> folderType = %d, Out func: %s, line:%d \n", folderType, __func__, __LINE__);
    return 0;
}

int checkTableVersion(char* mount_path){
    ALOGE("this is jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    char config_file_path[NORULE_SIZE];
    snprintf(config_file_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);

    FILE* fp = NULL;
    int n = 0;

    fp=fopen (config_file_path,"rb");
    if(fp==NULL){
        ALOGE("this is jni call-> Can't not open table config file. Out func: %s, line:%d \n", __func__, __LINE__);
        return OPEN_FOLDER_ERROR;
    }
    n = fgetc (fp);
    if(n == 0x45){ // 0x45 == 'E'
        ALOGE("this is jni call-> table version to old. Out func: %s, line:%d \n", __func__, __LINE__);
        fclose(fp);
        return TABLE_VERSION_TOO_OLD;
    }
    if(n == TABLE_VERSION){
        ALOGE("this is jni call-> Right table version, n = %d. Out func: %s, line:%d \n", n, __func__, __LINE__);
        fclose(fp);
        return SUCCESS;
    }

    fclose(fp);
    ALOGE("this is jni call-> Table version cannot recognize. Out func: %s, line:%d \n", __func__, __LINE__);
    return TABLE_VERSION_CANNOT_RECOGNIZE;
}

// true = 1, false = 0;
bool FH_ValidFormat(char* mount_path){
    ALOGE("this is jni call-> before mutex_lock. mount_path = %s. In func: %s, line:%d \n", mount_path, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. mount_path = %s. In func: %s, line:%d \n", mount_path, __func__, __LINE__);
    int rc;
    char config_file_path[NORULE_SIZE];
    snprintf(config_file_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);

    rc = SDA_file_exists(config_file_path);
    ALOGE("this is jni call-> before mutex_unlock. mount_path = %s. func: %s, line:%d \n", mount_path, __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. mount_path = %s. func: %s, line:%d \n", mount_path, __func__, __LINE__);
    return (rc == 0 ? true : false);
}

void queue_Release(void){
    ALOGE("this is jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    clear_queue(event_camera_one_queue);
    clear_queue(event_camera_two_queue);
    clear_queue(normal_camera_one_queue);
    clear_queue(normal_camera_two_queue);
    clear_queue(picture_camera_one_queue);
    clear_queue(picture_camera_two_queue);
    clear_queue(hash_event_camera_one_queue);
    clear_queue(hash_event_camera_two_queue);
    clear_queue(hash_normal_camera_one_queue);
    clear_queue(hash_normal_camera_two_queue);
    clear_queue(nmea_event_camera_one_queue);
    clear_queue(nmea_event_camera_two_queue);
    clear_queue(nmea_normal_camera_one_queue);
    clear_queue(nmea_normal_camera_two_queue);
    return;
}

// true = 1, false = 0;
int FH_Init(char* mount_path){
    ALOGE("this is jni call-> before mutex_lock. mount_path = %s, g_mutex = %p. In func: %s, line:%d \n", mount_path, g_mutex, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. mount_path = %s. g_mutex = %p. In func: %s, line:%d \n", mount_path, g_mutex, __func__, __LINE__);
    int i;
    int rc;

    if(mount_path == NULL){
        ALOGE("this is jni call-> before mutex_unlock. mount_path == NULL. Out func: %s, line:%d \n", __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. mount_path == NULL. Out func: %s, line:%d \n", __func__, __LINE__);
        return SDCARD_PATH_ERROR;
    }else{
        strncpy(g_mount_path, mount_path, strlen(mount_path));
    }

    struct statvfs buf;

    if (statvfs(mount_path, &buf) == -1){
        ALOGE("this is jni call-> before mutex_unlock. Sdcard path error. Out func: %s, line:%d \n", __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. Sdcard path error. Out func: %s, line:%d \n", __func__, __LINE__);
        return SDCARD_PATH_ERROR;
    }
    ALOGE("this is jni call-> Sdcard available space = %" PRIu64 ". func: %s, line:%d \n", ((uint64_t)buf.f_bavail * buf.f_bsize), __func__, __LINE__);

    uint64_t mount_path_block_size = ((uint64_t)buf.f_blocks * buf.f_bsize);
    uint64_t mount_path_avail_size = ((uint64_t)buf.f_bavail * buf.f_bsize);

    if(mount_path_avail_size < 76*MEGABYTE){
        ALOGE("this is jni call-> before mutex_unlock. Sdcard no space to use. mount_path_avail_size = %" PRIu64 ". Out func: %s, line:%d \n", mount_path_avail_size, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. Sdcard no space to use. mount_path_avail_size = %" PRIu64 ". Out func: %s, line:%d \n", mount_path_avail_size, __func__, __LINE__);
        return SDCARD_SPACE_FULL;
    }

    rc = set_max_file_num(mount_path_block_size);
    if(rc != SUCCESS){
        ALOGE("this is jni call-> before mutex_unlock. sdcard detect failed. Out func: %s, line:%d \n", __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. sdcard detect failed. Out func: %s, line:%d \n", __func__, __LINE__);
        return SDCARD_DETECT_SIZE_ERROR;
    }

    /* If mount_path doesn't have "CONFIG", return please format sdcard */
    char config_file_path[NORULE_SIZE];
    snprintf(config_file_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);
    rc = SDA_file_exists(config_file_path);
    if(rc == 0){
        rc = checkTableVersion(mount_path);
        if(rc != SUCCESS){
            ALOGE("this is jni call-> before mutex_unlock. Sdcard format error. Out func: %s, line:%d \n", __func__, __LINE__);
            MUTEX_UNLOCK(&g_mutex);
            ALOGE("this is jni call-> after mutex_unlock. Sdcard format error. Out func: %s, line:%d \n", __func__, __LINE__);
            return TABLE_VERSION_TOO_OLD;
        }
        int ret = SDA_get_structure_value_from_config(mount_path);
        if (ret != 0)
        {
            ALOGE("this is jni call-> before mutex_unlock. SDA_get_structure_value_from_config fail. Out func: %s, line:%d \n", __func__, __LINE__);
            MUTEX_UNLOCK(&g_mutex);
            ALOGE("this is jni call-> after mutex_unlock. SDA_get_structure_value_from_config fail. Out func: %s, line:%d \n", __func__, __LINE__);
            return TABLE_READ_ERROR;
        }
    }

    /* Scan which folder not exist */
    rc = SDA_scan_sdcard_folder_exist(mount_path);
    if(rc == -1){
        ALOGE("this is jni call-> before mutex_unlock. Sdcard mount_path error, mount_path = %s. Out func: %s, line:%d \n", mount_path, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. Sdcard mount_path error, mount_path = %s. Out func: %s, line:%d \n", mount_path, __func__, __LINE__);
        return SDCARD_PATH_ERROR;
    }

    int count = 0;
    for(i=0; i<TABLE_SIZE; i++){
        if(FH_Table[i].exist_flag == 0){
            count++;
        }
    }

    /* if exist_flag == 0, means folder not exist */
    float percent_add = 0;
    for(i=0; i<TABLE_SIZE; i++){
        if(FH_Table[i].exist_flag == 0){
            percent_add += FH_Table[i].percent;
        }
    }
    ALOGE("this is jni call-> every folder percent_add = %f. func: %s, line:%d \n", percent_add, __func__, __LINE__);

    /* Calucate folder avail space */
    if(count > 1){
        int recount = 0;
        uint64_t remaing_space = 0;
        for(i=0; i<TABLE_SIZE; i++){
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
        for(i=0; i<TABLE_SIZE; i++){
            if(FH_Table[i].exist_flag == 0){
                FH_Table[i].avail_space = mount_path_avail_size;
            }
        }
    }

    /* Calucate available file number */
    for(i=0; i<TABLE_SIZE; i++){
        if(FH_Table[i].exist_flag == 0){
            FH_Table[i].file_num = FH_Table[i].avail_space/FH_Table[i].every_block_space;
        }
    }

    /* Create folder in SDCARD */
    char create_folder_path[NORULE_SIZE];
    for(i=0; i<TABLE_SIZE; i++){
        if(FH_Table[i].exist_flag == 0){
            snprintf(create_folder_path, NORULE_SIZE, "%s/%s", mount_path, FH_Table[i].folder_type);
            mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
            memset(create_folder_path, 0, NORULE_SIZE);

            if(strcmp(FH_Table[i].folder_type, "SYSTEM") == 0){
                snprintf(create_folder_path, NORULE_SIZE, "%s/%s/FREE", mount_path, FH_Table[i].folder_type);
                mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
                memset(create_folder_path, 0, NORULE_SIZE);

                snprintf(create_folder_path, NORULE_SIZE, "%s/%s/NMEA", mount_path, FH_Table[i].folder_type);
                mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
                memset(create_folder_path, 0, NORULE_SIZE);

                snprintf(create_folder_path, NORULE_SIZE, "%s/%s", mount_path, FH_Table[e_NMEA_EVENT].folder_type);
                mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
                memset(create_folder_path, 0, NORULE_SIZE);

                snprintf(create_folder_path, NORULE_SIZE, "%s/%s", mount_path, FH_Table[e_NMEA_NORMAL].folder_type);
                mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
                memset(create_folder_path, 0, NORULE_SIZE);
            }
        }
    }

    snprintf(create_folder_path, NORULE_SIZE, "%s/%s", mount_path, FH_Table[e_HASH_EVENT].folder_type);
    mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
    memset(create_folder_path, 0, NORULE_SIZE);

    snprintf(create_folder_path, NORULE_SIZE, "%s/%s", mount_path, FH_Table[e_HASH_NORMAL].folder_type);
    mkdir(create_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
    memset(create_folder_path, 0, NORULE_SIZE);

    /* Check if SYSTEM exist but SYSTEM/FREE not exist, create FREE*/
    if(FH_Table[e_System].exist_flag == 1){
        char free_folder_path[NORULE_SIZE];
        sprintf(free_folder_path, "%s/SYSTEM/FREE", mount_path);
        rc = SDA_file_exists(free_folder_path);
        if(rc != 0){
            cout << "Create FREE" << endl;
            mkdir(free_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
        }
        char nmea_folder_path[NORULE_SIZE];
        sprintf(nmea_folder_path, "%s/SYSTEM/NMEA", mount_path);
        rc = SDA_file_exists(nmea_folder_path);
        if(rc != 0){
            cout << "Create NMEA" << endl;
            mkdir(nmea_folder_path, S_IRWXU | S_IRWXG | S_IROTH |S_IXOTH);
        }
    }

    /* JVC Define */
    /* If file_num > max_file_num, use max_file num */
     for(i=0; i<TABLE_SIZE; i++){
         if(i >= e_System){
             break;
         }
         if(FH_Table[i].file_num > FH_Table[i].max_file_num){
             FH_Table[i].file_num = FH_Table[i].max_file_num;
         }
     }
     FH_Table[e_HASH_EVENT].file_num = FH_Table[e_Event].file_num; //HASH_EVENT   = EVENT file_num
     FH_Table[e_HASH_NORMAL].file_num = FH_Table[e_Normal].file_num; //HASH_NORMAL  = NORMAL file_num
     FH_Table[e_NMEA_EVENT].file_num = FH_Table[e_Event].file_num; //NMEA/EVENT   = EVENT file_num
     FH_Table[e_NMEA_NORMAL].file_num = FH_Table[e_Normal].file_num; //NMEA/NORMAL  = NORMAL file_num

    /* Debug usage: print Structure data */
    for(i=0; i<TABLE_SIZE; i++){
        ALOGE("this is jni call-> %s, %s, %s, %f, %" PRId64", %" PRId64 ", %d, %d, %d, func: %s, line:%d \n",
              FH_Table[i].folder_type,
              FH_Table[i].cam1_extension,
              FH_Table[i].cam2_extension,
              FH_Table[i].percent,
              FH_Table[i].every_block_space,
              FH_Table[i].avail_space,
              FH_Table[i].max_file_num,
              FH_Table[i].file_num,
              FH_Table[i].exist_flag,
              __func__, __LINE__ );
    }

    /* write file_struct in config file */
    SDA_write_table_in_config(mount_path);

    queue_Release();

    storage_file_in_queue(e_Event, event_camera_one_queue, event_camera_two_queue);
    storage_file_in_queue(e_Normal, normal_camera_one_queue, normal_camera_two_queue);
    storage_file_in_queue(e_Picture, picture_camera_one_queue, picture_camera_two_queue);
    storage_file_in_queue(e_HASH_EVENT, hash_event_camera_one_queue, hash_event_camera_two_queue);
    storage_file_in_queue(e_HASH_NORMAL, hash_normal_camera_one_queue, hash_normal_camera_two_queue);
    storage_file_in_queue(e_NMEA_EVENT, nmea_event_camera_one_queue, nmea_event_camera_two_queue);
    storage_file_in_queue(e_NMEA_NORMAL, nmea_normal_camera_one_queue, nmea_normal_camera_two_queue);

    ALOGE("this is jni call-> before mutex_unlock. Init finish. before unlock_mutex. Out func: %s, line:%d \n", __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. Init finish. after unlock_mutex. Out func: %s, line:%d \n", __func__, __LINE__);
    return INIT_SUCCESS;
}

string open_file_and_save_in_queue(char* filename, eFolderType folderType, queue<string>& camera_one_queue, queue<string>& camera_two_queue){
    ALOGE("this is jni call-> filename = %s, folderType = %d. In func: %s, line:%d \n", filename, folderType, __func__, __LINE__);
    char free_path[NORULE_SIZE];
    snprintf(free_path, NORULE_SIZE, "%s%s", g_mount_path,"/SYSTEM/FREE");

    char folder_path[NORULE_SIZE];
    char purpose_path[NORULE_SIZE];
    string input_filename = filename;
    string first_filename;

    snprintf(folder_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);
    int recoder_file_already_exist_num = SDA_get_recoder_file_num(folder_path);
    int extension_number = SDA_get_free_extension_filenumber(folderType);
    int recoder_file_num = recoder_file_already_exist_num + extension_number;
    ALOGE("this is jni call -> folderType = %d, recoder_file_already_exist_num = %d, extension_number = %d, recoder_file_num = %d. func: %s, line:%d \n", folderType, recoder_file_already_exist_num, extension_number, recoder_file_num, __func__, __LINE__);

    int rc = detect_filename_format(input_filename);
    if(rc == CAMERA_ONE_FORMAT){
        first_filename = SDA_get_first_filename(free_path, FH_Table[folderType].cam1_extension);
    }
    if(rc == CAMERA_TWO_FORMAT){
        first_filename = SDA_get_first_filename(free_path, FH_Table[folderType].cam2_extension);
    }
    // if free folder have extension file, rename file to purpose filename
    if(first_filename.length() != 0){
        char first_path[NORULE_SIZE];
        snprintf(first_path, NORULE_SIZE, "%s/%s", free_path, first_filename.c_str());

        snprintf(purpose_path, NORULE_SIZE, "%s/%s", folder_path, filename);

        rename(first_path, purpose_path);

        int rc = detect_filename_format(input_filename);
        if(rc == CAMERA_ONE_FORMAT){
            camera_one_queue.push(input_filename);
        }
        if(rc == CAMERA_TWO_FORMAT){
            camera_two_queue.push(input_filename);
        }
        int exist_file_number = camera_one_queue.size() + camera_two_queue.size();

        ALOGE("this is jni call-> folderType = %d, exist file in SYSTEM/FREE/%s. exist_file_number = %d, return filename = %s. Out func: %s, line:%d \n", folderType, first_filename.c_str(), exist_file_number, purpose_path, __func__, __LINE__);
        return string(purpose_path);
    }
    if(recoder_file_num < FH_Table[folderType].file_num){
        ALOGE("this is jni call-> folderType = %d, recoder_file_num = %d, FH_Table file_num = %d. func: %s, line:%d \n", folderType, recoder_file_num, FH_Table[folderType].file_num, __func__, __LINE__);
        snprintf(purpose_path, NORULE_SIZE, "%s/%s", folder_path, filename);

        int rc = detect_filename_format(input_filename);
        if(rc == CAMERA_ONE_FORMAT){
            camera_one_queue.push(input_filename);
        }
        if(rc == CAMERA_TWO_FORMAT){
            camera_two_queue.push(input_filename);
        }
        int exist_file_number = camera_one_queue.size() + camera_two_queue.size();
        ALOGE("this is jni call->  folderType = %d, exist_file_number = %d, file number not arrive limit. return filename = %s. Out func: %s, line:%d \n", folderType, exist_file_number, filename, __func__, __LINE__);
        return string(purpose_path);
    }
    ALOGE("this is jni call-> FH_Open file was full, please delete some file. FH_Open folderType = %d, exist_file_number = %d, FH_Table file_num = %d. Out func: %s, line:%d \n", folderType, recoder_file_num, FH_Table[folderType].file_num, __func__, __LINE__);
    return "";
}

string FH_Open(char* filename, eFolderType folderType){
    ALOGE("this is jni call-> before mutex_lock. folderType = %d, g_mutex = %p. PID = %d. In func: %s, line:%d \n", folderType, g_mutex, getpid(), __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. folderType = %d, g_mutex = %p. PID = %d. In func: %s, line:%d \n", folderType, g_mutex, getpid(), __func__, __LINE__);

    if(strlen(g_mount_path) == 0){
        ALOGE("this is jni call-> before mutex_lock. folderType = %d. global_mount_path = %s. Out func: %s, line:%d \n", folderType, g_mount_path, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_lock. folderType = %d. global_mount_path = %s. Out func: %s, line:%d \n", folderType, g_mount_path, __func__, __LINE__);
        return "";
    }

    string open_filename;

    switch (folderType) {
        case e_Event:
            open_filename = open_file_and_save_in_queue(filename, folderType, event_camera_one_queue, event_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        case e_Normal:
            open_filename = open_file_and_save_in_queue(filename, folderType, normal_camera_one_queue, normal_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        case e_Picture:
            open_filename = open_file_and_save_in_queue(filename, folderType, picture_camera_one_queue, picture_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        case e_HASH_EVENT:
            open_filename = open_file_and_save_in_queue(filename, folderType, hash_event_camera_one_queue, hash_event_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        case e_HASH_NORMAL:
            open_filename = open_file_and_save_in_queue(filename, folderType, hash_normal_camera_one_queue, hash_normal_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        case e_NMEA_EVENT:
            open_filename = open_file_and_save_in_queue(filename, folderType, nmea_event_camera_one_queue, nmea_event_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        case e_NMEA_NORMAL:
            open_filename = open_file_and_save_in_queue(filename, folderType, nmea_normal_camera_one_queue, nmea_normal_camera_two_queue);
            ALOGE("this is jni call-> folderType = %d, open_filename = %s. func: %s, line:%d \n", folderType, open_filename.c_str(), __func__, __LINE__);
            break;
        default:
            ALOGE("this is jni call-> folerType Error, folderType = %d. func: %s, line:%d \n", folderType, __func__, __LINE__);
            break;
    }

    ALOGE("this is jni call-> before mutex_unlock. folderType = %d, return filename = %s, g_mutex = %p. Out func: %s, line:%d \n", folderType, open_filename.c_str(), g_mutex, __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. folderType = %d, return filename = %s, g_mutex = %p. Out func: %s, line:%d \n", folderType, open_filename.c_str(), g_mutex, __func__, __LINE__);
    return open_filename;
}

//
// true = 1, false = 0;
bool FH_Close(void){
    MUTEX_LOCK(&g_mutex);
    MUTEX_UNLOCK(&g_mutex);
    return true;
}




//
// true = 1, false = 0;
bool FH_Delete(const char* absolute_filepath, eCameraType cameraType){
    ALOGE("this is jni call-> before mutex_lock. absolute_filepath = %s, cameraType = %d. In func: %s, line:%d \n", absolute_filepath, cameraType, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. absolute_filepath = %s, cameraType = %d. In func: %s, line:%d \n", absolute_filepath, cameraType, __func__, __LINE__);
    /* !!!! modify to exist func */
    int fd = open(absolute_filepath, O_RDWR);
    if(fd == -1){
        ALOGE("this is jni call-> before mutex_unlock. absolute_filepath not exist, absolute_filepath = %s, cameraType = %d. Out func: %s, line:%d \n", absolute_filepath, cameraType, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. absolute_filepath not exist, absolute_filepath = %s, cameraType = %d, Out func: %s, line:%d \n", absolute_filepath, cameraType, __func__, __LINE__);
        return false;
    }
    close(fd);

    int i = 0;
    int rc = -1;

    char free_path[NORULE_SIZE];
    snprintf(free_path, NORULE_SIZE, "%s/SYSTEM/FREE", g_mount_path);

    string filename = absolute_filepath;
    string last_filename;

    /* If fine "Event" string, get last (number).eve in Free folder,
      then rename absolute_filepath with (number+1).eve in Free folder */
    if(filename.find("HASH") != -1){
        i=e_HASH_EVENT;
    }
    if(filename.find("NMEA") != -1){
        i=e_NMEA_EVENT;
    }

    for(i; i<TABLE_SIZE; i++){

        if(filename.find(FH_Table[i].folder_type) != -1){
            switch (i) {
                case e_Event:
                    if(cameraType == e_CameraOne){
                        event_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        event_camera_two_queue.pop();
                        break;
                    }
                case e_Normal:
                    if(cameraType == e_CameraOne){
                        normal_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        normal_camera_two_queue.pop();
                        break;
                    }
                case e_Picture:
                    if(cameraType == e_CameraOne){
                        picture_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        picture_camera_two_queue.pop();
                        break;
                    }
                case e_HASH_EVENT:
                    if(cameraType == e_CameraOne){
                        hash_event_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        hash_event_camera_two_queue.pop();
                        break;
                    }
                case e_HASH_NORMAL:
                    if(cameraType == e_CameraOne){
                        hash_normal_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        hash_normal_camera_two_queue.pop();
                        break;
                    }
                case e_NMEA_EVENT:
                    if(cameraType == e_CameraOne){
                        nmea_event_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        nmea_event_camera_two_queue.pop();
                        break;
                    }
                case e_NMEA_NORMAL:
                    if(cameraType == e_CameraOne){
                        nmea_normal_camera_one_queue.pop();
                        break;
                    }
                    if(cameraType == e_CameraTwo){
                        nmea_normal_camera_two_queue.pop();
                        break;
                    }
                default:
                    ALOGE("this is jni call -> not find any about folder_type, absolute_filepath = %s. func: %s, line:%d \n", absolute_filepath, __func__, __LINE__);
                    break;
            }
            char new_last_file[NORULE_SIZE];
            if(cameraType == e_CameraOne){
                last_filename = SDA_get_last_filename(free_path, FH_Table[i].cam1_extension);
                int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
                snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, FH_Table[i].cam1_extension);
                ALOGE("this is jni call -> CAMERA1 TYPE absolute_filepath = %s, new_last_file = %s. func: %s, line:%d \n", absolute_filepath, new_last_file, __func__, __LINE__);
            }
            if(cameraType == e_CameraTwo){
                last_filename = SDA_get_last_filename(free_path, FH_Table[i].cam2_extension);
                int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
                snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, FH_Table[i].cam2_extension);
                ALOGE("this is jni call -> CAMERA2 TYPE absolute_filepath = %s, new_last_file = %s. func: %s, line:%d \n", absolute_filepath, new_last_file, __func__, __LINE__);
            }

            // cout << "new last_filename: " << new_last_file << endl;
            rc = rename(absolute_filepath, new_last_file);
            ALOGE("this is jni call-> before mutex_unlock. rename [%s] to [%s], Out func: %s, line:%d \n", absolute_filepath, new_last_file, __func__, __LINE__);
            MUTEX_UNLOCK(&g_mutex);
            ALOGE("this is jni call-> after mutex_unlock. rename [%s] to [%s], Out func: %s, line:%d \n", absolute_filepath, new_last_file, __func__, __LINE__);
            return (rc == 0 ? true : false);
        }
    }

    // not find any about folderType
    ALOGE("this is jni call-> before mutex_unlock. not find any about folderType. absolute_filepath = %s. Out func: %s, line:%d \n", absolute_filepath, __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. not find any about folderType. absolute_filepath = %s. Out func: %s, line:%d \n", absolute_filepath, __func__, __LINE__);
    return false;
}

string FH_FindOldest(eFolderType folderType, eCameraType cameraType){
    ALOGE("this is jni call-> before mutex_lock. folderType: %d. In func: %s, line:%d \n", folderType, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. folderType: %d. In func: %s, line:%d \n", folderType, __func__, __LINE__);
    char finding_path[NORULE_SIZE];
    snprintf(finding_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);
    ALOGE("this is jni call-> finding_path: %s. func: %s, line:%d \n", finding_path, __func__, __LINE__);
    int rc = SDA_file_exists(finding_path);
    if(rc != 0){
        ALOGE("this is jni call-> before mutex_unlock. finding path not exist. folderType: %d, finding_path: %s. Out func: %s, line:%d \n", folderType, finding_path, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. finding path not exist. folderType: %d, finding_path: %s. Out func: %s, line:%d \n", folderType, finding_path, __func__, __LINE__);
        return "";
    }

    string oldest_file;
    oldest_file.append(finding_path);
    oldest_file.append("/");

    switch (folderType) {
        case e_Event:
            if(cameraType == e_CameraOne){
                oldest_file.append(event_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(event_camera_two_queue.front());
                break;
            }
        case e_Normal:
            if(cameraType == e_CameraOne){
                oldest_file.append(normal_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(normal_camera_two_queue.front());
                break;
            }
        case e_Picture:
            if(cameraType == e_CameraOne){
                oldest_file.append(picture_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(picture_camera_two_queue.front());
                break;
            }
        case e_HASH_EVENT:
            if(cameraType == e_CameraOne){
                oldest_file.append(hash_event_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(hash_event_camera_two_queue.front());
                break;
            }
        case e_HASH_NORMAL:
            if(cameraType == e_CameraOne){
                oldest_file.append(hash_normal_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(hash_normal_camera_two_queue.front());
                break;
            }
        case e_NMEA_EVENT:
            if(cameraType == e_CameraOne){
                oldest_file.append(nmea_event_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(nmea_event_camera_two_queue.front());
                break;
            }
        case e_NMEA_NORMAL:
            if(cameraType == e_CameraOne){
                oldest_file.append(nmea_normal_camera_one_queue.front());
                break;
            }
            if(cameraType == e_CameraTwo){
                oldest_file.append(nmea_normal_camera_two_queue.front());
                break;
            }
        default:
            ALOGE("this is jni call -> folderType error, folderType = %d. func: %s, line:%d \n", folderType, __func__, __LINE__);
            break;
    }

    ALOGE("this is jni call-> before mutex_unlock. folderType: %d, oldest_file = %s. Out func: %s, line:%d \n", folderType, oldest_file.c_str(), __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. folderType: %d, oldest_file = %s. Out func: %s, line:%d \n", folderType, oldest_file.c_str(), __func__, __LINE__);
    return oldest_file;
}

int FH_CheckFolderStatus(eFolderType folderType){
    ALOGE("this is jni call-> before mutex_lock. folderType: %d. In func: %s, line:%d \n", folderType, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. folderType: %d. In func: %s, line:%d \n", folderType, __func__, __LINE__);

    struct statvfs buf;
    if (statvfs(g_mount_path, &buf) == -1) {
        ALOGE("this is jni call-> before mutex_unlock. Sdcard path error. folderType = %d, g_mount_path = %s. Out func: %s, line:%d \n", folderType, g_mount_path, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. Sdcard path error. folderType = %d, g_mount_path = %s. Out func: %s, line:%d \n", folderType, g_mount_path, __func__, __LINE__);
        return GLOBAL_SDCARD_PATH_ERROR;
    }

    uint64_t mount_path_avail_size = ((uint64_t)buf.f_bavail * buf.f_bsize);

    char folder_path[NORULE_SIZE];
    snprintf(folder_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

    // Calucate exist recode file number
    int recoder_file_already_exist_num = SDA_get_recoder_file_num(folder_path);
    int extension_number = SDA_get_free_extension_filenumber(folderType);
    int recoder_file_num = recoder_file_already_exist_num + extension_number;
    ALOGE("this is jni call -> folderType = %d, recoder_file_already_exist_num = %d, extension_number = %d, recoder_file_num = %d. func: %s, line:%d \n", folderType, recoder_file_already_exist_num, extension_number, recoder_file_num, __func__, __LINE__);

    // file over limit
    if (recoder_file_num > FH_Table[folderType].file_num){
        ALOGE("this is jni call -> before mutex_unlock. Existing record file and over limit number. folderType: %d. Out func: %s, line:%d \n", folderType, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call -> after mutex_unlock. Existing record file and over limit number. folderType: %d. Out func: %s, line:%d \n", folderType, __func__, __LINE__);
        return EXIST_FILE_NUM_OVER_LIMIT;
    }

    uint64_t sdcard_avail_size = 0;
    if (mount_path_avail_size < FH_Table[folderType].avail_space) {
        sdcard_avail_size = mount_path_avail_size;
    } else {
        sdcard_avail_size = FH_Table[folderType].avail_space;
    }

    uint64_t using_file_size = 0;
    char path_and_filename[NORULE_SIZE];
    DIR *dp = NULL;
    dp = opendir(folder_path);
    struct dirent *dirp;
    struct stat attrib;

    if (dp == NULL) {
        ALOGE("this is jni call -> before mutex_unlock. Open folder error. folderType: %d, folder_path = %s. Out func: %s, line:%d \n", folderType, folder_path, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call -> after mutex_unlock. Open folder error. folderType: %d, folder_path = %s. Out func: %s, line:%d \n", folderType, folder_path, __func__, __LINE__);
        return OPEN_FOLDER_ERROR;
    }
    while (dirp = readdir(dp)) {
        string filename = dirp->d_name;
        if ((filename.compare(".") == 0) || (filename.compare("..") == 0)) {
            continue;
        }

        snprintf(path_and_filename, NORULE_SIZE, "%s/%s", folder_path, filename.c_str());
        stat(path_and_filename, &attrib);
        using_file_size = using_file_size + attrib.st_size;
        memset(path_and_filename, 0, NORULE_SIZE);
    }
    closedir(dp);
    if (FH_Table[folderType].avail_space < using_file_size) {
        ALOGE("this is jni call -> before mutex_unlock. Out of folder space limit. folderType = %d, FH_Table[folderType].avail_space = %" PRId64", using_file_size = %" PRId64". Out func: %s, line:%d \n", folderType, FH_Table[folderType].avail_space, using_file_size, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call -> after mutex_unlock. Out of folder space limit. folderType = %d, FH_Table[folderType].avail_space = %" PRId64", using_file_size = %" PRId64". Out func: %s, line:%d \n", folderType, FH_Table[folderType].avail_space, using_file_size, __func__, __LINE__);
        return FOLDER_SPACE_OVER_LIMIT;
    }

    uint64_t folder_avail_size = FH_Table[folderType].avail_space - using_file_size;
    if (sdcard_avail_size < folder_avail_size) {
        folder_avail_size = sdcard_avail_size;
    }

    int avail_record_num = recoder_file_num + (folder_avail_size / FH_Table[folderType].every_block_space);
    if (avail_record_num < 2) {
        ALOGE("this is jni call-> before mutex_unlock. sdcard no space, record file not enough to recycle. folderType = %d, avail_record_num = %d. Out func: %s, line:%d \n", folderType, avail_record_num, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. sdcard no space, record file not enough to recycle. folderType = %d, avail_record_num = %d. Out func: %s, line:%d \n", folderType, avail_record_num, __func__, __LINE__);
        return NO_SPACE_NO_NUMBER_TO_RECYCLE;
    }
    if (avail_record_num < FH_Table[folderType].file_num) {
        FH_Table[folderType].file_num = avail_record_num;
    }

    ALOGE("this is jni call-> %s, %s, %s, %f, %" PRId64", %" PRId64 ", %d, %d, %d, func: %s, line:%d \n",
          FH_Table[folderType].folder_type,
          FH_Table[folderType].cam1_extension,
          FH_Table[folderType].cam2_extension,
          FH_Table[folderType].percent,
          FH_Table[folderType].every_block_space,
          FH_Table[folderType].avail_space,
          FH_Table[folderType].max_file_num,
          FH_Table[folderType].file_num,
          FH_Table[folderType].exist_flag,
          __func__, __LINE__);
    ALOGE("this is jni call-> before mutex_unlock. folderType = %d, return file_num = %d. Out func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. folderType = %d, return file_num = %d. Out func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
    return FH_Table[folderType].file_num;
}

int FH_GetSDCardInfo(eFolderType folderType, eGetNum getNumOpt){
    ALOGE("this is jni call-> before mutex_lock. folderType = %d, getNumOpt = %d. In func: %s, line:%d \n", folderType, getNumOpt, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. folderType = %d, getNumOpt = %d. In func: %s, line:%d \n", folderType, getNumOpt, __func__, __LINE__);
    if (getNumOpt == e_getLimitNum){
        ALOGE("this is jni call-> before mutex_unlock. folderType = %d, limit_file_num = %d. Out func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
        MUTEX_UNLOCK(&g_mutex);
        ALOGE("this is jni call-> after mutex_unlock. folderType = %d, limit_file_num = %d. Out func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
        return FH_Table[folderType].file_num;
    }
    int current_num = -1;
    if (getNumOpt == e_getCurrentNum){
        switch(folderType){
            case e_Event:
                current_num = event_camera_one_queue.size() + event_camera_two_queue.size();
                break;
            case e_Normal:
                current_num = normal_camera_one_queue.size() + normal_camera_two_queue.size();
                break;
            case e_Picture:
                current_num = picture_camera_one_queue.size() + picture_camera_two_queue.size();
                break;
            default:
                ALOGE("this is jni call-> folderType error. folderType = %d. func: %s, line:%d \n", folderType, __func__, __LINE__);
                break;
        }
    }

    ALOGE("this is jni call-> before mutex_unlock. folderType = %d, current_num = %d. Out func: %s, line:%d \n", folderType, current_num, __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. folderType = %d, current_num = %d. Out func: %s, line:%d \n", folderType, current_num, __func__, __LINE__);
    return current_num;
}

int FH_GetFolderCameraTypeNumber(eFolderType folderType, eCameraType cameraType){
    ALOGE("this is jni call-> before mutex_lock. folderType = %d, cameraType = %d. In func: %s, line:%d \n", folderType, cameraType, __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. folderType = %d, cameraType = %d. In func: %s, line:%d \n", folderType, cameraType, __func__, __LINE__);
    int camera_number = 0;

    switch (folderType) {
        case e_Event:
            if(cameraType == e_CameraOne){
                camera_number = event_camera_one_queue.size();
                break;
            }
            if(cameraType == e_CameraTwo){
                camera_number = event_camera_two_queue.size();
                break;
            }
        case e_Normal:
            if(cameraType == e_CameraOne){
                camera_number = normal_camera_one_queue.size();
                break;
            }
            if(cameraType == e_CameraTwo){
                camera_number = normal_camera_two_queue.size();
                break;
            }
        case e_Picture:
            if(cameraType == e_CameraOne){
                camera_number = picture_camera_one_queue.size();
                break;
            }
            if(cameraType == e_CameraTwo){
                camera_number = picture_camera_two_queue.size();
                break;
            }
        default:
            ALOGE("this is jni call -> folderType error, folderType = %d, cameraType = %d. func: %s, line:%d \n", folderType, cameraType, __func__, __LINE__);
            break;
    }
    ALOGE("this is jni call-> before mutex_unlock. folderType = %d, cameraType = %d, camera_number = %d. Out func: %s, line:%d \n", folderType, cameraType, camera_number, __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. folderType = %d, cameraType = %d, camera_number = %d. Out func: %s, line:%d \n", folderType, cameraType, camera_number, __func__, __LINE__);
    return camera_number;
}

void FH_Sync(void){
    ALOGE("this is jni call-> before mutex_lock. In func: %s, line:%d \n", __func__, __LINE__);
    MUTEX_LOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_lock. In func: %s, line:%d \n", __func__, __LINE__);
    sync();
    ALOGE("this is jni call-> before mutex_unlock. Out func: %s, line:%d \n", __func__, __LINE__);
    MUTEX_UNLOCK(&g_mutex);
    ALOGE("this is jni call-> after mutex_unlock. Out func: %s, line:%d \n", __func__, __LINE__);
}

//
// true = 1, false = 0;
bool FH_lock(FILE* fp){

    MUTEX_LOCK(&g_mutex);
    MUTEX_UNLOCK(&g_mutex);
    return true;
}

//
// true = 1, false = 0
bool FH_unlock(FILE* fp){

    MUTEX_LOCK(&g_mutex);
    MUTEX_UNLOCK(&g_mutex);
    return true;
}
