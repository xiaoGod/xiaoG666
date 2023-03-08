//
//  BigNumber.h
//  cocos2d_libs
//
//  Created by xxx on 2019/08/20.
//

#ifndef __cocos2d_libs_BigNumber_h__
#define __cocos2d_libs_BigNumber_h__

class BigNumber
{
public:
    BigNumber();
    BigNumber(const BigNumber & bn);
    BigNumber(uint32 ut);
    virtual ~BigNumber();
    
    void SetDword(uint32 val);
    void SetQword(uint64 val);
    void SetBinary(const uint8 * bytes, int len);
    void SetHexStr(const char * str);
    
    void SetRand(int numbits);
    void SetUint64(uint64 va);
    uint64 GetUint64();
    
    BigNumber operator=(const BigNumber & bn);
    
    BigNumber operator+=(const BigNumber & bn);
    BigNumber operator+(const BigNumber & bn) {
        BigNumber t(*this);
        return t += bn;
    }
    BigNumber operator-=(const BigNumber & bn);
    BigNumber operator-(const BigNumber & bn) {
        BigNumber t(*this);
        return t -= bn;
    }
    BigNumber operator*=(const BigNumber & bn);
    BigNumber operator*(const BigNumber & bn) {
        BigNumber t(*this);
        return t *= bn;
    }
    BigNumber operator/=(const BigNumber & bn);
    BigNumber operator/(const BigNumber & bn) {
        BigNumber t(*this);
        return t /= bn;
    }
    BigNumber operator%=(const BigNumber & bn);
    BigNumber operator%(const BigNumber & bn) {
        BigNumber t(*this);
        return t %= bn;
    }
    
    bool operator==(const BigNumber & bn);
    bool operator!=(const BigNumber & bn)
    {
        BigNumber t(*this);
        return !(t==bn);
    }
    
    BigNumber ModExp(const BigNumber & bn1, const BigNumber & bn2);
    BigNumber Exp(const BigNumber &);
    
    int GetNumBytes(void);
    
    struct bignum_st *BN() { return _bn; }
    
    uint32 AsDword();
    uint8* AsByteArray();
    std::vector<uint8> AsByteVector();
    
    std::string AsHexStr();
    std::string AsDecStr();
    
private:
    struct bignum_st *_bn;
    uint8 *_array;
};

#endif /* __cocos2d_libs_BigNumber_h__ */
