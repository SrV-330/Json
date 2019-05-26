package com.wsf.json.entity;

import com.wsf.json.annotation.JsonBody;

@JsonBody
public class User {

    public User() {
    }
    private String name;
    private int age;
    private User fri;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User getFri() {
        return fri;
    }

    public void setFri(User fri) {
        this.fri = fri;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", fri=" + fri +
                '}';
    }
}
