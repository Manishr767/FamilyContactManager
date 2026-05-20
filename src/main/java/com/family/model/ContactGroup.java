package com.family.model;

public class ContactGroup {
    private int    id;
    private String groupName;

    public ContactGroup() {}
    public ContactGroup(int id, String groupName) {
        this.id        = id;
        this.groupName = groupName;
    }

    public int    getId()        { return id; }
    public String getGroupName() { return groupName; }
    public void   setId(int id)               { this.id = id; }
    public void   setGroupName(String name)   { this.groupName = name; }

    @Override public String toString() { return groupName; }
}
