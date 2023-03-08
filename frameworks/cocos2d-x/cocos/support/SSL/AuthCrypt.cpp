//
//  AuthCrypt.cpp
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#include "OpensslHeader.h"

AuthCrypt::AuthCrypt()
{
    m_bInitialized = false;
}

AuthCrypt::~AuthCrypt()
{
    
}

void AuthCrypt::Init( BigNumber * pCryptKey,BigNumber * pKey )
{
    
    HMACHash nEncryptHmac(static_cast<uint32>(pCryptKey->GetNumBytes()), pCryptKey->AsByteArray());
    uint8 *encryptHash = nEncryptHmac.ComputeHash(pKey);
    
    HMACHash nDecryptHmac(static_cast<uint32>(pCryptKey->GetNumBytes()), pCryptKey->AsByteArray());
    uint8 *decryptHash = nDecryptHmac.ComputeHash(pKey);
    
    m_Decrypt.Init(decryptHash);
    m_Encrypt.Init(encryptHash);
    
    uint8 syncBuf[1024];
    memset(syncBuf, 0, 1024);
    m_Decrypt.UpdateData(1024, syncBuf);
    
    memset(syncBuf, 0, 1024);
    m_Encrypt.UpdateData(1024, syncBuf);
    
    m_bInitialized = true;
}

void AuthCrypt::Init( BigNumber * pEncryptionKey,BigNumber * pDecryptionKey,BigNumber * pKey )
{
    HMACHash nEncryptHmac(static_cast<uint32>(pEncryptionKey->GetNumBytes()), pDecryptionKey->AsByteArray());
    uint8 *encryptHash = nEncryptHmac.ComputeHash(pKey);
    
    HMACHash nDecryptHmac(static_cast<uint32>(pEncryptionKey->GetNumBytes()), pDecryptionKey->AsByteArray());
    uint8 *decryptHash = nDecryptHmac.ComputeHash(pKey);
    
    m_Decrypt.Init(decryptHash);
    m_Encrypt.Init(encryptHash);
    
    uint8 syncBuf[1024];
    memset(syncBuf, 0, 1024);
    m_Decrypt.UpdateData(1024, syncBuf);
    
    memset(syncBuf, 0, 1024);
    m_Encrypt.UpdateData(1024, syncBuf);
    
    m_bInitialized = true;
}

void AuthCrypt::Init( BigNumber * pKey )
{
    uint8 arrCryptKey[16] = { 0x22, 0xBE, 0xE5, 0xCF, 0xBB, 0x07, 0x64, 0xD9, 0x00, 0x45, 0x1B, 0xD0, 0x24, 0xB8, 0xD5, 0x45 };
    HMACHash nCryptHmac(16, static_cast<uint8*>(arrCryptKey));
    uint8 * CryptHash = nCryptHmac.ComputeHash(pKey);
    
    m_Decrypt.Init(CryptHash);
    m_Encrypt.Init(CryptHash);
    
    uint8 syncBuf[1024];
    memset(syncBuf, 0, 1024);
    m_Decrypt.UpdateData(1024, syncBuf);
    
    memset(syncBuf, 0, 1024);
    m_Encrypt.UpdateData(1024, syncBuf);
    
    m_bInitialized = true;
}

void AuthCrypt::Decrypt( uint8 * pBuffer,size_t stLen )
{
    if(!m_bInitialized)
        return;
    
    m_Decrypt.UpdateData(stLen,pBuffer);
}

void AuthCrypt::Encrypt( uint8 * pBuffer,size_t stLen )
{
    if(!m_bInitialized)
        return;
    
    m_Encrypt.UpdateData(stLen,pBuffer);
}
