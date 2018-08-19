package com.ruber.xmltransmitter.xml;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.ruber.xmltransmitter.ConstructionException;
import com.ruber.xmltransmitter.FeedingObjectsConstructor;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class UniversalObjectsConstructor<T> implements FeedingObjectsConstructor<T> {

    private final AsyncXMLStreamReader<AsyncByteArrayFeeder> asyncXMLStreamReader;
    private final Class<T> classToConstruct;
    private final Constructor<T> classConstructor;

    private final String xmlNodeName;
    private T objectToConstruct;

    private Field fieldToConstruct;
    private Map<String, Field> fieldToConstructCache = new HashMap<>();
    private T preparedResult;

    UniversalObjectsConstructor(AsyncXMLStreamReader<AsyncByteArrayFeeder> asyncXMLStreamReader, String xmlNodeName,
                                Class<T> classToConstruct) throws NoSuchMethodException {
        this.asyncXMLStreamReader = asyncXMLStreamReader;
        this.xmlNodeName = xmlNodeName;
        this.classToConstruct = classToConstruct;
        this.classConstructor = classToConstruct.getDeclaredConstructor();
    }

    @Override
    public T next() {
        if (preparedResult == null) {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
        }
        T ret = preparedResult;
        preparedResult = null;
        return ret;
    }

    @Override
    public boolean hasNext() {

        int currentToken = -1;
        try {
            while (asyncXMLStreamReader.hasNext() && currentToken != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
                currentToken = asyncXMLStreamReader.next();
                if (currentToken != AsyncXMLStreamReader.EVENT_INCOMPLETE) {

                    switch (currentToken) {
                        case XMLStreamConstants.START_ELEMENT:
                            processStartNode();
                            break;
                        case XMLStreamConstants.END_ELEMENT:
                            T result = processEndNode();
                            if (result != null) {
                                this.preparedResult = result;
                                return true;
                            }
                            break;
                        case XMLStreamConstants.CHARACTERS:
                            processText();
                            break;

                    }
                }
            }
        } catch (XMLStreamException | InstantiationException | InvocationTargetException |
                IllegalAccessException | NoSuchFieldException e) {
            throw new ConstructionException(e);
        }
        return false;
    }

    private void processText() throws IllegalAccessException {
        if (constructionInProgress()) {
            if (fieldToConstruct != null) {
                if (fieldToConstruct.getType() != String.class) {
                    throw new UnsupportedOperationException(String.format("Not supported yet: %s", fieldToConstruct.getType()));
                }
                fieldToConstruct.set(objectToConstruct, asyncXMLStreamReader.getText());
            }
        }
    }

    private T processEndNode() {
        if (constructionInProgress()) {
            if (isRootNode()) {
                checkConstructionIsAwaitingForNewField();
                T ret = objectToConstruct;
                objectToConstruct = null;
                return ret;
            } else {
                checkConstructionFieldInProgress();
                fieldToConstruct = null;
            }
        }
        return null;
    }

    private void processStartNode() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        if (isRootNode()) {
            checkConstructionIsAwaitingForNewInstance();
            objectToConstruct = classConstructor.newInstance();
        } else {
            if (constructionInProgress()) {
                checkConstructionIsAwaitingForNewField();
                this.fieldToConstruct = getDeclaredField(asyncXMLStreamReader.getLocalName());
                this.fieldToConstruct.setAccessible(true);
            }
        }
    }

    private Field getDeclaredField(String name) throws NoSuchFieldException {
        Field field = fieldToConstructCache.get(name);
        if (field == null) {
            field = classToConstruct.getDeclaredField(name);
            fieldToConstructCache.put(name, field);
        }
        return field;
    }

    private boolean constructionInProgress() {
        return objectToConstruct != null;
    }

    private void checkConstructionIsAwaitingForNewField() {
        if (fieldToConstruct != null) {
            throw new ConstructionException(String.format("Constructing node %s is not complete. ", fieldToConstruct.toString()));
        }
    }

    private void checkConstructionFieldInProgress() {
        if (fieldToConstruct == null) {
            throw new ConstructionException("Have no nodes in construction");
        }
    }

    private void checkConstructionIsAwaitingForNewInstance() {
        if (objectToConstruct != null) {
            throw new ConstructionException(String.format("Previous instance construction is not completed: %s", objectToConstruct));
        }
    }

    private boolean isRootNode() {
        return xmlNodeName.equals(asyncXMLStreamReader.getLocalName());
    }
}
