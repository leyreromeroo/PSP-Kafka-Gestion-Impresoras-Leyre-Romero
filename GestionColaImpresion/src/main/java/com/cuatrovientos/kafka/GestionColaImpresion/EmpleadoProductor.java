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
	        ObjectMapper mapper = new ObjectMapper();
	        String topicDestino = "gestor-impresoras"; 

	        // Generamos 10 peticiones de empleados diferentes
	        for (int i = 1; i <= 10; i++) {
	            // Alternamos entre COLOR y BN para probar ambos semáforos
	            TipoImpresion tipo = (i % 2 == 0) ? TipoImpresion.COLOR : TipoImpresion.BN;
	            String nombreEmpleado = "Empleado_" + i;
	            
	            Documento miDoc = new Documento("Doc_Prueba_" + i, "Contenido del documento numero " + i, tipo, nombreEmpleado);

	            String jsonDoc = mapper.writeValueAsString(miDoc);
	            System.out.println(">>> [" + nombreEmpleado + "] ENVIANDO: " + miDoc.getTitulo() + " (" + tipo + ")");

	            ProducerRecord<String, String> record = new ProducerRecord<>(topicDestino, jsonDoc);

	            productor.send(record);
	            
	            // Un pequeño retraso para simular que no todos dan al botón a la vez
	            Thread.sleep(200); 
	        }

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