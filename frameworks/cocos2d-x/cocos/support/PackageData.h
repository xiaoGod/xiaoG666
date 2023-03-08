//
//  PackageData.h
//  libcocos2d iOS
//
//  Created by xxx on 2019/8/20.
//

#ifndef __cocos2d_libs_PackageData_h__
#define __cocos2d_libs_PackageData_h__

#define PACKAGE_SIGN 1129599051
#define SPK_CURRENT_VERSION 1
#define BLOCK_DATA_SIZE 0x4000

typedef uint8_t                         uint8;
typedef int8_t                          int8;
typedef int16_t                         int16;
typedef uint16_t                        uint16;
typedef uint32_t                        uint32;
typedef int32_t                         int32;
typedef int64_t                         int64;
typedef uint64_t                        uint64;

struct SPKFileHeader
{
    uint32 sign;
    uint32 fileSize;
    uint32 compressedSize;
    uint32 blockCount;
    uint32 blockCompressedSize;
    SPKFileHeader():
    sign(PACKAGE_SIGN),
    fileSize(0),
    compressedSize(0),
    blockCount(0),
    blockCompressedSize(0)
    {
        
    }
};

struct SPKBlockInfo
{
    uint32 blockSize;
    uint32 compressedSize;
    uint64 blockBegining;
    SPKBlockInfo():
    blockSize(0),
    compressedSize(0),
    blockBegining(0)
    {
        
    }
};

#endif /* __cocos2d_libs_PackageData_h__ */
