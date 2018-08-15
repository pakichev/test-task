package com.ruber.xmltransmitter;

import com.ruber.xmltransmitter.util.IO;
import com.ruber.xmltransmitter.util.Publishers;
import com.ruber.xmltransmitter.xml.XML;
import com.ruber.xmltransmitter.xml.XMLEvent;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;

public class XMLTransmitter {

    private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String... args) throws InterruptedException {

        String directory = "/home/ruber/projects/test-task/src/main/resources/com/ruber/xmltransmitter";

        Flowable<XMLEvent> flowable = Flowable.create(
                Publishers.allFilesFromDirectory(directory, 1), BackpressureStrategy.BUFFER
        )
                .concatMap(IO::readFileAsync)
                .map(ByteBuffer::array)
                .lift(XML.parseToXmlEventsOperator())
                .doOnNext(e -> {
                    log.debug("[TYPE] {}, [TEXT] {}", e.getType(), e.getText());
                });

        flowable.blockingSubscribe();
    }

}
