package com.cuatrovientos.kafka.GestionColaImpresion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Semaphore;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.google.gson.Gson;

public class ImpresoraConsumidor {
    // Semáforos
    private static final Semaphore semColor = new Semaphore(2);
    private static final Semaphore semBN = new Semaphore(3);

    public static void main(String[] args) {
        // 1. Configuración de Kafka 
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "grupo-impresoras");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        
        // 2. Suscribirse a ambos topics (BN y Color)
        consumer.subscribe(Arrays.asList("impresion-bn", "impresion-color"));

        System.out.println("Impresoras listas. Esperando documentos...");

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                for (ConsumerRecord<String, String> record : records) {
                    // Por cada mensaje, lanzamos un HILO para no bloquear el consumidor
                    new Thread(() -> {
                        // Aquí convertimos el JSON a Documento 
                    	Gson gson = new Gson();
                    	Documento doc = gson.fromJson(record.value(), Documento.class);
                    	imprimir(doc); // Aquí ya llama a tu método con los semáforos
                        System.out.println("Recibido trabajo de: " + record.topic());
                    }).start();
                }
            }
        } finally {
            consumer.close();
        }
    }

    public static void imprimir(Documento doc) {
        Semaphore semaforo = (doc.getTipo() == TipoImpresion.COLOR) ? semColor : semBN;
        String nombreImpresora = (doc.getTipo() == TipoImpresion.COLOR) ? "COLOR-P2" : "BN-PASILLO";
        String carpetaDestino = "impresoras_finales/" + nombreImpresora;

        try {
            semaforo.acquire(); // PSP: Exclusión mutua
            System.out.println("[TRABAJANDO] " + nombreImpresora + " imprimiendo: " + doc.getTitulo());
            
            File dir = new File(carpetaDestino);
            if (!dir.exists()) dir.mkdirs();

            Thread.sleep(1500); // Simulamos tiempo de impresión
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            semaforo.release(); // Liberar recurso
            System.out.println("[FINALIZADO] " + doc.getTitulo());
        }
    }
}