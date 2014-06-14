#include "jsfc4ish.h"

void print_steg_info() {
    putchar('\n');
    printf("steg_stat: %d\n", steg_info.steg_stat);
    printf("capacity: %lld\n", steg_info.capacity);
    printf("secret_size: %lld\n", steg_info.secret_size);
    printf("usage_rate: %f\n", steg_info.usage_rate);
    putchar('\n');
}

void test_max_min_capacity(const char * input_name) {
    jpeg_init();
    root_node = jpeg_read(input_name);
    
    long long max_capacity = 0, min_capacity = 0;
    
    dct_node *tmp_node = root_node;
    while (tmp_node != NULL) {
        short coef = tmp_node->coeff_struct.coefficient;

        max_capacity++;
        if(coef != 1 && coef != (-1) )min_capacity++;

        tmp_node = tmp_node->next;
    }
    
    jpeg_r_finish();
    
    printf("%f\n", min_capacity * 1.0 / max_capacity);
}

void print_linked_list(dct_node *root_node) {
    fprintf(out, "\n");
    fprintf(out, "dct_list_size: %d\n", dct_list_size);
    fprintf(out, "dct_list: \n");
    
    dct_node *tmp_node = root_node;
    while (tmp_node != NULL) {
        fprintf(out, "%i ", tmp_node->coeff_struct.coefficient);
        tmp_node = tmp_node->next;
    }
    fprintf(out, "\n");
}

void jpeg_init() {
    input_file = NULL;
    output_file = NULL;

    /* Initialize the JPEG compression and decompression objects with default error handling. */
    inputinfo.err = jpeg_std_error(&jerr);
    jpeg_create_decompress(&inputinfo);
    outputinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&outputinfo);
}


void jpeg_r_finish() {
    /* Finish compression and release memory */
    jpeg_finish_decompress(&inputinfo);
    jpeg_destroy_decompress(&inputinfo);

    /* Close files */
    if(input_file != NULL)fclose(input_file);
    if(output_file != NULL)fclose(output_file);
}


void jpeg_rw_finish() {
    /* Finish compression and release memory */
    jpeg_finish_compress(&outputinfo);
    jpeg_destroy_compress(&outputinfo);
    jpeg_finish_decompress(&inputinfo);
    jpeg_destroy_decompress(&inputinfo);

    /* Close files */
    if(input_file != NULL)fclose(input_file);
    if(output_file != NULL)fclose(output_file);
}


dct_node * jpeg_read(const char * input_name) {
    unsigned int i;
    dct_node * tmp_node = NULL;
    
    dct_list_size = 0;
    current_node = NULL;
    root_node = NULL;

    //a temp dct_coefficient struct
    dct_coefficient temp_coefficient;
    
    /* Open the input and output files */
    if ((input_file = fopen(input_name, READ_BINARY)) == NULL) {
        fprintf(stderr, "Can't open %s\n", input_name);
        exit(EXIT_FAILURE);
    }
    
    /* Specify data source for decompression and recompression */
    jpeg_stdio_src(&inputinfo, input_file);
    
    /* Read file header */
    (void) jpeg_read_header(&inputinfo, TRUE);
    
    /* Allocate memory for reading out DCT coeffs */
    for (compnum = 0; compnum < inputinfo.num_components; compnum++)
        coef_buffers[compnum] = ((&inputinfo)->mem->alloc_barray)
        ((j_common_ptr) & inputinfo, JPOOL_IMAGE,
            inputinfo.comp_info[compnum].width_in_blocks,
            inputinfo.comp_info[compnum].height_in_blocks);

    /* Read input file as DCT coeffs */
    coef_arrays = jpeg_read_coefficients(&inputinfo);
    
    /* Copy compression parameters from the input file to the output file */
    jpeg_copy_critical_parameters(&inputinfo, &outputinfo);

    /* Copy DCT coeffs to a new array */
    num_components = inputinfo.num_components;
    for (compnum = 0; compnum < num_components; compnum++) {
        height_in_blocks[compnum] = inputinfo.comp_info[compnum].height_in_blocks;
        width_in_blocks[compnum] = inputinfo.comp_info[compnum].width_in_blocks;
        block_row_size[compnum] = (size_t) SIZEOF(JCOEF) * DCTSIZE2 * width_in_blocks[compnum];
        for (rownum = 0; rownum < height_in_blocks[compnum]; rownum++) {
            row_ptrs[compnum] = ((&inputinfo)->mem->access_virt_barray)
                    ((j_common_ptr) & inputinfo, coef_arrays[compnum],
                    rownum, (JDIMENSION) 1, FALSE);
            for (blocknum = 0; blocknum < width_in_blocks[compnum]; blocknum++) {
                for (i = 0; i < DCTSIZE2; i++) {
                    coef_buffers[compnum][rownum][blocknum][i] = row_ptrs[compnum][0][blocknum][i];
                }
            }
        }
    }
    
    //Print out or modify DCT coefficients 
    //We only use (compnum==0) the first component !
    compnum = 0;
    for (rownum = 0; rownum < height_in_blocks[compnum]; rownum++) {
        for (blocknum = 0; blocknum < width_in_blocks[compnum]; blocknum++) {
            for (i = 0; i < DCTSIZE2; i++) {
                int this_iterations_coefficient = coef_buffers[compnum][rownum][blocknum][i];
                
                if (this_iterations_coefficient != 0) {
                    temp_coefficient.component_index = compnum;
                    temp_coefficient.row_index = rownum;
                    temp_coefficient.column_index = blocknum;
                    temp_coefficient.index = i;
                    temp_coefficient.coefficient = this_iterations_coefficient;

                    //append to dct_list at the tail
                    dct_node *new_node = malloc(sizeof (dct_node));
                    new_node->coeff_struct = temp_coefficient;
                    new_node->next = NULL;
                    new_node->prev = tmp_node;
                    
                    if(root_node == NULL)root_node = new_node;
                    
                    if(tmp_node != NULL)tmp_node->next = new_node;
                    tmp_node = new_node;


                    dct_list_size++;
                }
            }
        }
    }

    current_node = root_node;
    return root_node;
}

int jpeg_write(const char * output_name) {
    dct_node * tmp_node = NULL;
    dct_node * next_node;
    
    if ((output_file = fopen(output_name, WRITE_BINARY)) == NULL) {
        fprintf(stderr, "Can't open %s\n", output_name);
        exit(EXIT_FAILURE);
    }
    
    /* Specify data source for decompression and recompression */
    jpeg_stdio_dest(&outputinfo, output_file);
    
    
    tmp_node = root_node;
    while(tmp_node != NULL) {
        coef_buffers[tmp_node->coeff_struct.component_index][tmp_node->coeff_struct.row_index]
                [tmp_node->coeff_struct.column_index][tmp_node->coeff_struct.index] = tmp_node->coeff_struct.coefficient;

        next_node = tmp_node->next;
        free(tmp_node);
        tmp_node = next_node;
    }
    
    /* Output the new DCT coeffs to a JPEG file */
    for (compnum = 0; compnum < num_components; compnum++) {
        for (rownum = 0; rownum < height_in_blocks[compnum]; rownum++) {
            row_ptrs[compnum] = ((&outputinfo)->mem->access_virt_barray)
                    ((j_common_ptr) & outputinfo, coef_arrays[compnum],
                    rownum, (JDIMENSION) 1, TRUE);
            memcpy(row_ptrs[compnum][0][0],
                    coef_buffers[compnum][rownum][0],
                    block_row_size[compnum]);
        }
    }

    /* Write to the output file */
    jpeg_write_coefficients(&outputinfo, coef_arrays);

    return 0;
}

int steg_f4_method(const char *secret_message, unsigned int message_length) {
    int t, i;
    
    short dct_coef_num;
    char dct_coef_0or1;
    
    char secret_message_bit;
    
    dct_node * tmp_node = current_node;
    
    for(t = 0; t < message_length; ++t) {
        for(i = 7; i >= 0; ) {
            
            while( (tmp_node->coeff_struct.coefficient) == 0) {
                tmp_node = tmp_node->next;
            }
            
            secret_message_bit = (secret_message[t] >> i) & 1;
            
            dct_coef_num = tmp_node->coeff_struct.coefficient;
            if(dct_coef_num > 0) {
                if((dct_coef_num % 2) == 0 ) dct_coef_0or1 = 0; //正偶
                else dct_coef_0or1 = 1; //正奇
            } else {
                if((dct_coef_num % 2) == 0 ) dct_coef_0or1 = 1; //负偶
                else dct_coef_0or1 = 0; //负奇
            }
            
            if(secret_message_bit != dct_coef_0or1) {
                (dct_coef_num > 0) ? dct_coef_num--: dct_coef_num++;
            }
            
            if(dct_coef_num != 0) --i;
            
            tmp_node->coeff_struct.coefficient = dct_coef_num;
            tmp_node = tmp_node->next;
        }
    }
    
    current_node = tmp_node;
    
}

char * extract_f4_method(char *extracted_message, unsigned int message_length) {
    int t, i;
    
    short dct_coef_num;
    char dct_coef_0or1;
    
    dct_node * tmp_node = current_node;
    
    memset(extracted_message, 0, message_length);
    
    for(t = 0; t < message_length; ++t) {
        for(i = 7; i >= 0; --i) {
            
            while( (tmp_node->coeff_struct.coefficient) == 0) {
                tmp_node = tmp_node->next;
            }
            
            dct_coef_num = tmp_node->coeff_struct.coefficient;
            if(dct_coef_num > 0) {
                if((dct_coef_num % 2) == 0 ) dct_coef_0or1 = 0; //正偶
                else dct_coef_0or1 = 1; //正奇
            } else {
                if((dct_coef_num % 2) == 0 ) dct_coef_0or1 = 1; //负偶
                else dct_coef_0or1 = 0; //负奇
            }
            
            if(dct_coef_0or1) extracted_message[t] |= ( (dct_coef_0or1 & 1) << i);
            
            tmp_node = tmp_node->next;
        }
    }
    
    current_node = tmp_node;
}

int steg_message(const char *secret_message) {
    unsigned int message_length;
    message_length = (unsigned int) strlen(secret_message);


    long long secret_size = 0;
    secret_size += 2 + 4 + message_length;
    if(secret_size < (steg_info.capacity * Q)) {
        //Step0 "JM" !
        steg_f4_method("JM", 2);

        //Step1 message_length!
        steg_f4_method(&message_length, 4);
        
        //Step2 secret_message!
        steg_f4_method(secret_message, message_length);

    } else {
        return -1;
    }
    
    return 0;
}

char *extract_message(char *extracted_message) {
    unsigned int message_length;
    char flag_JM[2];

    //Step0 "JM" !
    extract_f4_method(flag_JM, 2);

    //Step1 message_length!
    extract_f4_method(&message_length, 4);

    //Step2 secret_message!
    memset(extracted_message, 0, message_length + 1);
    extract_f4_method(extracted_message, message_length);
    
    return extracted_message;
}


int steg_jpeg_message(const char * input_name, const char *secret_message, const char * output_name) {
    if ((out = fopen("/sdcard/ImageStegLibrary/debug.log", "w")) == NULL) {
        fprintf(stderr, "Cannot open debug.log file.\n");
        return -1;
    }

    jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();

    steg_message(secret_message);

    jpeg_write(output_name);
    jpeg_rw_finish();
    
    if(out != NULL)fclose(out);
    return 1;
}


char *extract_jpeg_message(const char * input_name, char *extracted_message) {
    jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();
    extract_message(extracted_message);
    jpeg_r_finish();
    
    return extracted_message;
}


unsigned long get_file_size(const char *filename){
    unsigned long size;
    FILE* fp = fopen( filename, "rb" );
    if(fp==NULL){
        printf("ERROR: Open file %s failed.\n", filename);
        return 0;
    }
    fseek( fp, 0L, SEEK_END );
    size=ftell(fp);
    fclose(fp);
    return size;
}

int steg_file(const char *secret_file) {
    int i;

    //Open the secret_file
    FILE *fp;
    if ((fp = fopen(secret_file, "rb")) == NULL) {
        fprintf(stderr, "Cannot open %s .\n", secret_file);
        return -3;
    }

    //get filename_length, filename
    char filename[256];
    unsigned short filename_length = 0;

    unsigned int secret_filepath_length = strlen(secret_file);
    int index;
    for(index = secret_filepath_length - 1; index >= 0; --index) {
        if(secret_file[index] != file_separator) filename_length++;
        else break;
    }
    index++;
    for(i = 0; (index < secret_filepath_length) && (i < filename_length); ++index, ++i) {
        filename[i] = secret_file[index];
    }
    filename[filename_length] = '\0';

    //get file_size
    unsigned int file_size = 0;
    file_size = get_file_size(secret_file);


    long long secret_size = 0;
    secret_size += 2 + 2 + filename_length + 4 + file_size;
    if(secret_size < (steg_info.capacity * Q)) {
        //Step0 "JF" !
        steg_f4_method("JF", 2);

        //Step1 filename_length 2*8bit
        steg_f4_method(&filename_length, 2);
        
        //Step2 filename
        steg_f4_method(filename, filename_length);

        //Step3 file_size 4*8bit
        steg_f4_method(&file_size, 4);
        
        //Step4 file_data
        char file_data[MAX_SECRET_FILE_SIZE];
        fread(file_data, 1, file_size, fp);
        steg_f4_method(file_data, file_size);

    } else {
        return -1;
    }
    
    fclose(fp);
    return 0;
}

int extract_file(const char *save_path, char *secret_filename) {
    //Step0 "JF" !
    char flag_JF[2];
    extract_f4_method(flag_JF, 2);

    //Step1 filename_length 8bit
    //Step2 filename
    char filename[256];
    unsigned short filename_length = 0;
    extract_f4_method(&filename_length, 2);
    extract_f4_method(filename, filename_length);
    filename[filename_length] = '\0';

    strcpy(secret_filename, filename);

    //Open secret_file
    char secret_file[MAX_PATH_LENGTH];
    strcpy(secret_file, save_path);
    char tmp[2];
    tmp[0] = file_separator;
    tmp[1] = '\0';
    strcat(secret_file, tmp);
    strcat(secret_file, filename);

    FILE *fp;
    if ((fp = fopen(secret_file, "wb")) == NULL) {
        fprintf(stderr, "Cannot open %s .\n", secret_file);
        return -3;
    }

    //Step3 file_size 4*8bit
    //Step4 file_data
    unsigned int file_size = 0;
    char file_data[MAX_SECRET_FILE_SIZE];
    extract_f4_method(&file_size, 4);
    extract_f4_method(file_data, file_size);

    //Write to secret_file
    fwrite(file_data, 1, file_size, fp);

    fclose(fp);

    return 0;
}


int steg_jpeg_file(const char * input_name, const char *secret_file, const char * output_name) {
    jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();

    steg_file(secret_file);

    jpeg_write(output_name);
    jpeg_rw_finish();

    return 0;
}

int extract_jpeg_file(const char * input_name, const char *save_path, char *secret_filename) {
    jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();
    extract_file(save_path, secret_filename);
    jpeg_r_finish();
    
    return 0;
}


/*
struct jpeg_steg_info {
    int steg_stat;//0:NO, 1:MESSAGE, 2:FILE
    long long capacity;//NO, MESSAGE, FILE
    long long secret_size;//MESSAGE, FILE
    float usage_rate;//MESSAGE, FILE
} steg_info;
*/
int analyze_steg_info() {
    /* capacity */
    long long capacity = dct_list_size / 8;
    steg_info.capacity = capacity;

    /* steg_stat */
    int steg_stat;
    char flag[2];
    extract_f4_method(flag, 2);
    if(flag[0] == 'J' && flag[1] == 'M') {
        steg_stat = 1;
    } else if(flag[0] == 'J' && flag[1] == 'F') {
        steg_stat = 2;
    } else {
        steg_stat = 0;
    }
    steg_info.steg_stat = steg_stat;

    /* secret_size */
    long long secret_size = 0;
    if(steg_stat == 0) {
        secret_size = 0;
    } else if(steg_stat == 1) {
        unsigned int message_length;
        extract_f4_method(&message_length, 4);
        secret_size += 2 + 4 + message_length;
    } else if(steg_stat == 2) {
        char filename[256];
        unsigned short filename_length = 0;
        unsigned int file_size = 0;
        extract_f4_method(&filename_length, 2);
        extract_f4_method(filename, filename_length);
        extract_f4_method(&file_size, 4);
        secret_size += 2 + 2 + filename_length + 4 + file_size;
    }
    steg_info.secret_size = secret_size;

    /* usage_rate */
    float usage_rate = secret_size * 1.0 / capacity;
    steg_info.usage_rate = usage_rate;


    current_node = root_node;    

    return 0;
}

