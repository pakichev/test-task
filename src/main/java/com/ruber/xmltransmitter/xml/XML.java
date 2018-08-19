package com.ruber.xmltransmitter.xml;

import io.reactivex.FlowableOperator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class XML {

    public static <T> FlowableOperator<T, byte[]> parseXmlNodesToObjects(String fieldName, Class<T> clazz) {

        return new FlowableOperator<T, byte[]>() {

            private final SAXXMLFeeder<T> feeder = new SAXXMLFeeder<>(fieldName, clazz);

            @Override
            public Subscriber<? super byte[]> apply(Subscriber<? super T> observer) {

                return new Subscriber<byte[]>() {

                    @Override
                    public void onSubscribe(Subscription s) {
                        observer.onSubscribe(s);
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        feeder.parse(bytes, observer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        feeder.endOfInput(observer);
                        observer.onComplete();
                    }

                };

            }

        };
    }
}
