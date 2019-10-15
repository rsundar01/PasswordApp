package com.security;

import java.util.Base64;

public class Base64Encoder {

    public byte[] encodeBase64(byte[] iData){
        return Base64.getEncoder().encode(iData);
    }

}
