/****************************************************************************
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos.com

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated engine source code (the "Software"), a limited,
 worldwide, royalty-free, non-assignable, revocable and non-exclusive license
 to use Cocos Creator solely to develop games on your target platforms. You shall
 not use Cocos Creator software for developing other software or tools that's
 used for developing games. You are not granted to publish, distribute,
 sublicense, and/or sell copies of Cocos Creator.

 The software or tools in this License Agreement are licensed, not sold.
 Xiamen Yaji Software Co., Ltd. reserves all rights not expressly granted to you.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/

#include "jsb_platform.h"

#include "cocos/scripting/js-bindings/jswrapper/SeApi.h"
#include "cocos/scripting/js-bindings/manual/jsb_conversions.hpp"
#include "cocos/scripting/js-bindings/manual/jsb_global.h"
#include "cocos/platform/CCFileUtils.h"

#include <string.h>
#include <android/log.h>
#define  LOG_TAG    "platform_android"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#include <regex>

#ifndef JCLS_CANVASIMPL
#define JCLS_CANVASIMPL  "org/cocos2dx/lib/CanvasRenderingContext2DImpl"
#endif


using namespace cocos2d;

static std::unordered_map<std::string, std::string> _fontFamilyNameMap;

const std::unordered_map<std::string, std::string>& getFontFamilyNameMap()
{
    return _fontFamilyNameMap;
}

static bool JSB_loadFont(se::State& s)
{
    const auto& args = s.args();
    size_t argc = args.size();
    CC_UNUSED bool ok = true;
    if (argc >= 1) {
        s.rval().setNull();

        std::string originalFamilyName;
        ok &= seval_to_std_string(args[0], &originalFamilyName);
        SE_PRECONDITION2(ok, false, "JSB_loadFont : Error processing argument: originalFamilyName");

        std::string source;
        ok &= seval_to_std_string(args[1], &source);
        SE_PRECONDITION2(ok, false, "JSB_loadFont : Error processing argument: source");

        std::string fontFilePath;
        std::regex re("url\\(\\s*'\\s*(.*?)\\s*'\\s*\\)");
        std::match_results<std::string::const_iterator> results;
        if (std::regex_search(source.cbegin(), source.cend(), results, re))
        {
            fontFilePath = results[1].str();
        }

        LOGD("fontPath is :%s", fontFilePath.c_str());

#if defined(RESOURCE_ENCRYPT)
        fontFilePath = FileUtils::getInstance()->fullPathForFilename(fontFilePath);
        //LOGD("font full Path is :%s", fontFilePath.c_str());
        std::string tempPath = FileUtils::getInstance()->getWritablePath() + cocos2dAudioDir();
        if(!FileUtils::getInstance()->isDirectoryExist(tempPath))
            FileUtils::getInstance()->createDirectory(tempPath);

        int idx = fontFilePath.find_last_of("/") + 1;
        std::string fileName = fontFilePath.substr(idx);
        std::string filePath = tempPath + fileName;

        if (!FileUtils::getInstance()->isFileExist(filePath))
        {
            LOGD("will decrypt,font full Path is :%s", filePath.c_str());
            Data data = FileUtils::getInstance()->getDataFromFile(fontFilePath);
            FileUtils::getInstance()->writeDataToFile(data, filePath);
        }
        fontFilePath = filePath;
#else
        fontFilePath = FileUtils::getInstance()->fullPathForFilename(fontFilePath);
#endif

        if (fontFilePath.empty())
        {
            SE_LOGE("Font (%s) doesn't exist!", fontFilePath.c_str());
            return true;
        }

        JniHelper::callStaticVoidMethod(JCLS_CANVASIMPL, "loadTypeface", originalFamilyName, fontFilePath);

        s.rval().setString(originalFamilyName);
        
        return true;
    }

    SE_REPORT_ERROR("wrong number of arguments: %d, was expecting %d", (int)argc, 1);
    return false;
}
SE_BIND_FUNC(JSB_loadFont)

bool register_platform_bindings(se::Object* obj)
{
    __jsbObj->defineFunction("loadFont", _SE(JSB_loadFont));
    return true;
}