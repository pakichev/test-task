package com.ruber.xmltransmitter.xml;

import io.reactivex.FlowableOperator;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class XML {

    public static FlowableOperator<XMLEvent, byte[]> parseToXmlEventsOperator() {

        return new FlowableOperator<>() {

            private final SAXXMLFeeder feeder = new SAXXMLFeeder();

            @Override
            public Subscriber<? super byte[]> apply(Subscriber<? super XMLEvent> observer) {

                return new Subscriber<>() {

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
