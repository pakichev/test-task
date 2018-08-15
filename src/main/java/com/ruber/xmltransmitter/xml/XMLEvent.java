package com.ruber.xmltransmitter.xml;


import java.util.Map;

public class XMLEvent {

    private final XMLEventTypes type;
    private final String text;
    private final Map<String, String> attributes;

    public enum XMLEventTypes {
        TEXT, START_ELEMENT, END_ELEMENT, START_DOCUMENT, END_DOCUMENT
    }

    public XMLEvent(XMLEventTypes type, String text, Map<String, String> attributes) {
        this.type = type;
        this.text = text;
        this.attributes = attributes;
    }

    public XMLEventTypes getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(type.toString());

        if (text != null) {
            sb.append(" ");
            sb.append(text);
        }

        if (attributes != null) {
            sb.append(" ");
            sb.append(attributes);
        }
        return sb.toString();
    }

}