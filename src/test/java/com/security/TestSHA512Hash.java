package com.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestSHA512Hash
{
    private SHA512Hash sha512Hash;
    private Base64Encoder base64Encoder;

    /** test data **/
    int nTestData = 2;
    String[] data = new String[nTestData];
    String[] hashdata = new String[nTestData];

    @Before
    public void setUp(){
        sha512Hash = new SHA512Hash();
        base64Encoder = new Base64Encoder();
        data[0] = "procore";
        hashdata[0] = "VWx+vwK0xCGmazm68Bs7grHIXJv7Nl0W3vwR2DZ79dLGgoG0L+/9O3zc1xRmM28ltCujLRUb1/nEqJU3fQJMRw==";

        data[1] = "angryMonkey";
        hashdata[1] = "ZEHhWB65gUlzdVwtDQArEyx+KVLzp/aTaRaPlBzYRIFj6vjFdqEb0Q5B8zVKCZ0vKbZPZklJz0Fd7su2A+gf7Q==";
    }


    @Test
    public void computeHash()
    {
        for(int i = 0; i < data.length; i++) {
            byte[] hash = sha512Hash.computeHash(data[i].getBytes());
            assertNotNull(hash);
        }
    }

    @Test
    public void computeBase64Hash(){
        for(int i = 0; i < data.length; i++) {
            byte[] hash = base64Encoder.encodeBase64(sha512Hash.computeHash(data[i].getBytes()));
            Assert.assertEquals(hashdata[i], new String(hash));
        }
    }
}
