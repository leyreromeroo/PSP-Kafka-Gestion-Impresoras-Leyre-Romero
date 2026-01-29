package com.cuatrovientos.kafka.GestionColaImpresion;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

public class GestorConsumidor {
	public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "grupo-gestion-impresion");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("gestor-impresoras"));
        
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(" esperando trabajos de impresión...");

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    // JSON -> Objeto Documento
                    Documento doc = objectMapper.readValue(record.value(), Documento.class);
                    
                    System.out.println("\n📥 [NUEVA PETICIÓN]");
                    System.out.println("Remitente: " + doc.getSender());
                    System.out.println("Título: " + doc.getTitulo());
                    
                    // Lógica según el TipoImpresion
                    if (doc.getTipo() == TipoImpresion.COLOR) {
                        System.out.println("ACCIÓN: Enviando a Impresora LASER-COLOR-P2");
                    } else {
                        System.out.println("ACCIÓN: Enviando a Impresora BN-PASILLO");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
