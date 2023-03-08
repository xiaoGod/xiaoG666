//
//  ResourceManager.h
//  libcocos2d iOS
//
//  Created by xxx on 2019/8/20.
//

#ifndef __cocos2d_libs_ResourceManager_h__
#define __cocos2d_libs_ResourceManager_h__

#include "cocos2d.h"
#include "PackageData.h"

#include <sys/stat.h>

using namespace cocos2d;

class ResourceManager {
    typedef std::vector<SPKBlockInfo> VecSPKBlockInfo;
    
public:
    ResourceManager();
    virtual ~ResourceManager();
    
    bool loadDataFromFile(FILE *fp, ResizableBuffer* data);

#if (CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID)
    bool loadDataFromFile(AAsset *fp, ResizableBuffer* data);
#endif
    
private:
    uint32 _writeBuffer(uint8 *buffer, const void * data, uint32 bytes, uint64 offset);
    uint32 _loadDataWithDecompress(FILE *fp, const SPKBlockInfo & block, void * dest);
    uint32 _loadDataWithDecryptAndDecompress(FILE *fp, void * dest, uint32 bytes, uint64 offset);

#if (CC_TARGET_PLATFORM == CC_PLATFORM_ANDROID)
    uint32 _loadDataWithDecompress(AAsset *fp, const SPKBlockInfo & block, void * dest);
    uint32 _loadDataWithDecryptAndDecompress(AAsset *fp, void * dest, uint32 bytes, uint64 offset);
#endif
    
private:
    SPKFileHeader m_header;
    uint64 m_offset;
    VecSPKBlockInfo m_blocks;
};

#endif /* __cocos2d_libs_ResourceManager_h__ */
