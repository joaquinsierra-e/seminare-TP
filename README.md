# Seminare S.R.L. — Prototipo Java (TP3)

Sistema de gestión comercial para Seminare S.R.L. (distribuidora Nidera, Río Cuarto).
Seminario de Práctica de Informática — UES21. Proceso Unificado de Desarrollo (PUD).

Casos de uso implementados: **CU01** Registrar Preventa · **CU02** Gestionar Stock de Semillas · **CU03** Despacho y Logística.

## Requisitos
- JDK 17 o superior
- MySQL 8.0.16+ (para las restricciones `CHECK`)
- Driver `mysql-connector-j-8.x.jar` copiado en la carpeta `lib/` (descarga: https://dev.mysql.com/downloads/connector/j/)

## Base de datos
1. Ejecutar `database/script_seminare.sql` (en MySQL Workbench o `mysql -u root -p < database/script_seminare.sql`). Recrea `seminare_db`.
2. Por defecto se conecta a `localhost:3306/seminare_db` con usuario `root` / contraseña `root`.
   Se puede cambiar con las variables de entorno `SEMINARE_DB_URL`, `SEMINARE_DB_USER` y `SEMINARE_DB_PASS`.

## Ejecución rápida (Windows)
Doble clic en `ejecutar.bat` (o `.\ejecutar.bat` en la terminal): descarga el driver JDBC si falta, compila y ejecuta. Acepta `--memoria`.

## Compilar y ejecutar a mano (desde la raíz del proyecto)
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

## Arquitectura en 3 capas
| Capa | Paquete | Contenido |
|---|---|---|
| Presentación | `src.Main` | Menú por consola (clase Boundary) |
| Negocio | `src.controlador` | `GestorPreventa`, `GestorInventario`, `GestorDespacho` |
| Persistencia | `src.dao` | Interfaces `I*DAO` + implementación JDBC (`PreparedStatement`, transacciones) y `src.dao.memoria` |
| Dominio | `src.modelo` | Entidades y reglas de negocio |
| Soporte | `src.excepciones`, `src.util` | Excepciones propias, ordenación/búsqueda, lectura de consola |

## Requisitos de la consigna y dónde se cumplen
| Requisito | Dónde |
|---|---|
| Encapsulamiento | Atributos privados; el stock solo cambia con `LoteInsumo.reservar()/liberar()` |
| Herencia | `Persona → Productor`; `HibridoNidera → HibridoMaiz / HibridoSoja / HibridoGirasol` |
| Abstracción | Clases abstractas `Persona` y `HibridoNidera`; interfaces `IOrdenDAO`, `ILoteDAO`, … |
| Polimorfismo | `precioUnitarioPara()` / `getPresentacion()` según el cultivo; DAO JDBC o en memoria intercambiables |
| Excepciones | `SeminareException` → `StockInsuficiente`, `DatosInvalidos`, `Persistencia`; captura centralizada en `Main.ejecutar()` |
| Menú, condicionales y bucles | `Main` (menús con `do/while` + `switch`), `Consola` (reintento de entradas) |
| Constructores | Todas las entidades validan en el constructor |
| Estructuras de datos | `List`, **pila** (`Deque`: cancelar última preventa), **cola** (`Queue`: cola de despacho FIFO) |
| Ordenación y búsqueda | `util.Algoritmos`: inserción, selección, búsqueda binaria y lineal |
| Transacciones | `OrdenDAO`: reserva, cancelación y despacho con `commit/rollback` y control de stock atómico |

## Casos de prueba (continuación de AP2)
- **CP-01** Reservar 20 bolsas de un lote con 100 → orden `RESERVADO`, stock 80.
- **CP-02** Reservar 150 sobre un lote con 80/100 → `StockInsuficienteException`, no se guarda nada.
- **CP-03** CUIT duplicado o con dígito verificador inválido → se rechaza el alta del productor.
- **CP-04** Cancelar la última preventa → las bolsas vuelven al lote.
- **CP-05** Despacho con patente inválida → se rechaza y la orden sigue `RESERVADO`; con patente válida → remito `R-0001-########`.

Todos los datos de ejemplo (CUIT, lotes, precios) son ficticios.
