package com.ruber.xmltransmitter.util;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IO {

    private static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int BUFFER_SIZE = 1024;

    private IO() {

    }

    public static Flowable<? extends ByteBuffer> readFileAsync(Path path) {
        return Flowable.create(emmiter ->
                        IO.readAsync(emmiter, 0, AsynchronousFileChannel.open(path, StandardOpenOption.READ)),
                BackpressureStrategy.BUFFER
        );
    }

    private static void readAsync(Emitter<ByteBuffer> emitter, int position, AsynchronousFileChannel fileChannel) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        fileChannel.read(byteBuffer, position, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {

            @Override
            public void completed(Integer result, ByteBuffer byteBuffer) {
                if (log.isTraceEnabled()) {
                    log.trace("Partial data was read: {}", new String(byteBuffer.array(), Charset.forName("UTF-8")));
                }

                if (result > 0) {
                    // это не рекурсивный вызов! вызывает CompletionHandler другой экзекутор (который внутри fileChannel)
                    // StackOverflowError невозможен
                    emitter.onNext(ByteBuffer.allocate(result).put(byteBuffer.array(), 0, result));

                    if (result == BUFFER_SIZE) {
                        readAsync(emitter, position + BUFFER_SIZE, fileChannel);
                    } else {
                        emitter.onComplete();
                    }
                } else {
                    emitter.onComplete();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                emitter.onError(exc);
            }

        });
    }

}
