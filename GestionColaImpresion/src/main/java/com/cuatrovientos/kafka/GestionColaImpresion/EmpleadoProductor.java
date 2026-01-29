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
			
			// 1. Creamos el objeto con datos de prueba
			Documento miDoc = new Documento("Factura_001", "Contenido de la factura...", TipoImpresion.BN, "Leyre Romero");

			// 2. Jackson ObjectMapper para convertir a JSON
			ObjectMapper mapper = new ObjectMapper();

			// 3. Convertimos el objeto a una cadena String (JSON)
			String jsonDoc = mapper.writeValueAsString(miDoc);
			
			// 4. Enviamos el JSON al topic
			System.out.println(">>> Enviando a Kafka el siguiente JSON: " + jsonDoc);
			productor.send(new ProducerRecord<>(KafkaConfig.TOPIC_GESTOR, miDoc.getSender(), jsonDoc));
			// Enviamos al topic "gestor-impresoras"
           

		}
		catch (Exception e){
			System.err.println("Excepcion general al intentar generar documentos: "+e.getMessage());
			e.printStackTrace();
		}
		finally {
			if (productor != null) {
				productor.flush();
				productor.close();
			}
		}
	}
}
