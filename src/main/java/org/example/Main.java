package org.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        Properties props = new Properties();
        props.put("application.id", "kafka-streams-app-v2");
        props.put("bootstrap.servers", "localhost:9092");
        props.put("default.key.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put("default.value.serde", "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put("auto.offset.reset", "earliest");


        StreamsBuilder builder = new StreamsBuilder();

        // 1. Lire depuis text-input
        KStream<String, String> sourceStream = builder.stream("text-input");

        // 2. Effectuer le traitement ( Trim , Multiples espaces , converit en majuscules )
        KStream<String, String> cleanedStream = sourceStream.mapValues(value ->
                value == null ? "" :
                        value.trim().replaceAll("\\s+", " ").toUpperCase()
        );

        // Mots interdits
        Set<String> bannedWords = Set.of("HACK", "SPAM","XXX");

        // 3. Filtrer les messages
        KStream<String, String> validStream =
                cleanedStream.filter((k, v) ->
                        !v.isBlank()
                                && v.length() <= 100
                                && bannedWords.stream().noneMatch(v::contains)
                );

        // INVALID (message ORIGINAL)
        KStream<String, String> invalidStream =
                cleanedStream.filter((k, v) ->
                        v.isBlank()
                                || v.length() > 100
                                || bannedWords.stream().anyMatch(v::contains)
                );

        // 4. Routage
        validStream.to("text-clean");
        invalidStream.to("text-dead-letter");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        streams.start();
    }
}