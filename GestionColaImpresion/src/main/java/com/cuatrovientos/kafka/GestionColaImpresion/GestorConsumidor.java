package com.cuatrovientos.kafka.GestionColaImpresion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GestorConsumidor {
	// Necesitaremos un productor para reenviar los mensajes transformados a las colas finales
	private static ImpresoraConsumidor impresoraSimulador = new ImpresoraConsumidor();

	public static void main(String[] args) {
		Properties props = new Properties();

		// Configuración
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.KAFKA_SERVER_IP_PORT);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "grupo-gestion-impresion"); 
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		// El Gestor debe escuchar el topic original donde los empleados mandan los JSON
		consumer.subscribe(Arrays.asList("gestor-impresoras")); 

		ObjectMapper objectMapper = new ObjectMapper();

		try {
			while (true) {
				ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
				for (ConsumerRecord<String, String> record : records) {
					Documento doc = objectMapper.readValue(record.value(), Documento.class);

					// EJECUCIÓN PARALELA
					// Tarea 1: Guardar original (Hilo independiente)
					new Thread(() -> guardarDocumentoOriginal(doc)).start();

					// Tarea 2: Transformar y "Enviar" (Hilo independiente)
					new Thread(() -> transformarYEnviarAImpresora(doc)).start();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			consumer.close();
		}
	}

	private static void transformarYEnviarAImpresora(Documento doc) {
		System.out.println("Transformando documento: " + doc.getTitulo());

		// Aplicamos la paginación de 400 caracteres solicitada
		List<String> paginas = paginarContenido(doc.getContenido());

		// Creamos una versión del documento "preparada" para la impresora
		// (En un entorno real, enviarías un mensaje por cada página a un nuevo Topic de Kafka)
		for (int i = 0; i < paginas.size(); i++) {
			String contenidoPagina = "PÁGINA " + (i + 1) + "/" + paginas.size() + "\n" + paginas.get(i);
			Documento docTransformado = new Documento(doc.getTitulo(), contenidoPagina, doc.getTipo(), doc.getSender());

			// Simulamos el envío a la impresora física usando nuestra clase de control de recursos
			impresoraSimulador.imprimir(docTransformado);
		}
	}

	private static void guardarDocumentoOriginal(Documento doc) {
		try {
			File dir = new File("carpeta_originales/" + doc.getSender());
			if (!dir.exists()) dir.mkdirs();

			FileWriter writer = new FileWriter(new File(dir, doc.getTitulo() + ".json"));
			writer.write(new ObjectMapper().writeValueAsString(doc));
			writer.close();
			System.out.println("Documento original guardado para: " + doc.getSender());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<String> paginarContenido(String contenido) {
		List<String> paginas = new ArrayList<>();
		int length = contenido.length();
		for (int i = 0; i < length; i += 400) {
			paginas.add(contenido.substring(i, Math.min(length, i + 400)));
		}
		return paginas;
	}
}
