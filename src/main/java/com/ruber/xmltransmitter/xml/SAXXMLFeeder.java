package com.ruber.xmltransmitter.xml;

import com.fasterxml.aalto.AsyncByteArrayFeeder;
import com.fasterxml.aalto.AsyncXMLInputFactory;
import com.fasterxml.aalto.AsyncXMLStreamReader;
import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.ruber.xmltransmitter.ConstructionException;
import com.ruber.xmltransmitter.FeedingObjectsConstructor;
import org.reactivestreams.Subscriber;

import javax.xml.stream.XMLStreamException;

public class SAXXMLFeeder<T> {

    private final AsyncByteArrayFeeder wrappedFeeder;
    private final FeedingObjectsConstructor<? extends T> fieldsConstructor;

    SAXXMLFeeder(String fieldName, Class<T> clazz) {
        AsyncXMLInputFactory f = new InputFactoryImpl();
        AsyncXMLStreamReader<AsyncByteArrayFeeder> asyncXMLStreamReader = f.createAsyncForByteArray();
        this.wrappedFeeder = asyncXMLStreamReader.getInputFeeder();
        try {
            this.fieldsConstructor = new UniversalObjectsConstructor<>(asyncXMLStreamReader, fieldName, clazz);
        } catch (NoSuchMethodException e) {
            throw new ConstructionException(e);
        }
    }

    public void endOfInput(Subscriber<? super T> observer) {
        wrappedFeeder.endOfInput();
        parse(new byte[0], observer);
        observer.onComplete();
    }

    public void parse(byte[] buffer, Subscriber<? super T> subscriber) {
        try {
            if (buffer.length > 0) {
                wrappedFeeder.feedInput(buffer, 0, buffer.length);
            }
            while (fieldsConstructor.hasNext()) {
                subscriber.onNext(fieldsConstructor.next());
            }
        } catch (XMLStreamException e) {
            subscriber.onError(e);
        }
    }

}
