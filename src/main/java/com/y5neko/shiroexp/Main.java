package com.y5neko.shiroexp;

import com.y5neko.shiroexp.misc.Log;
import org.apache.commons.cli.MissingArgumentException;

public class Main {
    public static void main(String[] args) throws Exception {
        try{
            Console.main(args);
        } catch (MissingArgumentException e){
            System.err.println(Log.buffer_logging("EROR", e.getMessage()));
        }
    }
}
