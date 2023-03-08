//
//  ResourceManager.cpp
//  libcocos2d iOS
//
//  Created by xxx on 2019/8/20.
//

#include "SSL/OpensslHeader.h"
#include "ResourceManager.h"

#if(CC_TARGET_PLATFORM == CC_PLATFORM_IOS)
#include <zlib.h>
#else
#include "zlib/zlib.h"
#endif

ResourceManager::ResourceManager(): m_offset(0)
{
    std::memset(&m_header, 0, sizeof(m_header));
    m_blocks.clear();
}

ResourceManager::~ResourceManager()
{
    
}

bool ResourceManager::loadDataFromFile(FILE *fp, ResizableBuffer* data)
{
    //load header
    size_t readsize = fread(&m_header, 1, sizeof(m_header), fp);
    if(readsize != sizeof(m_header))
        return false;
    
    m_offset = sizeof(m_header);
    m_blocks.resize(m_header.blockCount);
    
    //load blockinfo
    uint32 bytes = m_header.blockCompressedSize;
    m_offset = sizeof(m_header) + m_header.compressedSize;
    _loadDataWithDecryptAndDecompress(fp, &m_blocks[0], bytes, m_offset);
    m_offset += m_header.compressedSize;
    
    //load block
    uint64 offset = 0;
    uint8 buffer[BLOCK_DATA_SIZE + 0x100];
    uint8 *wtbuffer = reinterpret_cast<uint8*>(malloc(m_header.fileSize + 0x100));
    std::memset(wtbuffer, 0, m_header.fileSize + 0x100);
    for (auto block : m_blocks) {
//        CCLOG("[size:%d blockbegin:%llu]", block.compressedSize, block.blockBegining);
        std::memset(buffer, 0, BLOCK_DATA_SIZE + 0x100);
        uint32 bufferSize = _loadDataWithDecompress(fp, block, buffer);
#if !defined(COCOS2D_DEBUG) || COCOS2D_DEBUG == 0
        printf("bufferSize:%d\n", bufferSize);
#endif
        uint32 wtSize = _writeBuffer(wtbuffer, reinterpret_cast<const void*>(buffer), bufferSize, offset);
        offset += wtSize;
    }
    
    data->resize(m_header.fileSize);
    memcpy(data->buffer(), reinterpret_cast<const void*>(wtbuffer), m_header.fileSize);
    free(wtbuffer);
    
    return true;
}

uint32 ResourceManager::_writeBuffer(uint8 *buffer, const void * data, uint32 bytes, uint64 offset)
{
    memcpy(buffer + offset, data, bytes);
    return bytes;
}

uint32 ResourceManager::_loadDataWithDecompress(FILE *fp, const SPKBlockInfo & block, void * dest)
{
    uLongf decompressedSize = block.blockSize;
    fseek(fp, static_cast<long>(block.blockBegining), SEEK_SET);
    
    uint8 *buffer = reinterpret_cast<uint8*>(malloc(block.compressedSize + 0x100));
    memset(buffer, 0, block.compressedSize + 0x100);
    fread(buffer, 1, block.compressedSize, fp);
    if(block.compressedSize < block.blockSize) {
        uncompress(reinterpret_cast<Bytef*>(dest), &decompressedSize, \
                   reinterpret_cast<const Bytef*>(buffer), static_cast<uLongf>(block.compressedSize));
    }
    else
        std::memcpy(dest, buffer, block.blockSize);
    
    free(buffer);
    return static_cast<uint32>(decompressedSize);
}

uint32 ResourceManager::_loadDataWithDecryptAndDecompress(FILE *fp, void * dest, uint32 bytes, uint64 offset)
{
    uLongf decompressedSize = m_header.blockCount * sizeof(SPKBlockInfo) + 0x100;
    uint8 * buffer = reinterpret_cast<uint8*>(malloc(decompressedSize));
    
    fseek(fp, static_cast<long>(offset), SEEK_SET);
    fread(buffer, 1, bytes, fp);
    
    AuthCrypt crypt;
    BigNumber num;
    num.SetBinary(PACKAGE_KEY, sizeof(PACKAGE_KEY));
    crypt.Init(&num);
    crypt.Decrypt(reinterpret_cast<uint8*>(buffer), bytes);
    
    uncompress(reinterpret_cast<Bytef*>(dest), &decompressedSize, \
               reinterpret_cast<const Bytef*>(buffer), static_cast<uLongf>(bytes));
    
    free(buffer);
    return static_cast<uint32>(decompressedSize);
}
