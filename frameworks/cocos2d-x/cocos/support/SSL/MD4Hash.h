//
//  MD4Hash.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_MD4Hash_h__
#define __cocos2d_libs_MD4Hash_h__

class MD4Hash
{
public:
    MD4Hash();
    virtual ~MD4Hash();
    
    void UpdateData(const uint8* data, int len);
    void UpdateData(const std::string& str);
    
    void Initialize();
    void Finalize();
    
    inline uint8* GetDigest() { return m_digest; }
    inline int GetLength() { return MD4_DIGEST_LENGTH; }
    
    std::string ToHexStr32(bool toupper = true);
    
private:
    MD4_CTX m_ctx;
    uint8 m_digest[MD4_DIGEST_LENGTH];
};

#endif /* __cocos2d_libs_MD4Hash_h__ */
