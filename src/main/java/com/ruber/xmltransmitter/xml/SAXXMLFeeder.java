package com.ruber.xmltransmitter.xml;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import org.reactivestreams.Subscriber;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SAXXMLFeeder {

    private final AsyncXMLStreamReader<AsyncByteArrayFeeder> asyncXMLStreamReader;
    private final AsyncByteArrayFeeder wrappedFeeder;

    SAXXMLFeeder() {
        AsyncXMLInputFactory f = new InputFactoryImpl();
        this.asyncXMLStreamReader = f.createAsyncForByteArray();
        this.wrappedFeeder = asyncXMLStreamReader.getInputFeeder();
    }

    public void endOfInput(Subscriber<? super XMLEvent> observer) {
        wrappedFeeder.endOfInput();
        parse(new byte[0], observer);
        observer.onComplete();
    }

    public void parse(byte[] buffer, Subscriber<? super XMLEvent> subscriber) {
        try {
            if (buffer.length > 0) {
                wrappedFeeder.feedInput(buffer, 0, buffer.length);
            }

            int currentToken = -1;
            while (asyncXMLStreamReader.hasNext() && currentToken != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
                currentToken = asyncXMLStreamReader.next();
                if (currentToken != AsyncXMLStreamReader.EVENT_INCOMPLETE) {
                    subscriber.onNext(processEvent(currentToken));
                }
            }
        } catch (XMLStreamException e) {
            subscriber.onError(e);
        }
    }

    private XMLEvent processEvent(int token) {

        switch (token) {
            case XMLStreamConstants.START_DOCUMENT:
                return new XMLEvent(XMLEvent.XMLEventTypes.START_DOCUMENT, null, null);

            case XMLStreamConstants.END_DOCUMENT:
                return new XMLEvent(XMLEvent.XMLEventTypes.END_DOCUMENT, null, null);

            case XMLStreamConstants.START_ELEMENT:
                Map<String, String> attributes = new HashMap<>();
                for (int i = 0; i < asyncXMLStreamReader.getAttributeCount(); i++) {
                    attributes.put(asyncXMLStreamReader.getAttributeLocalName(i), asyncXMLStreamReader.getAttributeValue(i));
                }
                return new XMLEvent(XMLEvent.XMLEventTypes.START_ELEMENT, asyncXMLStreamReader.getLocalName(),
                        Collections.unmodifiableMap(attributes));

            case XMLStreamConstants.END_ELEMENT:
                return new XMLEvent(XMLEvent.XMLEventTypes.END_ELEMENT, asyncXMLStreamReader.getLocalName(), null);

            case XMLStreamConstants.CHARACTERS:
                return new XMLEvent(XMLEvent.XMLEventTypes.TEXT, asyncXMLStreamReader.getText(), null);
        }

        throw new IllegalArgumentException("Token is not identified: " + token);
    }

}
