#include <android/log.h>
#include "sdcardDefragmentAlg.h"
#include <queue>

#define CFG_NAME "table.config"
const char TABLE_VERSION = 0x03;

#define LOG_TAG "sdcardDefragmentAlg.cpp"
pthread_mutex_t g_mutex = PTHREAD_MUTEX_INITIALIZER;

struct file_struct{
    int max_file_num;
    int file_num;
    int exist_flag;
    float percent;
    uint64_t every_block_space;
    uint64_t avail_space;
    char folder_type[32];
    char folder_extension[32];
};

struct file_struct FH_Table[TABLE_SIZE] = {{0, 0, 0, 0.3, 76*MEGABYTE, 0, "EVENT", ".eve"}, // event
                                           {0, 0, 0, 0.5, 76*MEGABYTE, 0, "NORMAL",  ".nor"}, // normal
                                           {0, 0, 0, 0.01, 1*MEGABYTE, 0, "PICTURE", ".pic"}, // picture
                                           {0, 0, 0, 0.19,76*MEGABYTE, 0, "SYSTEM",  ".sys"}, // system
                                           {0, 0, 1, 0,   25*KILOBYTE, 0, "HASH_EVENT", ".ehash"},
                                           {0, 0, 1, 0,   25*KILOBYTE, 0, "HASH_NORMAL",".nhash"},
                                           {0, 0, 1, 0,  100*KILOBYTE, 0, "SYSTEM/NMEA/EVENT", ".neve"},
                                           {0, 0, 1, 0,  100*KILOBYTE, 0, "SYSTEM/NMEA/NORMAL",".nnor"}};

queue<string> event_files_queue;
queue<string> normal_files_queue;
queue<string> picture_files_queue;
queue<string> hash_event_files_queue;
queue<string> hash_normal_files_queue;
queue<string> nmea_event_files_queue;
queue<string> nmea_normal_files_queue;

char g_mount_path[NORULE_SIZE] = "\0";

int SDA_get_recoder_file_num(char* path){

    DIR *dp = opendir(path);
    struct dirent *dirp;

    if (dp == NULL){
        return -1;
    }

    int file_number = 0;

    while ((dirp = readdir(dp)) != NULL) {

        string filterFile = dirp->d_name;

        if(filterFile.length() < FILE_MINI_LENGTH || filterFile.length() > FILE_MAX_LENGTH){
            continue;
        }

        // if filename != Month formant
        if(atoi(filterFile.substr(2,2).c_str()) > MONTH_LIMIT){
            continue;
        }

        // if filename != Days formant
        if(atoi(filterFile.substr(4,2).c_str()) > DAYS_LIMIT){
            continue;
        }

        // if filename != Hour formant
        if(atoi(filterFile.substr(6,2).c_str()) > HOUR_LIMIT){
            continue;
        }

        // if filename != Minute formant
        if(atoi(filterFile.substr(8,2).c_str()) > MINUTE_LIMIT){
            continue;
        }

        // if filename != second formant
        if(atoi(filterFile.substr(10,2).c_str()) > SECOND_LIMIT){
            continue;
        }

        if((filterFile.compare(".") == 0) || (filterFile.compare("..") == 0)){
            continue;
        }

        file_number++;
    }
    closedir(dp);

    // cout << "filenumber= " << file_number << endl;

    return file_number;
}

void storge_record_file_in_map(map<string, string>& fileTable, char* finding_path){
    DIR *dp = opendir(finding_path);
    struct dirent *dirp;

    struct stat attrib;
    char file_timestramp[NORULE_SIZE];

    /* get finding_path "file name" and "file timestamp", store in Map */
    while (dirp = readdir(dp)){
        // puts(dirp->d_name);
        string inPath_filename = dirp->d_name;
        // cout << inPath_filename << ":" << inPath_filename.length() << endl;
        if(inPath_filename.length() < FILE_MINI_LENGTH || inPath_filename.length() > FILE_MAX_LENGTH){
            continue;
        }

        // if filename != Month format
        if(atoi(inPath_filename.substr(2,2).c_str()) > MONTH_LIMIT){
            continue;
        }

        // if filename != Days format
        if(atoi(inPath_filename.substr(4,2).c_str()) > DAYS_LIMIT){
            continue;
        }

        // if filename != Hour format
        if(atoi(inPath_filename.substr(6,2).c_str()) > HOUR_LIMIT){
            continue;
        }

        // if filename != Minute format
        if(atoi(inPath_filename.substr(8,2).c_str()) > MINUTE_LIMIT){
            continue;
        }

        // if filename != second format
        if(atoi(inPath_filename.substr(10,2).c_str()) > SECOND_LIMIT){
            continue;
        }

        char path_and_filename[NORULE_SIZE];
        snprintf(path_and_filename, NORULE_SIZE, "%s/%s", finding_path, inPath_filename.c_str());

        stat(path_and_filename, &attrib);
        strftime(file_timestramp, NORULE_SIZE, "%Y%m%d%H%M%S", localtime((const time_t *)&attrib.st_mtime));

        fileTable.insert(pair<string, string>(file_timestramp, inPath_filename));
    }

    closedir(dp);
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
        if(filterFile.find(FH_Table[folderType].folder_extension) != -1){
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
    ALOGE("this jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    queue<string> empty;
    swap(q, empty);
    ALOGE("this jni call-> Out func: %s, line:%d \n", __func__, __LINE__);
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
    ALOGE("this jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    int i = 0;
    char table_config_path[NORULE_SIZE];
    snprintf(table_config_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);
    FILE* fp = fopen(table_config_path, "rb");

    struct file_struct read_table;

    char ver;
    int retlen = fread(&ver, 1, 1, fp);
    if ( retlen == 1 )
    {
        ALOGE("this jni call-> retlen == 1 func: %s, line:%d \n", __func__, __LINE__);
        if ( ver == TABLE_VERSION )
        {
            for(i=0; i<TABLE_SIZE; i++) {
                retlen = fread(&read_table, sizeof(struct file_struct), 1, fp);
                if (retlen == 1)
                {
                    ALOGE("this jni call-> retlen == sizeof(file_struct) %ld == 1 func: %s, line:%d \n",
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
                ALOGE("this jni call-> retlen = %d func: %s, line:%d \n", retlen, __func__,
                      __LINE__);
                if (retlen == 1) {
                    ALOGE("this jni call-> read func: %s, line:%d \n", __func__, __LINE__);
                    fclose(fp);
                    return 0;
                }
            }
        }
    }

    ALOGE("this jni call-> Out func: %s, line:%d \n", __func__, __LINE__);
    fclose(fp);
    return -1;
}

int set_max_file_num(uint64_t sdcard_size){
    ALOGE("this jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    int sd_size = sdcard_size/GIGABYTE;
    if(sd_size == 0){
        return SDCARD_DETECT_SIZE_ERROR;
    }
    if(sd_size < 4){
        ALOGE("this jni call-> 4G func: %s, line:%d \n", __func__, __LINE__);
        FH_Table[e_Event].max_file_num = 10;
        FH_Table[e_Normal].max_file_num = 20;
        FH_Table[e_Picture].max_file_num = 30;
        return SUCCESS;
    }else
    if(sd_size < 8){
        ALOGE("this jni call-> 8G func: %s, line:%d \n", __func__, __LINE__);
        FH_Table[e_Event].max_file_num = 20;
        FH_Table[e_Normal].max_file_num = 40;
        FH_Table[e_Picture].max_file_num = 60;
        return SUCCESS;
    }else
    if(sd_size < 16){
        ALOGE("this jni call-> 16G func: %s, line:%d \n", __func__, __LINE__);
        FH_Table[e_Event].max_file_num = 40;
        FH_Table[e_Normal].max_file_num = 80;
        FH_Table[e_Picture].max_file_num = 120;
        return SUCCESS;
    }else
    if(sd_size < 32){
        ALOGE("this jni call-> 32G func: %s, line:%d \n", __func__, __LINE__);
        FH_Table[e_Event].max_file_num = 80;
        FH_Table[e_Normal].max_file_num = 160;
        FH_Table[e_Picture].max_file_num = 240;
        return SUCCESS;
    }else
    if(sd_size < 64){
        ALOGE("this jni call-> 64G func: %s, line:%d \n", __func__, __LINE__);
        FH_Table[e_Event].max_file_num = 160;
        FH_Table[e_Normal].max_file_num = 320;
        FH_Table[e_Picture].max_file_num = 480;
        return SUCCESS;
    }else
    if(sd_size < 128){
        ALOGE("this jni call-> 128G func: %s, line:%d \n", __func__, __LINE__);
        FH_Table[e_Event].max_file_num = 320;
        FH_Table[e_Normal].max_file_num = 640;
        FH_Table[e_Picture].max_file_num = 960;
        return SUCCESS;
    }
    ALOGE("this jni call-> Out func: %s, line:%d \n", __func__, __LINE__);
    return SDCARD_NOT_SUPPORT;
}

void check_queue_status(int old_date_flag, queue<string>& folder_files_queue){
    cout << "queue first = " << folder_files_queue.front() << endl;
    if(old_date_flag == 1){

        while(true){
            cout << " |Queue| queuefront = " << folder_files_queue.front() << endl;
            cout << " |Queue| queueback  = " << folder_files_queue.back() << endl;
            string queue_pop = folder_files_queue.front();
            if(atoi(queue_pop.substr(0,2).c_str()) >= 70){
                cout << "!! 1970 in front !!" << endl;
                break;
            }

            folder_files_queue.pop();
            folder_files_queue.push(queue_pop);
        }
        cout << " |Queue| rotak finish queuefront = " << folder_files_queue.front() << endl;
        cout << " |Queue| rotak finish queueback  = " << folder_files_queue.back() << endl;
    }

//     ALOGE("this is jni call1--> Q|| SHOW THE QUEUE ||Q \n");
//     while(!folder_files_queue.empty()){
//         string str_queue_top = folder_files_queue.front();
//         ALOGE("this is jni call1--> %s \n", str_queue_top.c_str());
//         folder_files_queue.pop();
//     }
}

int storage_normal_file_in_queue(eFolderType folderType, queue<string>& folder_files_queue){
    char file_path[NORULE_SIZE];
    snprintf(file_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

    vector<string> files = vector<string>();

    DIR *dp = opendir(file_path);
    struct dirent *dirp;

    int old_date_flag = 0;

    if (dp == NULL){
        cout << "Error opening " << endl;
        return 0;
    }

    while ((dirp = readdir(dp)) != NULL) {

        string inPath_filename = dirp->d_name;
        // cout << inPath_filename << endl;
        if(atoi(inPath_filename.substr(0,2).c_str()) >= 70){
            old_date_flag = 1;
        }

        if(inPath_filename.length() < FILE_MINI_LENGTH || inPath_filename.length() > FILE_MAX_LENGTH){
            continue;
        }
        if(atoi(inPath_filename.substr(2,2).c_str()) > MONTH_LIMIT){
            continue;
        }

        // if filename != Days format
        if(atoi(inPath_filename.substr(4,2).c_str()) > DAYS_LIMIT){
            continue;
        }

        // if filename != Hour format
        if(atoi(inPath_filename.substr(6,2).c_str()) > HOUR_LIMIT){
            continue;
        }

        // if filename != Minute format
        if(atoi(inPath_filename.substr(8,2).c_str()) > MINUTE_LIMIT){
            continue;
        }

        // if filename != second format
        if(atoi(inPath_filename.substr(10,2).c_str()) > SECOND_LIMIT){
            continue;
        }
        files.push_back(inPath_filename);
    }

    closedir(dp);

    if(files.empty() == true){
        return 0;
    }

    sort (files.begin(), files.end());

    ALOGE("this is jni call1--> vector sort finish, start print \n");

    for (vector<string>::const_iterator i = files.begin(); i != files.end(); ++i){
        cout << *i << ' ' << endl;
        string vector_str = *i;
        ALOGE("this is jni call1--> %s \n", vector_str.c_str());
        folder_files_queue.push(*i);
    }


    string first_file_name = files.front();

    cout << "first_filename = " << first_file_name << endl;

    check_queue_status(old_date_flag, folder_files_queue);

    return 0;
}

int checkTableVersion(char* mount_path){
    ALOGE("this jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    char config_file_path[NORULE_SIZE];
    snprintf(config_file_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);

    FILE* fp = NULL;
    int n = 0;

    fp=fopen (config_file_path,"rb");
    if(fp==NULL){
        ALOGE("this jni call-> Error opening file. func: %s, line:%d \n", __func__, __LINE__);
        return OPEN_FOLDER_ERROR;
    }
    n = fgetc (fp);
    if(n == 0x45){ // 0x45 == 'E'
        ALOGE("this jni call-> table format Failed. func: %s, line:%d \n", __func__, __LINE__);
        fclose(fp);
        return TABLE_VERSION_TOO_OLD;
    }
    if(n == TABLE_VERSION){
        ALOGE("this jni call-> n = %d. In func: %s, line:%d \n", n, __func__, __LINE__);
        fclose(fp);
        return SUCCESS;
    }

    ALOGE("this jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    fclose(fp);
    return TABLE_VERSION_CANNOT_RECOGNIZE;
}

// true = 1, false = 0;
bool FH_ValidFormat(char* mount_path){
    pthread_mutex_lock(&g_mutex);
    int rc;
    char config_file_path[NORULE_SIZE];
    snprintf(config_file_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);

    rc = SDA_file_exists(config_file_path);
    pthread_mutex_unlock(&g_mutex);
    return (rc == 0 ? true : false);
}

void queue_Release(void){
    ALOGE("this jni call-> In func: %s, line:%d \n", __func__, __LINE__);
    clear_queue(event_files_queue);
    clear_queue(normal_files_queue);
    clear_queue(picture_files_queue);
    clear_queue(hash_event_files_queue);
    clear_queue(hash_normal_files_queue);
    clear_queue(nmea_event_files_queue);
    clear_queue(nmea_normal_files_queue);
    return;
}

// true = 1, false = 0;
int FH_Init(char* mount_path){

    pthread_mutex_lock(&g_mutex);
    int i;
    int rc;

    if(mount_path == NULL){
        pthread_mutex_unlock(&g_mutex);
        return SDCARD_PATH_ERROR;
    }else{
        strncpy(g_mount_path, mount_path, strlen(mount_path));
    }

    /* If mount_path doesn't have "CONFIG", return please format sdcard */
    char config_file_path[NORULE_SIZE];
    snprintf(config_file_path, NORULE_SIZE, "%s/%s", mount_path, CFG_NAME);
    rc = SDA_file_exists(config_file_path);
    if(rc == 0){
        rc = checkTableVersion(mount_path);
        if(rc != SUCCESS){
            ALOGE("this jni call-> Sdcard format error. func: %s, line:%d \n", __func__, __LINE__);
            pthread_mutex_unlock(&g_mutex);
            return TABLE_VERSION_TOO_OLD;
        }
        int ret = SDA_get_structure_value_from_config(mount_path);
        if (ret != 0)
        {
            ALOGE("this jni call-> SDA_get_structure_value_from_config fail. func: %s, line:%d \n", __func__, __LINE__);
            pthread_mutex_unlock(&g_mutex);
            return TABLE_READ_ERROR;
        }
    }

    struct statvfs buf;

    if (statvfs(mount_path, &buf) == -1){
        ALOGE("Sdcard path error. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return SDCARD_PATH_ERROR;
    }
    ALOGE("Sdcard available space = %" PRIu64 ". func: %s, line:%d \n", ((uint64_t)buf.f_bavail * buf.f_bsize), __func__, __LINE__);

    uint64_t mount_path_block_size = ((uint64_t)buf.f_blocks * buf.f_bsize);
    uint64_t mount_path_avail_size = ((uint64_t)buf.f_bavail * buf.f_bsize);

    rc = set_max_file_num(mount_path_block_size);
    if(rc != SUCCESS){
        ALOGE("this jni call-> sdcard detect failed. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return SDCARD_DETECT_SIZE_ERROR;
    }

    /* Scan which folder not exist */
    rc = SDA_scan_sdcard_folder_exist(mount_path);
    if(rc == -1){
        cout << "Scan SDCARD failed." << endl;
        pthread_mutex_unlock(&g_mutex);
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
    ALOGE("this is jni call1-->. percent_add = %f. func: %s, line:%d \n", percent_add, __func__, __LINE__);

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

    /* Debug usage: print Structure data */
    // for(i=0; i<TABLE_SIZE; i++){
    // 	cout << FH_Table[i].folder_type << " "
    // 	<< FH_Table[i].folder_extension << " " << FH_Table[i].percent << " "
    // 	<< FH_Table[i].every_block_space << " " << FH_Table[i].avail_space << " "
    // 	<< FH_Table[i].max_file_num << " " << FH_Table[i].file_num << " "
    // 	<< FH_Table[i].exist_flag << endl;
    // }

    ALOGE("this is jni call1--> sizeof percent = %ld. func: %s, line:%d \n", sizeof(FH_Table[0].percent), __func__, __LINE__);

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

    for(i=0; i<TABLE_SIZE; i++){
        ALOGE("this is jni call1-->%s, %s, %f, %" PRId64", %" PRId64 ", %d, %d, %d, func: %s, line:%d \n",
              FH_Table[i].folder_type,
              FH_Table[i].folder_extension,
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

    storage_normal_file_in_queue(e_Event, event_files_queue);
    storage_normal_file_in_queue(e_Normal, normal_files_queue);
    storage_normal_file_in_queue(e_Picture, picture_files_queue);
    storage_normal_file_in_queue(e_HASH_EVENT, hash_event_files_queue);
    storage_normal_file_in_queue(e_HASH_NORMAL, hash_normal_files_queue);
    storage_normal_file_in_queue(e_NMEA_EVENT, nmea_event_files_queue);
    storage_normal_file_in_queue(e_NMEA_NORMAL, nmea_normal_files_queue);

    pthread_mutex_unlock(&g_mutex);
    return SUCCESS;
}

string open_file_and_save_in_queue(char* filename, eFolderType folderType, queue<string> &now_queue){

    char free_path[NORULE_SIZE];
    snprintf(free_path, NORULE_SIZE, "%s%s", g_mount_path,"/SYSTEM/FREE");

    char folder_path[NORULE_SIZE];
    char purpose_path[NORULE_SIZE];
    string first_filename;
    // ALOGE("this is jni call1-->FH_Open filename %s",filename);
    // ALOGE("this is jni call1-->FH_Open folderType %d",folderType);

    snprintf(folder_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

    // if free folder have .eve extension, rename .eve file to purpose filename
    first_filename = SDA_get_first_filename(free_path, FH_Table[folderType].folder_extension);
    if(first_filename.length() != 0){
        char first_path[NORULE_SIZE];
        snprintf(first_path, NORULE_SIZE, "%s/%s", free_path, first_filename.c_str());

        snprintf(purpose_path, NORULE_SIZE, "%s/%s", folder_path, filename);

        rename(first_path, purpose_path);

        now_queue.push(filename);
        ALOGE("this is jni call1-->. exist file in SYSTEM/FREE. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return string(purpose_path);

        // if no .eve extension in System/Free & folder file number < Event.file_num
    }else if(now_queue.size() < FH_Table[folderType].file_num){
        ALOGE("this is jni call1-->now_queue size = %d. func: %s, line:%d \n", now_queue.size(), __func__, __LINE__);
        ALOGE("this is jni call1-->FH_Table file_num = %d. func: %s, line:%d \n", FH_Table[folderType].file_num, __func__, __LINE__);
        snprintf(purpose_path, NORULE_SIZE, "%s/%s", folder_path, filename);

        now_queue.push(filename);
        ALOGE("this is jni call1-->. file number not arrive limit. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return string(purpose_path);
    }else{
//        cout << "file was full, please delete some file." << endl;

        ALOGE("this is jni call1-->FH_Open folderType = %d, now_queue size = %d. func: %s, line:%d \n", folderType, now_queue.size(), __func__, __LINE__);
        ALOGE("this is jni call1-->folderType = %d, FH_Table file_num = %d. func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
        ALOGE("this is jni call1-->FH_Open file was full, please delete some file. unc: %s, line:%d \n", __func__, __LINE__);

        pthread_mutex_unlock(&g_mutex);
        return "";
    }
}

string FH_Open(char* filename, eFolderType folderType){

    pthread_mutex_lock(&g_mutex);
    ALOGE("this jni call -> func: %s, line:%d \n", __func__, __LINE__);

    if(strlen(g_mount_path) == 0){
        pthread_mutex_unlock(&g_mutex);
        return "";
    }

    string open_filename;

    switch (folderType) {
        case e_Event:
            open_filename = open_file_and_save_in_queue(filename, folderType, event_files_queue);
             ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        case e_Normal:
            open_filename = open_file_and_save_in_queue(filename, folderType, normal_files_queue);
             ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        case e_Picture:
            open_filename = open_file_and_save_in_queue(filename, folderType, picture_files_queue);
            ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        case e_HASH_EVENT:
            open_filename = open_file_and_save_in_queue(filename, folderType, hash_event_files_queue);
            ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        case e_HASH_NORMAL:
            open_filename = open_file_and_save_in_queue(filename, folderType, hash_normal_files_queue);
            ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        case e_NMEA_EVENT:
            open_filename = open_file_and_save_in_queue(filename, folderType, nmea_event_files_queue);
            ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        case e_NMEA_NORMAL:
            open_filename = open_file_and_save_in_queue(filename, folderType, nmea_normal_files_queue);
            ALOGE("open_filename = %s. func: %s, line:%d \n", open_filename.c_str(), __func__, __LINE__);
            break;
        default:
             ALOGE("folderType = %d. func: %s, line:%d \n", folderType, __func__, __LINE__);
            break;
    }

    pthread_mutex_unlock(&g_mutex);
    return open_filename;
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
bool FH_Delete(const char* absolute_filepath){
    ALOGE("this jni call -> absolute_filepath = %s, func: %s, line:%d \n", absolute_filepath, __func__, __LINE__);
    pthread_mutex_lock(&g_mutex);

    int fd = open(absolute_filepath, O_RDWR);
    if(fd == -1){
        ALOGE("this jni call -> absolute_filepath not exist, func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
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
            // cout << "find FH_Table: " << FH_Table[i].folder_type << endl;
            last_filename = SDA_get_last_filename(free_path, FH_Table[i].folder_extension);
            int number_filename = atoi(last_filename.substr(0, last_filename.find(".")).c_str());
            char new_last_file[NORULE_SIZE];
            snprintf(new_last_file, sizeof(new_last_file), "%s/%d%s", free_path, number_filename+1, FH_Table[i].folder_extension);
            // cout << "new last_filename: " << new_last_file << endl;
            rc = rename(absolute_filepath, new_last_file);
            ALOGE("this jni call -> Out func. rename %s to new_last_file = %s, func: %s, line:%d \n", absolute_filepath, new_last_file, __func__, __LINE__);
            pthread_mutex_unlock(&g_mutex);
            return (rc == 0 ? true : false);
        }
    }

    // not find any about folderType
    ALOGE("this jni call -> return false. Out func: %s, line:%d \n", __func__, __LINE__);
    pthread_mutex_unlock(&g_mutex);
    return false;
}

string FH_FindOldest(eFolderType folderType){
    ALOGE("this jni call -> folderType: %d func: %s, line:%d \n", folderType, __func__, __LINE__);
    pthread_mutex_lock(&g_mutex);
    ALOGE("this jni call -> folderType: %d func: %s, line:%d \n", folderType, __func__, __LINE__);
    char finding_path[NORULE_SIZE];
    snprintf(finding_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);
    ALOGE("this jni call -> finding_path: %s func: %s, line:%d \n", finding_path, __func__, __LINE__);
    // cout << "findingpath: " << finding_path  << endl;
    int rc = SDA_file_exists(finding_path);
    if(rc != 0){
        ALOGE("this jni call -> folderType: %d func: %s, line:%d \n", folderType, __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return "";
    }

    string oldest_file;
    oldest_file.append(finding_path);
//    oldest_file.append("/");
//    oldest_file.append(FH_Table[folderType].folder_type);
    oldest_file.append("/");

    switch (folderType) {
        case e_Event:
            oldest_file.append(event_files_queue.front());
            event_files_queue.pop();
            break;
        case e_Normal:
            oldest_file.append(normal_files_queue.front());
            normal_files_queue.pop();
            break;
        case e_Picture:
            oldest_file.append(picture_files_queue.front());
            picture_files_queue.pop();
            break;
        case e_HASH_EVENT:
            oldest_file.append(hash_event_files_queue.front());
            hash_event_files_queue.pop();
            break;
        case e_HASH_NORMAL:
            oldest_file.append(hash_normal_files_queue.front());
            hash_normal_files_queue.pop();
            break;
        case e_NMEA_EVENT:
            oldest_file.append(nmea_event_files_queue.front());
            nmea_event_files_queue.pop();
            break;
        case e_NMEA_NORMAL:
            oldest_file.append(nmea_normal_files_queue.front());
            nmea_normal_files_queue.pop();
            break;
        default:
             ALOGE("folderType = %d. func: %s, line:%d \n", folderType, __func__, __LINE__);
            break;
    }

    ALOGE("this jni call -> folderType: %d func: %s, line:%d \n", folderType, __func__, __LINE__);
    pthread_mutex_unlock(&g_mutex);
    ALOGE("oldest_file = %s. func: %s, line:%d \n", oldest_file.c_str(), __func__, __LINE__);
    return oldest_file;
}

int FH_CheckFolderStatus(eFolderType folderType){
    pthread_mutex_lock(&g_mutex);
    ALOGE("this jni call -> folderType = %d func: %s, line:%d \n", folderType, __func__, __LINE__);

    struct statvfs buf;
    if (statvfs(g_mount_path, &buf) == -1) {
        cout << "Sdcard path error. g_mount_path: " << g_mount_path << " func: "<< __func__ << " line: " << __LINE__ << endl;
        pthread_mutex_unlock(&g_mutex);
        return SDCARD_PATH_ERROR;
    }
    printf("in bytes, that's %" PRIu64 " \n", ((uint64_t)buf.f_bavail * buf.f_bsize));

    uint64_t mount_path_avail_size = ((uint64_t)buf.f_bavail * buf.f_bsize);

    char folder_path[NORULE_SIZE];
    snprintf(folder_path, NORULE_SIZE, "%s/%s", g_mount_path, FH_Table[folderType].folder_type);

    // Calucate exist recode file number
    int recoder_file_already_exist_num = SDA_get_recoder_file_num(folder_path);
    int extension_number = SDA_get_free_extension_filenumber(folderType);
    int recoder_file_num = recoder_file_already_exist_num + extension_number;
    ALOGE("Existing record file and over limit number. recoder_file_num = %d. func: %s, line:%d \n", recoder_file_num, __func__, __LINE__);

    // file over limit
    if (recoder_file_num > FH_Table[folderType].file_num){
        ALOGE("Existing record file and over limit number. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
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
        ALOGE("Open folder error. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
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
        ALOGE("Out of folder space limit. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return FOLDER_SPACE_OVER_LIMIT;
    }

    uint64_t folder_avail_size = sdcard_avail_size - using_file_size;
    if (sdcard_avail_size < using_file_size) {
        folder_avail_size = 0;
    }

    int avail_record_num = recoder_file_num + (folder_avail_size / FH_Table[folderType].every_block_space);
    if (avail_record_num < 2) {
        ALOGE("sdcard no space, record file not enough to recycle. func: %s, line:%d \n", __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return NO_SPACE_NO_NUMBER_TO_RECYCLE;
    }
    if (avail_record_num < FH_Table[folderType].file_num) {
        FH_Table[folderType].file_num = avail_record_num;
    }

    ALOGE("this is jni call1-->%s, %s, %f, %" PRId64", %" PRId64 ", %d, %d, %d \n",FH_Table[folderType].folder_type, FH_Table[folderType].folder_extension, FH_Table[folderType].percent, FH_Table[folderType].every_block_space, FH_Table[folderType].avail_space, FH_Table[folderType].max_file_num, FH_Table[folderType].file_num, FH_Table[folderType].exist_flag);
    ALOGE("this is jni call -> folderType = %d, return file_num = %d. func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
    pthread_mutex_unlock(&g_mutex);
    return FH_Table[folderType].file_num;
}

int FH_GetSDCardInfo(eFolderType folderType, eGetNum getNumOpt){
    ALOGE("this jni call-> folderType = %d func: %s, line:%d \n", folderType, __func__, __LINE__);
    pthread_mutex_lock(&g_mutex);
    if (getNumOpt == e_getLimitNum){
        ALOGE("this jni call-> folderType = %d, limit_file_num = %d. func: %s, line:%d \n", folderType, FH_Table[folderType].file_num, __func__, __LINE__);
        pthread_mutex_unlock(&g_mutex);
        return FH_Table[folderType].file_num;
    }
    int current_num = -1;
    if (getNumOpt == e_getCurrentNum){
        switch(folderType){
            case e_Event:
                current_num = event_files_queue.size();
                break;
            case e_Normal:
                current_num = normal_files_queue.size();
                break;
            case e_Picture:
                current_num = picture_files_queue.size();
                break;
            default:
                ALOGE("this jni call-> folderType error. func: %s, line:%d \n", __func__, __LINE__);
                break;
        }
    }

    ALOGE("this jni call-> folderType = %d, current_num = %d func: %s, line:%d \n", folderType, current_num, __func__, __LINE__);
    pthread_mutex_unlock(&g_mutex);
    return current_num;
}

void FH_Sync(void){
    pthread_mutex_lock(&g_mutex);
    sync();
    pthread_mutex_unlock(&g_mutex);
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
