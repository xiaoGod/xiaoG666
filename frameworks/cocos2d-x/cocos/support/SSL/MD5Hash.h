//
//  MD5Hash.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_MD5Hash_h__
#define __cocos2d_libs_MD5Hash_h__

class MD5Hash
{
public:
    MD5Hash();
    virtual ~MD5Hash();
    
    void UpdateData(const uint8* data, int len);
    void UpdateData(const std::string & str);
    
    void Initialize();
    void Finalize();
    
    inline uint8* GetDigest() { return m_digest; }
    inline int GetLength() { return MD5_DIGEST_LENGTH; }
    
    std::string ToHexStr32(bool toupper = true);
    
private:
    MD5_CTX m_ctx;
    uint8 m_digest[MD5_DIGEST_LENGTH];
};

#endif /* __cocos2d_libs_MD5Hash_h__ */
