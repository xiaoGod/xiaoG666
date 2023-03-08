//
//  RC4.cpp
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#include "OpensslHeader.h"

RC4::RC4()
{
    m_ctx = EVP_CIPHER_CTX_new();
    EVP_CIPHER_CTX_init(m_ctx);
    EVP_EncryptInit_ex(m_ctx, EVP_rc4(), NULL, NULL, NULL);
    EVP_CIPHER_CTX_set_key_length(m_ctx, SHA_DIGEST_LENGTH);
}

RC4::RC4(uint8 *seed)
{
    EVP_CIPHER_CTX_init(m_ctx);
    EVP_EncryptInit_ex(m_ctx, EVP_rc4(), NULL, NULL, NULL);
    EVP_CIPHER_CTX_set_key_length(m_ctx, SHA_DIGEST_LENGTH);
    EVP_EncryptInit_ex(m_ctx, NULL, NULL, seed, NULL);
}

RC4::RC4( uint8 len )
{
    EVP_CIPHER_CTX_init(m_ctx);
    EVP_EncryptInit_ex(m_ctx, EVP_rc4(), NULL, NULL, NULL);
    EVP_CIPHER_CTX_set_key_length(m_ctx, len);
}

RC4::RC4( uint8 *seed,uint8 len )
{
    EVP_CIPHER_CTX_init(m_ctx);
    EVP_EncryptInit_ex(m_ctx, EVP_rc4(), NULL, NULL, NULL);
    EVP_CIPHER_CTX_set_key_length(m_ctx, len);
    EVP_EncryptInit_ex(m_ctx, NULL, NULL, seed, NULL);
}

RC4::~RC4()
{
    EVP_CIPHER_CTX_free(m_ctx);
}

void RC4::Init(uint8 *seed)
{
    EVP_EncryptInit_ex(m_ctx, NULL, NULL, seed, NULL);
}

void RC4::UpdateData(size_t len, uint8 *data)
{
    int outlen = 0;
    EVP_EncryptUpdate(m_ctx, data, &outlen, data, (int)len);
    EVP_EncryptFinal_ex(m_ctx, data, &outlen);
}
