//
//  AuthCrypt.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_AuthCrypt_h__
#define __cocos2d_libs_AuthCrypt_h__

#include "RC4.h"
#include "BigNumber.h"

class AuthCrypt
{
private:
    RC4    m_Decrypt;
    RC4    m_Encrypt;
    bool    m_bInitialized;
public:
    AuthCrypt();
    ~AuthCrypt();
    void Init(BigNumber * pCryptKey,BigNumber * pKey);
    void Init(BigNumber * pEncryptionKey,BigNumber * pDecryptionKey,BigNumber * pKey);
    void Init(BigNumber * pKey);
    
    void Decrypt(uint8 * pBuffer,size_t stLen);
    void Encrypt(uint8 * pBuffer,size_t stLen);
};

#endif /* __cocos2d_libs_AuthCrypt_h__ */
