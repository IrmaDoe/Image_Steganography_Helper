# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH:= $(call my-dir)


# First, libjpeg.a
#
include $(CLEAR_VARS)

LOCAL_MODULE    := jpeg
LOCAL_SRC_FILES := jpeg/jaricom.c jpeg/jcapimin.c jpeg/jcapistd.c jpeg/jcarith.c jpeg/jccoefct.c jpeg/jccolor.c \
        jpeg/jcdctmgr.c jpeg/jchuff.c jpeg/jcinit.c jpeg/jcmainct.c jpeg/jcmarker.c jpeg/jcmaster.c \
        jpeg/jcomapi.c jpeg/jcparam.c jpeg/jcprepct.c jpeg/jcsample.c jpeg/jctrans.c jpeg/jdapimin.c \
        jpeg/jdapistd.c jpeg/jdarith.c jpeg/jdatadst.c jpeg/jdatasrc.c jpeg/jdcoefct.c jpeg/jdcolor.c \
        jpeg/jddctmgr.c jpeg/jdhuff.c jpeg/jdinput.c jpeg/jdmainct.c jpeg/jdmarker.c jpeg/jdmaster.c \
        jpeg/jdmerge.c jpeg/jdpostct.c jpeg/jdsample.c jpeg/jdtrans.c jpeg/jerror.c jpeg/jfdctflt.c \
        jpeg/jfdctfst.c jpeg/jfdctint.c jpeg/jidctflt.c jpeg/jidctfst.c jpeg/jidctint.c jpeg/jquant1.c \
        jpeg/jquant2.c jpeg/jutils.c jpeg/jmemmgr.c jpeg/jmemnobs.c

include $(BUILD_STATIC_LIBRARY)


# Second, libjsfc4ish.so , which will depend on and include the libjpeg.a
#
include $(CLEAR_VARS)

LOCAL_MODULE    := jsfc4ish
LOCAL_SRC_FILES := jsfc4ish.c jpeg_steg_extract.c 

LOCAL_STATIC_LIBRARIES := jpeg

LOCAL_C_INCLUDES += $(LOCAL_PATH)/jpeg
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
