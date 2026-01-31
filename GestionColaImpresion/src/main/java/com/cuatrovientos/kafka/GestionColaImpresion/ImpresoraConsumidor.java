package com.cuatrovientos.kafka.GestionColaImpresion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class ImpresoraConsumidor {
	/**
	 * Implementación de exclusión mutua mediante Semáforos.
	 * Controla el acceso a recursos limitados (impresoras) evitando 
	 * condiciones de carrera y gestionando la cola de hilos de forma segura.
	 */
	private final Semaphore semColor = new Semaphore(2);
	private final Semaphore semBN = new Semaphore(3);

	/**
	 * Simula la impresión de un documento controlando el acceso a las impresoras físicas
	 * @param doc El documento o página a imprimir
	 */
	public void imprimir(Documento doc) {
		// Seleccionamos el semáforo y la carpeta destino según el tipo
		Semaphore semaforo = (doc.getTipo() == TipoImpresion.COLOR) ? semColor : semBN;
		String nombreImpresora = (doc.getTipo() == TipoImpresion.COLOR) ? "COLOR-P2" : "BN-PASILLO";
		String carpetaDestino = "impresoras_finales/" + nombreImpresora;

		try {
			// Intentamos adquirir el recurso (hacer cola si están todas ocupadas)
			semaforo.acquire(); 

			System.out.println("🖨️ [TRABAJANDO] Impresora " + nombreImpresora + " imprimiendo: " + doc.getTitulo());

			// Simulación de la impresión física: Crear el archivo en la carpeta correspondiente
			File dir = new File(carpetaDestino);
			if (!dir.exists()) dir.mkdirs();

			// Usamos System.nanoTime() para que si un documento tiene varias páginas, no se sobrescriban
			String nombreFichero = doc.getTitulo().replace(" ", "_") + "_" + System.nanoTime() + ".txt";
			try (FileWriter writer = new FileWriter(new File(dir, nombreFichero))) {
				writer.write("REMITENTE: " + doc.getSender() + "\n");
				writer.write("CONTENIDO:\n" + doc.getContenido());
			}

			// Simulamos el tiempo que tarda la máquina en imprimir (1.5 segundos)
			Thread.sleep(1500); 

		} catch (InterruptedException | IOException e) {
			System.err.println("Error en el proceso de impresión: " + e.getMessage());
			Thread.currentThread().interrupt();
		} finally {
			// Liberamos el semáforo para que otros puedan imprimir
			semaforo.release();
			System.out.println("[FINALIZADO] " + doc.getTitulo() + " en " + nombreImpresora);
		}
	}
}