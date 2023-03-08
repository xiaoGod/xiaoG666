//
//  HMACHash.cpp
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#include "OpensslHeader.h"

HMACHash::HMACHash(uint32 len, uint8 *seed)
{
    
    assert(len == SEED_KEY_SIZE);
    m_ctx = HMAC_CTX_new();
    HMAC_Init_ex(m_ctx, seed, SEED_KEY_SIZE, EVP_sha1(), nullptr);
}

HMACHash::~HMACHash()
{
    HMAC_CTX_free(m_ctx);
}

void HMACHash::UpdateBigNumber(BigNumber *bn)
{
    UpdateData(bn->AsByteArray(), bn->GetNumBytes());
}

void HMACHash::UpdateData(const uint8 *data, int length)
{
    HMAC_Update(m_ctx, data, static_cast<size_t>(length));
}

void HMACHash::Finalize()
{
    unsigned int length = 0;
    HMAC_Final(m_ctx, static_cast<uint8*>(m_digest), &length);
    assert(length == SHA_DIGEST_LENGTH);
}

uint8 *HMACHash::ComputeHash(BigNumber *bn)
{
    HMAC_Update(m_ctx, bn->AsByteArray(), static_cast<size_t>(bn->GetNumBytes()));
    Finalize();
    return static_cast<uint8*>(m_digest);
}

std::string HMACHash::ToHexStr20(bool toupper /*= true*/)
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
