package org.openconnectors.kafka;

import com.twitter.heron.streamlet.Context;
import com.twitter.heron.streamlet.Source;
import org.openconnectors.config.ConfigProvider;
import org.openconnectors.util.PullWrapperOnPushSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class HeronKafkaSource implements Source<String> {

    private static final Logger LOG = LoggerFactory.getLogger(HeronKafkaSource.class);
    private PullWrapperOnPushSource<String> pullHose;
    private Collection<String> buffer;

    @Override
    public void setup(Context context) {
        if (pullHose == null) {
            KafkaSource kafkaSource = new KafkaSource();
            pullHose = new PullWrapperOnPushSource<> (kafkaSource, 1024 * 1024,
                PullWrapperOnPushSource.BufferStrategy.BLOCK);
        }
        try {
            pullHose.open(new ConfigProvider());
        } catch (Exception e) {
            throw new RuntimeException("Exception during setup of Kafka Source", e);
        }

    }

    @Override
    public String get() {
        if (buffer == null || buffer.isEmpty()) {
            try {
                buffer = pullHose.fetch();
            } catch (Exception e) {
                LOG.error("Exception thrown while fetching from Kafka Source");
            }
        }
        if (buffer != null && !buffer.isEmpty()) {
            String retval = buffer.iterator().next();
            buffer.clear();
            return retval;
        } else {
            return null;
        }
    }

    @Override
    public void cleanup() {
        try {
            pullHose.close();
        } catch (Exception e) {
            LOG.error("Exception thrown while closing Kafka Source");
        }
    }
}
