# Seminare S.R.L. — Sistema de gestión comercial en Java (versión final, TP4)

Sistema de gestión comercial para Seminare S.R.L. (distribuidora Nidera, Río Cuarto).
Seminario de Práctica de Informática — UES21. Proceso Unificado de Desarrollo (PUD).

Casos de uso implementados: **CU01** Registrar Preventa · **CU02** Gestionar Stock de Semillas · **CU03** Despacho y Logística.

## Requisitos
- JDK 17 o superior
- MySQL 8.0.16+ (para las restricciones `CHECK`)
- Driver `mysql-connector-j-8.x.jar` copiado en la carpeta `lib/` (descarga: https://dev.mysql.com/downloads/connector/j/)

## Base de datos
1. Ejecutar `database/script_seminare.sql` (en MySQL Workbench o `mysql -u root -p < database/script_seminare.sql`). Recrea `seminare_db`.
2. Los datos de conexión se leen de `config/db.properties` (clave=valor). Copiar `config/db.properties.ejemplo`
   como `db.properties` y completar usuario y clave. `ejecutar.bat` lo crea solo la primera vez.
   `db.properties` está en `.gitignore`: la contraseña no se publica en GitHub.
3. **Importante:** el script empieza con `DROP DATABASE`. Ejecutarlo de nuevo borra todo lo cargado desde el programa.

## Compilar y ejecutar (desde la raíz del proyecto)
Windows (PowerShell / cmd):
```
javac -encoding UTF-8 -cp "lib/*" -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp "out;lib/*" src.Main
```
Linux / macOS:
```
javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name '*.java')
java -cp "out:lib/*" src.Main
```
Modo demostración **sin MySQL** (datos en memoria): agregar el argumento `--memoria`.
Si MySQL no responde, el programa ofrece pasar a ese modo.

Prueba repetible contra MySQL: `demo.bat` corre el programa con las respuestas de `datos/demo_entrada.txt`
y guarda la pantalla completa en `capturas/salida_demo.txt`.

## Patrón de diseño: MVC (con DAO y Fábrica)
| Parte | Paquete | Contenido |
|---|---|---|
| Vista | `src.vista` | `VistaBase` (abstracta) y `VistaPrincipal`, `VistaPreventa`, `VistaInventario`, `VistaDespacho` |
| Controlador | `src.controlador` | `GestorPreventa`, `GestorInventario`, `GestorDespacho` |
| Modelo | `src.modelo` + `src.dao` | Entidades con sus reglas + acceso a MySQL por interfaces `I*DAO` (JDBC) |
| Archivos | `src.archivos` | `ConfiguracionDB` (lee `config/db.properties`), `ArchivoRemito` (remitos .txt), `ArchivoListaPrecios` (CSV) |
| Soporte | `src.excepciones`, `src.util` | Excepciones propias, ordenación/búsqueda, lectura de consola |

`src.Main` solo arma las piezas: crea los DAO con `FabricaDAO`, se los pasa a los controladores y los controladores a las vistas.

## Requisitos de la consigna y dónde se cumplen
| Requisito | Dónde |
|---|---|
| Encapsulamiento | Atributos privados; el stock solo cambia con `LoteInsumo.reservar()/liberar()` |
| Herencia | `Persona → Productor`; `HibridoNidera → HibridoMaiz / HibridoSoja / HibridoGirasol` |
| Abstracción | Clases abstractas `Persona` y `HibridoNidera`; interfaces `IOrdenDAO`, `ILoteDAO`, … |
| Polimorfismo | `precioUnitarioPara()` / `getPresentacion()` según el cultivo; DAO JDBC o en memoria intercambiables |
| Excepciones | `SeminareException` → `StockInsuficiente`, `DatosInvalidos`, `Persistencia`, `Archivo`; captura centralizada en `VistaBase.ejecutar()` |
| Menú, condicionales y bucles | `VistaBase.mostrar()` (`do/while`), vistas (`switch`), `Consola` (reintento de entradas) |
| Arreglos + ArrayList | Opciones de menú `String[]`; `CULTIVOS` y matriz `int[][]` del resumen de stock; filas del CSV como `String[]` dentro de un `ArrayList<String[]>`; `File.list()` |
| Archivos | Lectura de `config/db.properties`; escritura y lectura de remitos; exportación e importación de la lista de precios (CSV) |
| Constructores | Todas las entidades validan en el constructor |
| Estructuras de datos | `List`, **pila** (`Deque`: cancelar última preventa), **cola** (`Queue`: cola de despacho FIFO) |
| Ordenación y búsqueda | `util.Algoritmos`: inserción, selección, búsqueda binaria y lineal |
| Transacciones | `OrdenDAO`: reserva, cancelación y despacho con `commit/rollback` y control de stock atómico |

## Casos de prueba
- **CP-01** Reservar 20 bolsas de un lote con 100 → orden `RESERVADO`, stock 80.
- **CP-02** Reservar 150 sobre un lote con 80/100 → `StockInsuficienteException`, no se guarda nada.
- **CP-03** CUIT duplicado o con dígito verificador inválido → se rechaza el alta del productor.
- **CP-04** Cancelar la última preventa → las bolsas vuelven al lote.
- **CP-05** Despacho con patente inválida → se rechaza y la orden sigue `RESERVADO`; con patente válida → remito `R-0001-########`.
- **CP-09** Resumen de stock por cultivo → la suma de bolsas coincide con `SELECT SUM(bolsas_disponibles)`.
- **CP-10** Importar CSV con filas válidas e inválidas → se actualizan las válidas en MySQL y se informa cada fila rechazada.
- **CP-11** Importar un archivo que no existe → `[ARCHIVO]` y el programa sigue.
- **CP-12** Despachar → se crea `remitos/R-0001-########.txt` y se puede volver a leer desde el menú.

Todos los datos de ejemplo (CUIT, lotes, precios) son ficticios.
