//
//  MD4Hash.cpp
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#include "OpensslHeader.h"

MD4Hash::MD4Hash()
{
    
}

MD4Hash::~MD4Hash()
{
    
}

void MD4Hash::UpdateData(const uint8 *data, int len)
{
    MD4_Update(&m_ctx, data, len);
}

void MD4Hash::UpdateData(const std::string &str)
{
    UpdateData((const uint8*)str.data(), (int)str.length());
}

void MD4Hash::Initialize()
{
    MD4_Init(&m_ctx);
}

void MD4Hash::Finalize()
{
    MD4_Final(m_digest, &m_ctx);
}

std::string MD4Hash::ToHexStr32(bool toupper /*= true*/)
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
