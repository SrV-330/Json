package com.wsf.json;

import com.sun.xml.internal.bind.v2.TODO;
import com.wsf.json.annotation.JsonBody;
import com.wsf.json.exception.JsonParserException;
import com.wsf.json.exception.JsonUnsupportClassException;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Struct;
import java.util.*;

public class JsonParser {

    private static Queue<Character> jsonQueue=new LinkedList<>();

    private static HashMap<String,Class> jsonBodies=new HashMap<>();

    private Stack<Object> stack=new Stack<>();

    public JsonParser(){
        parserJsonBody();
    }

    public JsonParser(String jsonBody){
        this();
        for(int i=0;i<jsonBody.length();i++){
            jsonQueue.offer(jsonBody.charAt(i));
        }

    }

    public Object parser() {
        int c = -1;

        while (!jsonQueue.isEmpty()) {
            c=-1;
            c = jsonQueue.peek();
            if (c == '{') {
                c = jsonQueue.poll();
                stack.push(c);
            } else if (c == '"') {
                jsonQueue.poll();
                stack.push(parserString());
            }else if(c==':'){
                jsonQueue.poll();
                c=jsonQueue.peek();
                if(c=='"'){
                    stack.push(parserField());
                }else if(c=='{'){
                    stack.push(parserField());
                }
            }else if(c=='}'){
                return parserEnd();
            }else if(c=='['){
                //TODO
            }

        }
        return null;
    }

    private Class obtainClass(String... fieldNames){

        for (Map.Entry<String,Class> entry:jsonBodies.entrySet()){
            Class clazz=entry.getValue();
            if(containerClass(clazz,fieldNames)){
                return clazz;
            }
        }
        return null;

    }
    private boolean containerClass(Class clazz, String... fieldNames){
        for(String fieldName:fieldNames){
            if(!(containerClass(clazz,fieldName)
                    ||containerSubClass(clazz.getSuperclass(),fieldName))){
                return false;
            }
        }
        return  true;
    }
    private boolean containerClass(Class clazz,String fieldName){

        if(clazz==Object.class) return false;
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }

    }
    private boolean containerSubClass(Class clazz,String fieldName){
        if(clazz==Object.class) return false;
        try {
            clazz.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return containerSubClass(clazz.getSuperclass(),fieldName);
        }
    }
    private void parserJsonBody(){

        List<File> list=new ArrayList<>();
        getJavaFiles("src",list);
        List<String> packages=new ArrayList<>();
        for(File f:list){
            String s=f.getAbsolutePath().replace("src/main/java/","")
                    .replace(".java","");
            packages.add(s);
        }

        for(String className:packages){

            try {
                Class<?> clazz=Class.forName(className);
                if(clazz.isAnnotationPresent(JsonBody.class)) {

                    jsonBodies.put(className, clazz);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                continue;
            }
        }


    }

    private void getJavaFiles(String path,List<File> list){
        File file=new File(path);
        if(file.isFile()){
            if(file.getName().endsWith(".java")) {

                list.add(file);
                return;
            }
        }

        File[] files=file.listFiles();
        for(File f:files){
            if(f.isFile()){
                if(f.getName().endsWith(".java")){
                    list.add(f);
                }
            }else{
                getJavaFiles(f.getAbsolutePath(),list);
            }
        }



    }

    private void setFields(Object object,Class clazz,List<Node> nodes){

        for(Node node:nodes) {

            setField(object,clazz,node);

        }
    }
    private void setField(Object object,Class clazz,Node node){


        if(clazz==Object.class) return;
        try {
            Field field=clazz.getDeclaredField(node.getKey());
            field.setAccessible(true);
            if(field.getClass().isPrimitive()) {
                setPrimitiveField(object, field, node.getValue());
            }else{
                setPrimitiveWrapperClass(object,field,node.getValue());
            }

        } catch (Exception e) {
            setField(object,clazz.getSuperclass(),node);
        }


    }
    private void setPrimitiveField(Object object,Field field,Object value)
    throws Exception{
        try {
            Class<?> c=field.getClass();
            String s= String.valueOf( value);
            if(c==Integer.TYPE){
                field.set(object,Integer.parseInt(s));
            }else if(c==Long.TYPE){
                field.set(object,Long.parseLong(s));
            }else if(c==Double.TYPE){
                field.set(object,Double.parseDouble(s));
            }else if(c==Float.TYPE) {
                field.set(object,Float.parseFloat(s));
            }else if(c==Short.TYPE){
                field.set(object,Short.parseShort(s));
            }else if(c==Byte.TYPE){
                field.set(object,Byte.parseByte(s));
            }else if(c==Boolean.TYPE){
                field.set(object,Boolean.parseBoolean(s));
            }else {
                throw new JsonUnsupportClassException("JsonParser Unsupport Class: "+c.getName());
            }
        } catch (IllegalAccessException e) {
            throw e;
        }
    }
    private void setPrimitiveWrapperClass(Object object,Field field,Object value)
    throws Exception{
        try {
            Class<?> c=field.getClass();
            String s= String.valueOf( value);
            if(c==Integer.class){
                field.set(object,Integer.parseInt(s));
            }else if(c==Long.class){
                field.set(object,Long.parseLong(s));
            }else if(c==Double.class){
                field.set(object,Double.parseDouble(s));
            }else if(c==Float.class) {
                field.set(object,Float.parseFloat(s));
            }else if(c==Short.class){
                field.set(object,Short.parseShort(s));
            }else if(c==Byte.class){
                field.set(object,Byte.parseByte(s));
            }else if(c==Boolean.class){
                field.set(object,Boolean.parseBoolean(s));
            }else if(c==String.class) {
                field.set(object,s);
            }else {
                field.set(object,value);
            }
        } catch (IllegalAccessException e) {
            throw e;
        }
    }
    private Object parserEnd(){
        jsonQueue.poll();
        Node node=null;
        List<Node> nodes=new ArrayList<>();
        Object o=null;
        o=stack.peek();
        List<String> keys=new ArrayList<>();
        List<Object> values=new ArrayList<>();
        while(o instanceof Node){
            stack.pop();
            node=(Node)o;
            nodes.add(node);

        }
        if(o instanceof Character||o.getClass()==Character.TYPE){
            if((Character)o=='{'){

                //do nothing;
            }else{
                throw new JsonParserException("miss symbol \'{\'");
            }
        }else{
            throw new JsonParserException("can not parser object:"+o.toString());
        }
        Class clazz=null;
        for(Node n:nodes){
            keys.add(n.getKey());
            values.add(n.getValue());
            clazz=obtainClass(keys.toArray(new String[]{}));
        }
        if(clazz==null) throw
                new JsonParserException("can not find class by fields: "+keys.toString());

        try {
            o=clazz.newInstance();
            setFields(o,clazz,nodes);
            return o;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


    }


    private Node parserField(){

        String key=(String)stack.pop();
        int c=-1;
        c=jsonQueue.peek();
        if(c=='"') {
            jsonQueue.poll();
            return new Node(key, parserString());
        }else if(c=='{'){
            return new Node(key,new JsonParser().parser());
        }
        return new Node(key,null);
    }

    private String parserString(){
        int c=-1;
        c=jsonQueue.poll();
        StringBuilder sb=new StringBuilder("");
        while (c!='"'&&!jsonQueue.isEmpty()){
            sb.append(c);
        }
        if(c!='"'){
            throw new JsonParserException("miss symbol \'\"\' in the end");
        }
        return  sb.toString();
    }

    class Node{
        private String key;
        private Object value;

        public Node(Object value) {
            this.value = value;
        }

        public Node(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }



}
