//
//  MD5.cpp
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#include "OpensslHeader.h"
#include <string.h>

MD5Hash::MD5Hash()
{
    MD5_Init(&m_ctx);
}

MD5Hash::~MD5Hash()
{
    
}

void MD5Hash::UpdateData(const uint8 *data, int len)
{
    MD5_Update(&m_ctx, data, len);
}

void MD5Hash::UpdateData(const std::string &str)
{
    UpdateData((const uint8*)str.data(), (int)str.length());
}

void MD5Hash::Initialize()
{
    MD5_Init(&m_ctx);
}

void MD5Hash::Finalize()
{
    MD5_Final(m_digest, &m_ctx);
}

std::string MD5Hash::ToHexStr32(bool toupper /*= true*/)
{
    std::string strRet;
    strRet.clear();
    
    char buf[MD5_DIGEST_LENGTH*2+1] = {0};
    char temp[3] = {0};
    
    ::memset(buf, 0, 33);
    for(int i=0; i<MD5_DIGEST_LENGTH; i++){
        sprintf(temp,"%02x",m_digest[i]);
        strcat(buf, temp);
    }
    
    strRet = buf;
    if(toupper)
        std::transform(strRet.begin(), strRet.end(), strRet.begin(), ::toupper);
    return strRet;
}
