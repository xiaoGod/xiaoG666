/****************************************************************************
Copyright (c) 2008-2010 Ricardo Quesada
Copyright (c) 2010-2012 cocos2d-x.org
Copyright (c) 2011      Zynga Inc.
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

#include "base/ccMacros.h"
#include <string>
#include "cocos2d.h"

NS_CC_BEGIN

#define RES_SIGN "/assets/"
#define IGNORE ".jsc"

    CC_DLL const char* cocos2dVersion()
    {
        return "2.4.9";
    }

    CC_DLL const char* cocos2dAudioDir()
    {
        return "audiodir/";
    }

    CC_DLL bool isInEncryptResDir(const std::string& filePathName)
    {
#ifdef RESOURCE_ENCRYPT
        std::string::size_type pos = filePathName.find(RES_SIGN);
    std::string::size_type pos1 = filePathName.find(IGNORE);
    if(pos != std::string::npos && pos1 == std::string::npos)
        return true;

    return false;
#else
        return false;
#endif
    }

    CC_DLL const char* packageID()
    {
        return "com.mobile.game111";
    }

NS_CC_END

