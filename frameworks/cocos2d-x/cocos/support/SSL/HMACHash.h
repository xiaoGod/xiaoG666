//
//  HMACHash.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_HMACHash_h__
#define __cocos2d_libs_HMACHash_h__

#define SEED_KEY_SIZE 16

class HMACHash
{
public:
    HMACHash(uint32 len, uint8 *seed);
    ~HMACHash();
    void UpdateBigNumber(BigNumber *bn);
    void UpdateData(const uint8 *data, int length);
    void Finalize();
    uint8 *ComputeHash(BigNumber *bn);
    uint8 *GetDigest() { return (uint8*)m_digest; }
    int GetLength() { return SHA_DIGEST_LENGTH; }
    
    std::string ToHexStr20(bool toupper = true);
    
private:
    HMAC_CTX *m_ctx;
    uint8 m_digest[SHA_DIGEST_LENGTH];
};

#endif /* __cocos2d_libs_HMACHash_h__ */
