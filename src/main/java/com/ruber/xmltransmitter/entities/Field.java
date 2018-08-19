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

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getRequired() {
        return required;
    }

    public String getDigitOnly() {
        return digitOnly;
    }

    public String getReadOnly() {
        return readOnly;
    }
}
