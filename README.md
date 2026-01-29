# Sistema Gestor de Colas de Impresión - Kafka

Este proyecto implementa un sistema de mensajería distribuida utilizando **Apache Kafka** para gestionar una cola de impresión empresarial. El sistema permite la transformación de documentos, persistencia de originales y distribución eficiente entre múltiples impresoras siguiendo el patrón **Productor-Consumidor**.

## 1. Arquitectura de TOPICS
Se ha diseñado una topología de tres topics para garantizar la independencia de procesos y el escalado horizontal:

* **`gestor-impresoras`**: Topic de entrada. Recibe los mensajes JSON originales enviados por los empleados.
* **`impresion-bn`**: Topic intermedio para mensajes transformados (máximo 400 caracteres) destinados a las 3 impresoras de blanco y negro.
* **`impresion-color`**: Topic intermedio para mensajes transformados destinados a las 2 impresoras de color.



## 2. Estructura de Clases (Java/Maven)
El proyecto está construido con Maven y la librería `kafka-clients`. La lógica se divide en las siguientes clases:

* **`EmpleadoProductor`**: Simula al empleado que manda a imprimir. [cite_start]Genera y envía el JSON con la estructura: `{ "titulo", "documento", "tipo", "sender" }`.
* **`OrquestadorConsumidor`**: Es el componente central que realiza dos tareas en paralelo:
    * **Persistencia**: Guarda el JSON original en carpetas locales según el `sender`.
    * **Transformación**: Divide el texto en "páginas" de 400 caracteres y lo redirige al topic de B/N o Color.
* **`ImpresoraConsumidor`**: Simula la impresión final. Se instancian múltiples veces (3 para B/N y 2 para Color) usando **Consumer Groups** para repartir la carga.

## 3. Guía del Implantador (Puesta en Marcha)
Para desplegar el sistema en un entorno local de desarrollo:

1.  **Requisitos**: JDK > 17 y Kafka 4.1.1.
2.  **Formatear Almacenamiento (KRaft)**:
    ```powershell
    .\kafka-storage.bat format --standalone -t <TU_CLUSTER_ID> -c ..\..\config\server.properties
    ``` 
3.  **Iniciar Servidor**:
    ```powershell
    .\kafka-server-start.bat ..\..\config\server.properties
    ``` 
4.  **Crear Topics**: Ejecutar el comando `kafka-topics.bat --create` para los tres topics mencionados en el punto 1.

## 4. Guía del Mantenedor (Reinicio y Limpieza)
Para asegurar la salud del sistema o realizar un mantenimiento preventivo:

* **Limpieza de Mensajes**: Detenga los servicios y elimine la carpeta `C:\tmp\kraft-combined-logs`. Esto eliminará todos los mensajes acumulados que no se borran por defecto en Kafka.
* **Reseteo de Identidad**: Si el sistema muestra errores de `Invalid cluster.id`, es necesario volver a ejecutar el proceso de formateo (`format`) antes de arrancar el broker.
* **Monitoreo**: Use `--describe` para verificar el estado de las particiones y el ritmo de los consumidores (Offset).

## 5. Información para el Desarrollador
* **Dependencia**: Incluir `kafka-clients` v4.1.1 en el `pom.xml`.
* **Serialización**: Se utilizan `StringSerializer` y `StringDeserializer` para el manejo de los mensajes JSON.
* **Paralelismo**: El orquestador debe ser capaz de procesar la persistencia y la transformación de forma asíncrona para máxima eficiencia.
