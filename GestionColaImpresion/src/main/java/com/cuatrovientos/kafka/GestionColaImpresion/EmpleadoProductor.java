package com.cuatrovientos.kafka.GestionColaImpresion;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmpleadoProductor {

	public static void main(String[] args) {

		KafkaProducer<String, String> productor = null;

		try {
			Properties props = new Properties();
			props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfig.KAFKA_SERVER_IP_PORT); 
			props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

			productor = new KafkaProducer<>(props);

			// 1. Creamos un documento con contenido largo para probar la paginación de 400 caracteres
			String contenidoLargo = "Este es un texto muy largo que debería ser dividido en varias páginas por nuestro gestor. ".repeat(10);
			Documento miDoc = new Documento("Manual_Usuario", contenidoLargo, TipoImpresion.COLOR, "Leyre Romero");

			ObjectMapper mapper = new ObjectMapper();

			// 2. El empleado envía al GESTOR, no a la impresora final
			String topicDestino = "gestor-impresoras"; 

			String jsonDoc = mapper.writeValueAsString(miDoc);
			System.out.println(">>> ENVIANDO PETICIÓN AL GESTOR: " + miDoc.getTitulo());

			ProducerRecord<String, String> record = new ProducerRecord<>(topicDestino, jsonDoc);

			productor.send(record, (metadata, e) -> {
				if (e == null) {
					System.out.println("Documento recibido por el sistema de gestión.");
				} else {
					System.err.println("Error de comunicación: " + e.getMessage());
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (productor != null) {
				productor.flush();
				productor.close();
			}
		}
	}
}
