package model;

public class Student {
    private int id;
    private String name;
    private int age;
    private String branch;

    public Student() {
    }

    public Student(int id, String name, int age, String branch) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.branch = branch;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}

