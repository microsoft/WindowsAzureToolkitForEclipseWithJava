package com.interopbridges.tools.windowsazure;

public enum OSFamilyType {
    WINDOWS_SERVER_2008_R2(2,"Windows Server 2008 R2"),
    WINDOWS_SERVER_2012(3,"Windows Server 2012");

    private int value;
    private String name;

    private OSFamilyType(int value,String name){
        this.value = value;
        this.name  = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
