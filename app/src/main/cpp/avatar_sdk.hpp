/* Copyright (C) Itseez3D, Inc. - All Rights Reserved
* You may not use this file except in compliance with an authorized license
* Unauthorized copying of this file, via any medium is strictly prohibited
* Proprietary and confidential
* UNLESS REQUIRED BY APPLICABLE LAW OR AGREED BY ITSEEZ3D, INC. IN WRITING, SOFTWARE DISTRIBUTED UNDER THE LICENSE IS DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR
* CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED
* See the License for the specific language governing permissions and limitations under the License.
* Written by Itseez3D, Inc. <support@itseez3D.com>, September 2018
*/

/*! \file avatar_sdk.hpp
This file contains public methods of the itSeez3D Offline Avatar SDK.
*/

#pragma once

#include <vector>
#include <string>

#if WIN32
#define DllExport __declspec(dllexport)
#else
#define DllExport
#endif

/**
*  The handler of the calculation progress.
*  @param[in] float The calculation progress in percents.
*/
typedef void(*ReportProgress)(float);

/**
*  Initializes the Avatar SDK. Should be called once before any other methods.
*  @param[in] programName Name of your application.
*  @return Zero in the successful execution.
*/
extern "C" DllExport int initAvatarSdk(const char* programName);

/**
*  Initializes resources required for the avatar generation. Should be called before the first avatar generation.
*  @param[in] rootPath Path to the directory where binary resources are located.
*  @return Pointer to the internal resources handler 
*/
extern "C" DllExport void* initializeResources(const char* rootPath);

/**
*  Deinitializes resources.
*  @param[in] resourcesHandler Pointer to the internal resources handler.
*/
extern "C" DllExport void deinitializeResources(void* resourcesHandler);

/**
*  Generates an avatar and stores it in the memory till the next avatar is generated
*  @param[in] resourcesHandler Pointer to the internal resources handler.
*  @param[in] imagePath Path to the photo in the PNG or JPEG format.
*  @param[in] reportProgress Callback that is invoked when the calculation progress is changed.
*  @return Zero in the successful execution.
*/
extern "C" DllExport int generateAvatarFromPhoto(void* resourcesHandler, const char* imagePath, ReportProgress reportProgress);

/**
*  Generates an avatar with all available resources (blendshapes and haircuts)
*  @param[in] resourcesHandler Pointer to the internal resources handler.
*  @param[in] imagePath Path to the photo in the PNG or JPEG format.
*  @param[in] outputDir Output directory.
*  @param[in] resourcesJson JSON with resources that should be generated.
*  @param[in] reportProgress Callback that is invoked when the calculation progress is changed.
*  @return Zero in the successful execution.
*/
extern "C" DllExport int generateAvatarFromPhotoWithResources(void* resourcesHandler, const char *imagePath, const char* outputDir, 
    const char *resourcesJson, ReportProgress reportProgress);

/**
*  Gets a pointer to the buffer of vertices for the latest generated avatar.
*  The vertices buffer is a raw float values with 3 floats per vertex.
*  @param[out] verticesCount Vertices count.
*  @return Vertices buffer pointer.
*/
extern "C" DllExport const float* getAvatarVertices(int& verticesCount);

/**
*  Gets a pointer to the RGB texture for the latest generated avatar.
*  RGB values are stored as 3 channels of unsigned char data.
*  @param[out] textureWidth Texture width.
*  @param[out] textureHeight Texture height.
*  @return RGB texture data pointer.
*/
extern "C" DllExport const unsigned char* getAvatarTexture(int& textureWidth, int& textureHeight);

/**
*  Gets the JSON with all available resources (blendshapes and haircuts).
*  You could modify this JSON and pass it to the generateAvatarFromPhotoWithResources method if some of the haircuts or blendshapes shouldn't be generated.
*  @param[out] resourcesJson Pointer the buffer in which the JSON will be written.
*  @param[in] resourcesBufferSize Size of the buffer.
*  @return Zero if success.
*/
extern "C" DllExport int getBaseResourcesJson(char *resourcesJson, int resourcesBufferSize);

/**
*  Returns the description of the latest error.
*  @param[out] buffer Pointer to the buffer where the message will be written.
*  @param[in] bufferSize Size of the buffer.
*/
extern "C" DllExport void getLastError(char* buffer, int bufferSize);
