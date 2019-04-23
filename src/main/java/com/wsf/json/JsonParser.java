package com.wsf.json;

import com.wsf.json.annotation.JsonBody;
import com.wsf.json.exception.JsonParserException;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

public class JsonParser {

    private static Queue<Character> jsonQueue=new LinkedList<>();

    private static HashMap<String,Class> jsonBodies=new HashMap<>();

    private Stack<Object> stack=new Stack<>();

    public JsonParser(){

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

            }

        }
        return null;
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


    private void parserEnd(){
        jsonQueue.poll();
        Node node=null;
        List<Node> nodes=new ArrayList<>();
        Object o=null;
        o=stack.peek();
        List<String> keys=new ArrayList<>();
        while(o instanceof Node){
            stack.pop();
            node=(Node)o;
            nodes.add(node);

        }
        for(Node n:nodes){
            keys.add(n.getKey());
        }
        if(o instanceof Character||o.getClass()==Character.TYPE){
            if((Character)o=='{'){

            }else{
                throw new JsonParserException("miss symbol \'{\'");
            }
        }else{
            throw new JsonParserException("can not parser object:"+o.toString());
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
