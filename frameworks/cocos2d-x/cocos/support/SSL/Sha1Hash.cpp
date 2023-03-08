//
//  Sha1Hash.cpp
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#include "OpensslHeader.h"

Sha1Hash::Sha1Hash()
{
    SHA1_Init(&mC);
}

Sha1Hash::~Sha1Hash()
{
}

void Sha1Hash::UpdateData(const uint8 *dta, int len)
{
    SHA1_Update(&mC, dta, len);
}

void Sha1Hash::UpdateData(const std::string &str)
{
    UpdateData((uint8 *)str.c_str(), (int)str.length());
}

void Sha1Hash::UpdateBigNumbers(BigNumber *bn0, ...)
{
    va_list v;
    BigNumber *bn;
    
    va_start(v, bn0);
    bn = bn0;
    while (bn)
    {
        UpdateData(bn->AsByteArray(), bn->GetNumBytes());
        bn = va_arg(v, BigNumber *);
    }
    va_end(v);
}

void Sha1Hash::Initialize()
{
    SHA1_Init(&mC);
}

void Sha1Hash::Finalize(void)
{
    SHA1_Final(mDigest, &mC);
}

std::string Sha1Hash::ToHexStr20(bool toupper /*= true*/)
{
    std::string strRet;
    strRet.clear();
    
    char buf[MD5_DIGEST_LENGTH*2+1] = {0};
    char temp[3] = {0};
    
    ::memset(buf, 0, 33);
    for(int i=0; i<MD5_DIGEST_LENGTH; i++){
        sprintf(temp,"%02x",mDigest[i]);
        strcat(buf, temp);
    }
    
    strRet = buf;
    if(toupper)
        std::transform(strRet.begin(), strRet.end(), strRet.begin(), ::toupper);
    return strRet;
}
