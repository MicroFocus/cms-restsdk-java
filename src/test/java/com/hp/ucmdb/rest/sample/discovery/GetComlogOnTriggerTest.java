package com.hp.ucmdb.rest.sample.discovery; 

import org.junit.Test; 
import org.junit.Before; 
import org.junit.After; 

/** 
* GetComlogOnTrigger Tester. 
* 
* @author <Authors name> 
* @since <pre>Jul 8, 2020</pre> 
* @version 1.0 
*/ 
public class GetComlogOnTriggerTest { 

    @Test
    public void testMain() throws Exception {
        GetComlogOnTrigger.main(new String[]{"16.187.188.130","admin","Admin_1234", "myzone"});
    } 

} 
