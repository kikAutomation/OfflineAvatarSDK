/* Copyright (C) Itseez3D, Inc. - All Rights Reserved
* You may not use this file except in compliance with an authorized license
* Unauthorized copying of this file, via any medium is strictly prohibited
* Proprietary and confidential
* UNLESS REQUIRED BY APPLICABLE LAW OR AGREED BY ITSEEZ3D, INC. IN WRITING, SOFTWARE DISTRIBUTED UNDER THE LICENSE IS DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR
* CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED
* See the License for the specific language governing permissions and limitations under the License.
* Written by Itseez3D, Inc. <support@itseez3D.com>, September 2018
*/

#include <jni.h>
#include <string>
#include <android/log.h>
#include "avatar_sdk.hpp"

void* resourcesHandler = NULL;

JNIEnv* jniEnv;
jobject activityObj;
jmethodID updateProgressMethod;

void updateProgress(float progress)
{
    if (jniEnv && updateProgressMethod && activityObj)
        jniEnv->CallVoidMethod(activityObj, updateProgressMethod, progress);
}


extern "C"
JNIEXPORT int JNICALL
Java_itseez3d_avatar_sdk_com_MainActivity_initializeAvatarSdk(JNIEnv *env, jobject thiz, jstring resourcesPath) {
    const char* resources = env->GetStringUTFChars(resourcesPath, NULL);

    int code = initAvatarSdk("AvatarSDKSample");
    if (code == 0)
    {
        resourcesHandler = initializeResources(resources);
        if (resourcesHandler == NULL)
            return -1;
    }
    return code;
}

extern "C"
JNIEXPORT void JNICALL
Java_itseez3d_avatar_sdk_com_MainActivity_deinitializeAvatarSdk(JNIEnv *env, jobject) {
    if (resourcesHandler)
    {
        deinitializeResources(resourcesHandler);
        resourcesHandler = NULL;
    }
}

extern "C"
JNIEXPORT int JNICALL
Java_itseez3d_avatar_sdk_com_MainActivity_generateAvatar(JNIEnv *env, jobject thiz, jstring photoPath) {
    const char* photoPathStr = env->GetStringUTFChars(photoPath, NULL);

    jclass activityClass = env->GetObjectClass(thiz);
    updateProgressMethod = env->GetMethodID(activityClass, "updateProgress", "(F)V");
    jniEnv = env;
    activityObj = thiz;

    char jsonBuffer[4096];
    int code = getBaseResourcesJson(jsonBuffer, 4096);
    if (code != 0)
        return code;

    code = generateAvatarFromPhotoWithResources(resourcesHandler, photoPathStr, "//sdcard/itseez3d/model", jsonBuffer, updateProgress);
    return code;
}

