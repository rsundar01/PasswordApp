package com.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SHA512Hash {

    private final String HASH_ALGORITHM = "SHA-512";

    public byte[] computeHash(byte[] iData){
        byte[] computedHash = null;
        try{
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(iData);
            computedHash = md.digest();
        }catch(NoSuchAlgorithmException nsaException){
            throw new RuntimeException(nsaException.getMessage());
        }
        return computedHash;
    }


}
