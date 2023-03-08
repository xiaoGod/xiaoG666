//
//  Sha1Hash.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_Sha1Hash_h__
#define __cocos2d_libs_Sha1Hash_h__

class Sha1Hash
{
public:
    Sha1Hash();
    ~Sha1Hash();
    
    void UpdateFinalizeBigNumbers(BigNumber *bn0, ...);
    void UpdateBigNumbers(BigNumber *bn0, ...);
    
    void UpdateData(const uint8 *dta, int len);
    void UpdateData(const std::string &str);
    
    void Initialize();
    void Finalize();
    
    uint8 *GetDigest(void) { return mDigest; };
    int GetLength(void) { return SHA_DIGEST_LENGTH; };
    
    BigNumber GetBigNumber();
    std::string ToHexStr20(bool toupper = true);
    
private:
    SHA_CTX mC;
    uint8 mDigest[SHA_DIGEST_LENGTH];
};

#endif /* __cocos2d_libs_Sha1Hash_h__ */
