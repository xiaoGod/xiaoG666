//
//  OpensslHeader.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_OpensslHeader_h__
#define __cocos2d_libs_OpensslHeader_h__

#include <stdint.h>

typedef uint8_t                         uint8;
typedef int8_t                          int8;
typedef int16_t                         int16;
typedef uint16_t                        uint16;
typedef uint32_t                        uint32;
typedef int32_t                         int32;
typedef int64_t                         int64;
typedef uint64_t                        uint64;

const uint8 PACKAGE_KEY[] = {0x01, 0x23, 0x45, 0x67, 0x89, 0xab, 0xcd, 0xef, 0x75, 0xb7, 0x87, 0x80, 0x99, 0xe0, 0xc5, 0x96, 0x01, 0x23, 0x45, 0x67};

#include <string>
#include <list>
#include <vector>
#include <assert.h>


#include "openssl/bn.h"
#include "openssl/md5.h"
#include "openssl/md4.h"
#include "openssl/sha.h"
#include "openssl/evp.h"
#include "openssl/hmac.h"

#include "RC4.h"
#include "MD5Hash.h"
#include "MD4Hash.h"
#include "BigNumber.h"
#include "Sha1Hash.h"
#include "HMACHash.h"
#include "AuthCrypt.h"

#endif /* __cocos2d_libs_OpensslHeader_h__ */
