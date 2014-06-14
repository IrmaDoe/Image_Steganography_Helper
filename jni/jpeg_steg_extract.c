#include <stdlib.h>
#include <stdio.h>
#include <jni.h>
#include <android/log.h>
#include "jsfc4ish.h"

/*
 * Class:     edu_hit_ict_ish_MainActivity
 * Method:    analyzeJpegStegInfo
 * Signature: (Ljava/lang/String;Ledu/hit/ict/ish/JpegStegInfo;)Ledu/hit/ict/ish/JpegStegInfo;
 */
JNIEXPORT jobject JNICALL Java_edu_hit_ict_ish_MainActivity_analyzeJpegStegInfo(JNIEnv * env, jobject thiz, jstring inputFile, jobject info) {
	char input_name[MAX_PATH_LENGTH];

	const jbyte *path1;
	path1 = (*env)->GetStringUTFChars(env, inputFile, NULL);
	sprintf(input_name, "%s\0", path1);
	(*env)->ReleaseStringUTFChars(env, inputFile, path1);

    jpeg_init();
    jpeg_read(input_name);
	analyze_steg_info();
	jpeg_r_finish();

	jclass m_cls = (*env)->FindClass(env, "edu/hit/ict/ish/JpegStegInfo");

	jfieldID m_fid_0 = (*env)->GetFieldID(env, m_cls, "stegStat", "I");
	jfieldID m_fid_1 = (*env)->GetFieldID(env, m_cls, "capacity", "J");
	jfieldID m_fid_2 = (*env)->GetFieldID(env, m_cls, "secretSize", "J");
	jfieldID m_fid_3 = (*env)->GetFieldID(env, m_cls, "usageRate", "F");

	(*env)->SetIntField(env, info, m_fid_0, steg_info.steg_stat);
	(*env)->SetLongField(env, info, m_fid_1, steg_info.capacity);
	(*env)->SetLongField(env, info, m_fid_2, steg_info.secret_size);
	(*env)->SetFloatField(env, info, m_fid_3, steg_info.usage_rate);

	return info;
}


/*
 * Class:     edu_hit_ict_ish_MainActivity
 * Method:    stegJpegMessage
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_edu_hit_ict_ish_MainActivity_stegJpegMessage(JNIEnv * env, jobject thiz, jstring inputFile, jstring secretMessage, jstring outputFile) {
	char input_name[MAX_PATH_LENGTH], output_name[MAX_PATH_LENGTH];
	char secret_message[MAX_SECRET_MESSAGE_LENGTH];

	const jbyte *path1, *path2;
	path1 = (*env)->GetStringUTFChars(env, inputFile, NULL);
	path2 = (*env)->GetStringUTFChars(env, outputFile, NULL);
	sprintf(input_name, "%s\0", path1);
	sprintf(output_name, "%s\0", path2);
	(*env)->ReleaseStringUTFChars(env, inputFile, path1);
	(*env)->ReleaseStringUTFChars(env, outputFile, path2);

	const jbyte *message;
	message = (*env)->GetStringUTFChars(env, secretMessage, NULL);
	sprintf(secret_message, "%s\0", message);
	(*env)->ReleaseStringUTFChars(env, secretMessage, message);

	// start steg
	int result_code = 0;

    jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();

    if(steg_info.steg_stat == 0) {
	    result_code = steg_message(secret_message);

	    if(result_code == 0) {
	    	jpeg_write(output_name);
	    	jpeg_rw_finish();
	    } else {
	    	jpeg_r_finish();
	    }

	} else {
		jpeg_r_finish();
		result_code = -2;
	}

    return result_code;
}

/*
 * Class:     edu_hit_ict_ish_MainActivity
 * Method:    extractJpegMessage
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_hit_ict_ish_MainActivity_extractJpegMessage(JNIEnv * env, jobject thiz, jstring inputFile) {
	char input_name[MAX_PATH_LENGTH];
	char extracted_message[MAX_SECRET_MESSAGE_LENGTH];

	const jbyte *path1;
	path1 = (*env)->GetStringUTFChars(env, inputFile, NULL);
	sprintf(input_name, "%s\0", path1);
	(*env)->ReleaseStringUTFChars(env, inputFile, path1);

	// start extract
	jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();

    if(steg_info.steg_stat == 1) {
 	   extract_message(extracted_message);
	}
    
    jpeg_r_finish();
    
	return (*env)->NewStringUTF(env, extracted_message);
}


/*
 * Class:     edu_hit_ict_ish_MainActivity
 * Method:    stegJpegFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_edu_hit_ict_ish_MainActivity_stegJpegFile(JNIEnv * env, jobject thiz, jstring inputFile, jstring secretFile, jstring outputFile) {
	char input_name[MAX_PATH_LENGTH], output_name[MAX_PATH_LENGTH];
	char secret_file[MAX_PATH_LENGTH];

	const jbyte *path1, *path2;
	path1 = (*env)->GetStringUTFChars(env, inputFile, NULL);
	path2 = (*env)->GetStringUTFChars(env, outputFile, NULL);
	sprintf(input_name, "%s\0", path1);
	sprintf(output_name, "%s\0", path2);
	(*env)->ReleaseStringUTFChars(env, inputFile, path1);
	(*env)->ReleaseStringUTFChars(env, outputFile, path2);

	const jbyte *file_name;
	file_name = (*env)->GetStringUTFChars(env, secretFile, NULL);
	sprintf(secret_file, "%s\0", file_name);
	(*env)->ReleaseStringUTFChars(env, secretFile, file_name);

	// start steg
	int result_code = 0;

	jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();

    if(steg_info.steg_stat == 0) {
    	result_code = steg_file(secret_file);

    	if(result_code == 0) {
    		jpeg_write(output_name);
    		jpeg_rw_finish();
    	} else {
    		jpeg_r_finish();
    	}

	} else {
		jpeg_r_finish();
		result_code = -2;
	}

    return result_code;
}


/*
 * Class:     edu_hit_ict_ish_MainActivity
 * Method:    extractJpegFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_edu_hit_ict_ish_MainActivity_extractJpegFile(JNIEnv * env, jobject thiz, jstring inputFile, jstring savePath) {
	char input_name[MAX_PATH_LENGTH];
	char save_path[MAX_PATH_LENGTH];

	char secret_filename[MAX_PATH_LENGTH];
	memset(secret_filename, 0, MAX_PATH_LENGTH);

	const jbyte *path1, *path2;
	path1 = (*env)->GetStringUTFChars(env, inputFile, NULL);
	path2 = (*env)->GetStringUTFChars(env, savePath, NULL);
	sprintf(input_name, "%s\0", path1);
	sprintf(save_path, "%s\0", path2);
	(*env)->ReleaseStringUTFChars(env, inputFile, path1);
	(*env)->ReleaseStringUTFChars(env, savePath, path2);

	// start extract
	jpeg_init();
    root_node = jpeg_read(input_name);
    analyze_steg_info();

    if(steg_info.steg_stat == 2) {
    	extract_file(save_path, secret_filename);
    }

    jpeg_r_finish();
    
	return (*env)->NewStringUTF(env, secret_filename);
}

