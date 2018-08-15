package com.ruber.xmltransmitter.util;

import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Publishers {

    private Publishers() {

    }

    public static FlowableOnSubscribe<Path> allFilesFromDirectory(String directory, int maxDepth) {
        return new EachFileInDirectoryPublisher(directory, maxDepth);
    }

    private static class EachFileInDirectoryPublisher implements FlowableOnSubscribe<Path> {

        private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private final String directory;
        private final int maxDepth;

        EachFileInDirectoryPublisher(String directory, int maxDepth) {
            this.directory = directory;
            this.maxDepth = maxDepth;
        }

        @Override
        public void subscribe(FlowableEmitter<Path> emitter) {
            try {
                Files.walk(Paths.get(directory), maxDepth)
                        .forEach(file -> {
                            log.debug("Emit file: {}", file.getFileName());
                            if (!emitter.isCancelled() && isXmlFile(file)) {
                                emitter.onNext(file);
                            }
                        });
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        }

        private static boolean isXmlFile(Path path) {
            return path.toFile().getName().toLowerCase().endsWith(".xml");
        }

    }

}
