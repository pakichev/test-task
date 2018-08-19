package com.ruber.xmltransmitter.entities;

public class Field {
    private String name;
    private String type;
    private String value;
    private String required;
    private String digitOnly;
    private String readOnly;

    @Override
    public String toString() {
        return "Field{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' +
                ", required='" + required + '\'' +
                ", digitOnly='" + digitOnly + '\'' +
                ", readOnly='" + readOnly + '\'' +
                '}';
    }
}
