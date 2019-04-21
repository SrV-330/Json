package com.wsf.json;

import com.wsf.json.exception.JsonParserException;

import java.util.*;

public class JsonParse {

    private static Queue<Character> jsonQueue=new LinkedList<>();

    private static HashMap<String,Class> jsonBodies=new HashMap<>();
    private Stack<Object> stack=new Stack<>();

    public JsonParse(){}

    public JsonParse(String jsonBody){

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

    private void getJsonBody(){

    }
    private void getClasses(){

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
            return new Node(key,new JsonParse().parser());
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
