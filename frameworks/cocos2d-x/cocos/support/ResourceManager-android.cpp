//
// Created by ido on 2019-08-25.
//

#include "ResourceManager.h"
#include "SSL/OpensslHeader.h"
#include "android/asset_manager.h"
#include "zlib/zlib.h"

bool ResourceManager::loadDataFromFile(AAsset *fp, ResizableBuffer* data)
{
    //load header
    size_t readsize = AAsset_read(fp, &m_header, sizeof(m_header));
    if(readsize != sizeof(m_header))
        return false;

    m_offset = sizeof(m_header);
    //CCLOG("loadDataFromFile blockCount:%d,blockCompressedSize is %d,compressedSize is %d,fileSize is %d", m_header.blockCount,m_header.blockCompressedSize,m_header.compressedSize,m_header.fileSize);
    m_blocks.clear();
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

    int index = 0;
    for (auto block : m_blocks) {
        //CCLOG("[index:%d, blocksize:%d blockbegin:%llu]", index++, block.compressedSize, block.blockBegining);
        std::memset(buffer, 0, BLOCK_DATA_SIZE + 0x100);
        uint32 bufferSize = _loadDataWithDecompress(fp, block, buffer);
        uint32 wtSize = _writeBuffer(wtbuffer, reinterpret_cast<const void*>(buffer), bufferSize, offset);
        offset += wtSize;
    }

    data->resize(m_header.fileSize);
    memcpy(data->buffer(), reinterpret_cast<const void*>(wtbuffer), m_header.fileSize);
    free(wtbuffer);

    return true;
}

uint32 ResourceManager::_loadDataWithDecompress(AAsset *fp, const SPKBlockInfo & block, void * dest)
{
    uLongf decompressedSize = block.blockSize;
    AAsset_seek(fp, static_cast<long>(block.blockBegining), 0);

    uint8 buffer[BLOCK_DATA_SIZE + 0x100];
    AAsset_read(fp, buffer, block.compressedSize);
    if(block.compressedSize < block.blockSize)
        uncompress(reinterpret_cast<Bytef*>(dest), reinterpret_cast<uLongf*>(&decompressedSize), \
                   reinterpret_cast<const Bytef*>(buffer), static_cast<uLongf>(block.compressedSize));
    else
        std::memcpy(dest, buffer, block.blockSize);

    return static_cast<uint32>(decompressedSize);
}

uint32 ResourceManager::_loadDataWithDecryptAndDecompress(AAsset *fp, void * dest, uint32 bytes, uint64 offset)
{
    uLongf decompressedSize = m_header.blockCount * sizeof(SPKBlockInfo) + 0x100; //m_header.blockCompressedSize + 0x100;
    uint8 * buffer = reinterpret_cast<uint8*>(malloc(decompressedSize));

    std::memset(buffer, 0, decompressedSize);
    AAsset_seek(fp, static_cast<long>(offset), 0);
    AAsset_read(fp, buffer, bytes);

    AuthCrypt crypt;
    BigNumber num;
    num.SetBinary(PACKAGE_KEY, sizeof(PACKAGE_KEY));
    crypt.Init(&num);
    crypt.Decrypt(reinterpret_cast<uint8*>(buffer), bytes);

    int result = uncompress(reinterpret_cast<Bytef*>(dest), reinterpret_cast<uLongf*>(&decompressedSize), \
               reinterpret_cast<const Bytef*>(buffer), static_cast<uLongf>(bytes));

    if (result != Z_OK)
        CCLOGERROR("uncompress error: %d in ResourceManager::_loadDataWithDecryptAndDecompress", result);

    free(buffer);
    return static_cast<uint32>(decompressedSize);
}