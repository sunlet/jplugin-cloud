package net.jplugin.cloud.rpc;

import net.jplugin.common.kits.AssertKit;
import net.jplugin.common.kits.JsonKit;
import net.jplugin.common.kits.ReflactKit;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Test {
    public static class Address{
        String street;
        String num;

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }
    }

    public static class Bean {
        String name;
        int age;
        Address addr;

        public Address getAddr() {
            return addr;
        }

        public void setAddr(Address addr) {
            this.addr = addr;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException {
        Bean b = new Bean();
        b.setAge(11);
        b.setName("lh");
        Address addr= new Address();
        addr.setNum("88");
        addr.setStreet("hebei");
        b.setAddr(addr);

        List<Bean> list = new ArrayList();
        list.add(b);

        String str = JsonKit.object2JsonEx(list);


        Method method = ReflactKit.findSingeMethodExactly(Test.class, "aaa");

        Type ret = method.getGenericReturnType();
        System.out.println(ret);

        AssertKit.assertTrue(ret == void.class);




//        System.out.println(result);
    }

    public void aaa(Map<String,List<Bean>>[] bbb){
        
    }

    class AAA{

    }
}
