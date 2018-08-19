package com.ruber.xmltransmitter;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.ruber.xmltransmitter.entities.Field;
import com.ruber.xmltransmitter.util.IO;
import com.ruber.xmltransmitter.util.Publishers;
import com.ruber.xmltransmitter.xml.XML;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.Future;

public class XMLTransmitter {

    private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String... args) throws InterruptedException, IOException, XMLStreamException {

        System.out.print("Enter path:");
        Scanner sc = new Scanner(System.in);
        String inputDirectory = sc.nextLine();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        Flowable<XMLStreamWriter> flowable = Flowable.create(
                Publishers.allFilesFromDirectory(inputDirectory, 1), BackpressureStrategy.BUFFER
        )
                .concatMap(path -> {

                    Path destination = Paths.get(inputDirectory, "output", path.toFile().getName().replaceAll("src", "dist"));
                    if(Files.exists(destination)) {
                        Files.delete(destination);
                    }

                    XMLStreamWriter writer = factory.createXMLStreamWriter(new FileWriter(destination.toFile()));
                    writer.writeStartDocument();
                    writer.writeStartElement("Data");

                    return IO.readFileAsync(path)
                            .map(ByteBuffer::array)
                            .lift(XML.parseXmlNodesToObjects("Field", Field.class))
                            .doOnNext(e -> {
                                log.debug("[FIELD] {}", e);
                            })
                            .map(field -> {

                                writer.writeStartElement(field.getType());
                                writer.writeAttribute("name", field.getName());
                                writer.writeAttribute("required", asBoolean(field.getRequired()));
                                writer.writeAttribute("digitOnly", asBoolean(field.getDigitOnly()));
                                writer.writeAttribute("readOnly", asBoolean(field.getReadOnly()));
                                writer.writeAttribute("value", field.getValue());
                                writer.writeEndElement();

                                return writer;
                            })
                            .doOnComplete(() -> {
                                writer.writeEndElement();
                                writer.writeEndDocument();
                                writer.flush();
                            })
                            .doFinally(() -> writer.close())
                            .doOnComplete( () -> {
                                Future<HttpResponse<JsonNode>> jsonResponse = Unirest.post("http://localhost:8080/post")
                                        .field("data", path.toFile(), "dst.xml").asJsonAsync();
                            } );
                });

        flowable
                .blockingSubscribe();
    }

    private static String asBoolean(String text) {
        return String.valueOf("1".equals(text));
    }
}
