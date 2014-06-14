/* 
 * File:   jsfc4ish.h
 * Author: jasonworg
 *
 * Created on 2014年5月13日, 下午4:18
 */

#ifndef JSFC4ISH_H
#define	JSFC4ISH_H

#ifdef	__cplusplus
extern "C" {
#endif

#include <stdio.h>
#include <string.h>
#include "cdjpeg.h"   /* Common decls for compressing and decompressing jpegs */

#define MAX_SECRET_MESSAGE_LENGTH 100*1024 /*100 KB */
#define MAX_SECRET_FILE_SIZE 2*1024*1024 /*2 MB */

#define MAX_PATH_LENGTH 512

#define Q 0.5

#define file_separator '/'


typedef struct dct_coefficient dct_coefficient;
//struct containing DCT coefficient and metadata
struct dct_coefficient {
    //metadata
    short component_index;
    short row_index;
    short column_index;
    short index;
    
    //DCT coefficient
    short coefficient;
};

typedef struct dct_node dct_node;
//linked list node
struct dct_node {
    dct_coefficient coeff_struct;
    dct_node *next;
    dct_node *prev;
};


//some global vars for Debug !
FILE *out;

//some functions for debug
void print_linked_list(dct_node *root_node);
void test_max_min_capacity(const char * input_name);
void print_steg_info();

int steg_jpeg_message(const char * input_name, const char *secret_message, const char * output_name);
char *extract_jpeg_message(const char * input_name, char *extracted_message);

int steg_jpeg_file(const char * input_name, const char *secret_file, const char * output_name);
int extract_jpeg_file(const char * input_name, const char *save_path, char *secret_filename);



//some global vars for read/write jpeg file
struct jpeg_decompress_struct inputinfo;
struct jpeg_compress_struct outputinfo;
struct jpeg_error_mgr jerr;
jvirt_barray_ptr *coef_arrays;
JDIMENSION i, compnum, rownum, blocknum;
JBLOCKARRAY coef_buffers[MAX_COMPONENTS];
JBLOCKARRAY row_ptrs[MAX_COMPONENTS];
FILE * input_file;
FILE * output_file;
int num_components;
size_t block_row_size[MAX_COMPONENTS];
int width_in_blocks[MAX_COMPONENTS];
int height_in_blocks[MAX_COMPONENTS];

void jpeg_init();

void jpeg_r_finish();
void jpeg_rw_finish();

dct_node * jpeg_read(const char * input_name);//root_node
int jpeg_write(const char * output_name);//root_node


//some global vars for steg/extract
long long dct_list_size;
dct_node * root_node;
dct_node * current_node;


int steg_f4_method(const char *secret_message, unsigned int message_length);
char * extract_f4_method(char *extracted_message, unsigned int message_length);

// analyze jpeg_steg_info
struct jpeg_steg_info {
    int steg_stat;//0:NO, 1:MESSAGE, 2:FILE
    long long capacity;//NO, MESSAGE, FILE
    long long secret_size;//MESSAGE, FILE
    float usage_rate;//MESSAGE, FILE
} steg_info;

int analyze_steg_info();

int steg_message(const char *secret_message);
char *extract_message(char *extracted_message);

int steg_file(const char *secret_file);
int extract_file(const char *save_path, char *secret_filename);



#ifdef	__cplusplus
}
#endif

#endif	/* JSFC4ISH_H */

