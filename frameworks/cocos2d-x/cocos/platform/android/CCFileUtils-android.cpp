/****************************************************************************
Copyright (c) 2010-2012 cocos2d-x.org
Copyright (c) 2013-2016 Chukong Technologies Inc.
Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
****************************************************************************/

#include "platform/CCPlatformConfig.h"
#if CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID

#include "platform/android/CCFileUtils-android.h"
#include "platform/android/jni/JniHelper.h"
#include "platform/android/jni/JniImp.h"
#include "android/asset_manager.h"
#include "android/asset_manager_jni.h"
#include "base/ZipUtils.h"
#include <stdlib.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "external/sources/json/document-wrapper.h"

#define  LOG_TAG    "CCFileUtils-android.cpp"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#define  ASSETS_FOLDER_NAME          "@assets/"

#ifndef JCLS_HELPER
#define JCLS_HELPER "org/cocos2dx/lib/Cocos2dxHelper"
#endif

#include "support/ResourceManager.h"


NS_CC_BEGIN

AAssetManager* FileUtilsAndroid::assetmanager = nullptr;
ZipFile* FileUtilsAndroid::obbfile = nullptr;

void FileUtilsAndroid::setassetmanager(AAssetManager* a) {
    if (nullptr == a) {
        LOGD("setassetmanager : received unexpected nullptr parameter");
        return;
    }

    cocos2d::FileUtilsAndroid::assetmanager = a;
}

FileUtils* FileUtils::getInstance()
{
    if (s_sharedFileUtils == nullptr)
    {
        s_sharedFileUtils = new FileUtilsAndroid();
        if (!s_sharedFileUtils->init())
        {
          delete s_sharedFileUtils;
          s_sharedFileUtils = nullptr;
          CCLOG("ERROR: Could not init CCFileUtilsAndroid");
        }
    }
    return s_sharedFileUtils;
}

FileUtilsAndroid::FileUtilsAndroid()
{
}

FileUtilsAndroid::~FileUtilsAndroid()
{
    if (obbfile)
    {
        delete obbfile;
        obbfile = nullptr;
    }
}

bool FileUtilsAndroid::init()
{
    _defaultResRootPath = ASSETS_FOLDER_NAME;
    
    std::string assetsPath(getApkPathJNI());
    if (assetsPath.find("/obb/") != std::string::npos)
    {
        obbfile = new ZipFile(assetsPath);
    }

    _hasFileListInternal = loadFileListInternal();

    return FileUtils::init();
}

std::string FileUtilsAndroid::getNewFilename(const std::string &filename) const
{
    std::string newFileName = FileUtils::getNewFilename(filename);
    // ../xxx do not fix this path
    auto pos = newFileName.find("../");
    if (pos == std::string::npos || pos == 0)
    {
        return newFileName;
    }

    std::vector<std::string> v(3);
    v.resize(0);
    auto change = false;
    size_t size = newFileName.size();
    size_t idx = 0;
    bool noexit = true;
    while (noexit)
    {
        pos = newFileName.find('/', idx);
        std::string tmp;
        if (pos == std::string::npos)
        {
            tmp = newFileName.substr(idx, size - idx);
            noexit = false;
        }else
        {
            tmp = newFileName.substr(idx, pos - idx + 1);
        }
        auto t = v.size();
        if (t > 0 && v[t-1].compare("../") != 0 &&
             (tmp.compare("../") == 0 || tmp.compare("..") == 0))
        {
            v.pop_back();
            change = true;
        }else
        {
            v.push_back(tmp);
        }
        idx = pos + 1;
    }

    if (change)
    {
        newFileName.clear();
        for (auto &s : v)
        {
            newFileName.append(s);
        }
    }
    return newFileName;
}

bool FileUtilsAndroid::isFileExistInternal(const std::string& strFilePath) const
{
    if (strFilePath.empty())
    {
        return false;
    }

    bool bFound = false;

    // Check whether file exists in apk.
    if (strFilePath[0] != '/')
    {
        const char* s = strFilePath.c_str();

        // Found "@assets/" at the beginning of the path and we don't want it
        if (strFilePath.find(ASSETS_FOLDER_NAME) == 0) s += strlen(ASSETS_FOLDER_NAME);
        if (obbfile && obbfile->fileExists(s))
        {
            bFound = true;
        }
        else if (FileUtilsAndroid::assetmanager)
        {
            AAsset* aa = AAssetManager_open(FileUtilsAndroid::assetmanager, s, AASSET_MODE_UNKNOWN);
            if (aa)
            {
                bFound = true;
                AAsset_close(aa);
            } else {
                // CCLOG("[AssetManager] ... in APK %s, found = false!", strFilePath.c_str());
            }
        }
    }
    else
    {
        FILE *fp = fopen(strFilePath.c_str(), "r");
        if (fp)
        {
            bFound = true;
            fclose(fp);
        }
    }
    return bFound;
}

bool FileUtilsAndroid::isDirectoryExistInternal(const std::string& dirPath_) const
{
    if (dirPath_.empty())
    {
        return false;
    }

    std::string dirPath = dirPath_;
    if (dirPath[dirPath.length() - 1] == '/')
    {
        dirPath[dirPath.length() - 1] = '\0';
    }

    // find absolute path in flash memory
    if (dirPath[0] == '/')
    {
//        CCLOG("find in flash memory dirPath(%s)", dirPath.c_str());
        struct stat st;
        if (stat(dirPath.c_str(), &st) == 0)
        {
            return S_ISDIR(st.st_mode);
        }
    }
    else
    {
        // find it in apk's assets dir
        // Found "@assets/" at the beginning of the path and we don't want it
//        CCLOG("find in apk dirPath(%s)", dirPath.c_str());
        const char* s = dirPath.c_str();
        if (dirPath.find(_defaultResRootPath) == 0)
        {
            s += _defaultResRootPath.length();
        }
        if (FileUtilsAndroid::assetmanager)
        {
            AAssetDir* aa = AAssetManager_openDir(FileUtilsAndroid::assetmanager, s);
            if (aa && AAssetDir_getNextFileName(aa))
            {
                AAssetDir_close(aa);
                return true;
            }
        }
    }
    
    return false;
}

bool FileUtilsAndroid::isAbsolutePath(const std::string& strPath) const
{
    // On Android, there are two situations for full path.
    // 1) Files in APK, e.g. assets/path/path/file.png
    // 2) Files not in APK, e.g. /data/data/org.cocos2dx.hellocpp/cache/path/path/file.png, or /sdcard/path/path/file.png.
    // So these two situations need to be checked on Android.
    if (strPath[0] == '/' || strPath.find(ASSETS_FOLDER_NAME) == 0)
    {
        return true;
    }
    return false;
}

FileUtils::Status FileUtilsAndroid::getContents(const std::string& filename, ResizableBuffer* buffer)
{
    if (filename.empty())
        return FileUtils::Status::NotExists;

    std::string fullPath = fullPathForFilename(filename);
    if (fullPath.empty())
        return FileUtils::Status::NotExists;

    if (fullPath[0] == '/')
        return FileUtils::getContents(fullPath, buffer);

    // LOGD("FileUtilsAndroid::assetmanager start fileNmae %s",filename.c_str());

    std::string relativePath;
    size_t position = fullPath.find(ASSETS_FOLDER_NAME);
    if (0 == position) {
        // "@assets/" is at the beginning of the path and we don't want it
        relativePath += fullPath.substr(strlen(ASSETS_FOLDER_NAME));
    } else {
        relativePath = fullPath;
    }

    if (obbfile)
    {
        if (obbfile->getFileData(relativePath, buffer))
            return FileUtils::Status::OK;
    }

    if (nullptr == assetmanager) {
        LOGD("... FileUtilsAndroid::assetmanager is nullptr");
        return FileUtils::Status::NotInitialized;
    }

    AAsset* asset = AAssetManager_open(assetmanager, relativePath.data(), AASSET_MODE_UNKNOWN);
    if (nullptr == asset) {
        LOGD("asset (%s) is nullptr", filename.c_str());
        return FileUtils::Status::OpenFailed;
    }

    auto size = AAsset_getLength(asset);
    buffer->resize(size);

#if defined(RESOURCE_ENCRYPT)
    // std::string::size_type pos = fullPath.find("/res/");
    if (isInEncryptResDir(fullPath)) {
        //CCLOG(" FileUtilsAndroid decrypt file: %s", fullPath.c_str());
        ResourceManager _manager;
        bool ret = _manager.loadDataFromFile(asset, buffer);
        AAsset_close(asset);
        if(ret)
            return Status::OK;
        else
            return Status::ReadFailed;
    }
#endif

    int readsize = AAsset_read(asset, buffer->buffer(), size);
    AAsset_close(asset);

    if (readsize < size) {
        if (readsize >= 0)
            buffer->resize(readsize);
        return FileUtils::Status::ReadFailed;
    }

    return FileUtils::Status::OK;
}

std::string FileUtilsAndroid::getWritablePath() const
{
    // Fix for Nexus 10 (Android 4.2 multi-user environment)
    // the path is retrieved through Java Context.getCacheDir() method
    std::string dir("");
    std::string tmp = JniHelper::callStaticStringMethod(JCLS_HELPER, "getWritablePath");

    if (tmp.length() > 0)
    {
        dir.append(tmp).append("/");

        return dir;
    }
    else
    {
        return "";
    }
}

std::string FileUtilsAndroid::getBundlePath() const
{
    std::string dir = "";
    dir = JniHelper::callStaticStringMethod(JCLS_HELPER, "getBundlePath");
    return dir;
}

bool FileUtilsAndroid::loadFileListInternal()
{
    std::string fileListPath = "assets/config/fileList.json";
    if (nullptr == assetmanager) {
        LOGD("in loadFileListInternal: FileUtilsAndroid::assetmanager is nullptr");
        return false;
    }

    AAsset* asset = AAssetManager_open(assetmanager, fileListPath.data(), AASSET_MODE_UNKNOWN);
    if (nullptr == asset) {
        LOGD("in loadFileListInternal: asset (%s) is nullptr", fileListPath.c_str());
        return false;
    }

    std::string content;
    auto size = AAsset_getLength(asset);
    content.resize(size);

    AAsset_read(asset, (void*)content.c_str(), size);
    AAsset_close(asset);

    rapidjson::Document json;
    json.Parse<0>(content.c_str());
    if (json.HasParseError()) {
        LOGD("in loadFileListInternal: json (%s) parse error", fileListPath.c_str());
        return false;
    }
    _fileListMap.clear();
    auto array = json.GetArray();
    for (int i=0; i<array.Size(); i++) {
        auto items = array[i].GetObject();
        for (const auto &item : items) {
            std::string key = item.name.GetString();
            std::string value = item.value.GetString();
            _fileListMap.emplace(key, value);
//            _filenameLookupDict.emplace(key, value);
        }
    }

    if (_fileListMap.empty())
        return false;

    return true;
}

std::string FileUtilsAndroid::updateFileName(const std::string& fileName) const
{
    auto cacheIter = _fileListMap.find(fileName);
    if(cacheIter != _fileListMap.end())
    {
        return cacheIter->second;
    }

    return fileName;
}

NS_CC_END

#endif // CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID
