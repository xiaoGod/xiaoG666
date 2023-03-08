//
//  RC4.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_RC4_h__
#define __cocos2d_libs_RC4_h__

class RC4
{
public:
    RC4();
    RC4(uint8 len);
    RC4(uint8 * seed);
    RC4(uint8 * seed,uint8 len);
    ~RC4();
    void Init(uint8 * seed);
    void UpdateData(size_t len ,uint8 * data);

private:
    EVP_CIPHER_CTX *m_ctx;
};

#endif /* __cocos2d_libs_RC4_h__ */
