-- 1. CREACIÓN Y SELECCIÓN DE LA BASE DE DATOS
DROP DATABASE IF EXISTS seminare_db;
CREATE DATABASE seminare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE seminare_db;

-- 2. CREACIÓN DE TABLAS (DDL)

-- Tabla de Productores Agrícolas
CREATE TABLE productores (
    productor_id INT AUTO_INCREMENT PRIMARY KEY,
    razon_social VARCHAR(120) NOT NULL,
    cuit VARCHAR(13) NOT NULL UNIQUE,
    telefono VARCHAR(30),
    localidad VARCHAR(50) DEFAULT 'Río Cuarto'
) ENGINE=InnoDB;

-- Tabla de Catálogo Híbridos Nidera
CREATE TABLE hibridos_nidera (
    hibrido_id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_variedad VARCHAR(50) NOT NULL UNIQUE,
    cultivo VARCHAR(50) NOT NULL,
    tratamiento VARCHAR(100),
    precio_usd DECIMAL(10,2) NOT NULL DEFAULT 100.00 CHECK (precio_usd > 0) -- TP3: precio de lista
) ENGINE=InnoDB;

-- Tabla de Inventario de Lotes / Insumos
CREATE TABLE lotes_insumo (
    lote_id INT AUTO_INCREMENT PRIMARY KEY,
    hibrido_id INT NOT NULL,
    numero_lote VARCHAR(50) NOT NULL UNIQUE,
    bolsas_disponibles INT NOT NULL CHECK (bolsas_disponibles >= 0),
    poder_germinativo DECIMAL(5,2) NOT NULL,
    FOREIGN KEY (hibrido_id) REFERENCES hibridos_nidera(hibrido_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Tabla de Órdenes de Preventa
CREATE TABLE ordenes_preventa (
    orden_id INT AUTO_INCREMENT PRIMARY KEY,
    productor_id INT NOT NULL,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'RESERVADO',
    condicion_pago VARCHAR(20) NOT NULL DEFAULT 'CONTADO',          -- TP3: CONTADO | CANJE_GRANOS | PLAZO
    cotizacion_usd DECIMAL(12,2) NOT NULL DEFAULT 1000.00 CHECK (cotizacion_usd > 0), -- TP3: ARS por USD
    CHECK (estado IN ('RESERVADO','DESPACHADO','CANCELADO')),
    FOREIGN KEY (productor_id) REFERENCES productores(productor_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Tabla Detalle de Órdenes (Relación muchos a muchos entre Órdenes y Lotes)
CREATE TABLE detalles_orden (
    detalle_id INT AUTO_INCREMENT PRIMARY KEY,
    orden_id INT NOT NULL,
    lote_id INT NOT NULL,
    cantidad_bolsas INT NOT NULL CHECK (cantidad_bolsas > 0),
    precio_unitario_usd DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (orden_id) REFERENCES ordenes_preventa(orden_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (lote_id) REFERENCES lotes_insumo(lote_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- TP3: Despacho y logística (CU03). Una orden se despacha una sola vez (UNIQUE).
CREATE TABLE ordenes_despacho (
    despacho_id INT AUTO_INCREMENT PRIMARY KEY,
    orden_id INT NOT NULL UNIQUE,
    fecha_despacho DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remito VARCHAR(20) NOT NULL UNIQUE,
    patente_camion VARCHAR(10) NOT NULL,
    FOREIGN KEY (orden_id) REFERENCES ordenes_preventa(orden_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 3. INSERCIÓN DE DATOS DE PRUEBA (DML)

INSERT INTO productores (razon_social, cuit, telefono, localidad) VALUES
('Agropecuaria El Imperio S.A.', '30-71123456-6', '0358-4640000', 'Río Cuarto'),
('Estancia Las Lilas SRL', '30-65432109-0', '0358-4651122', 'Higueras');

INSERT INTO hibridos_nidera (codigo_variedad, cultivo, tratamiento, precio_usd) VALUES
('AX 7784 VT3P', 'Maíz', 'Acceleron Premium', 185.00),
('NS 5025 VIP3', 'Soja', 'Plenus completo', 62.00),
('NIDERA SOL 340 CL', 'Girasol', 'Curasemilla estándar', 98.50);

INSERT INTO lotes_insumo (hibrido_id, numero_lote, bolsas_disponibles, poder_germinativo) VALUES
(1, 'LOT-MAIZ-2026-01', 80, 98.50),   -- 100 recibidas - 20 reservadas en la orden #1 de ejemplo
(2, 'LOT-SOJA-2026-02', 250, 95.00),
(1, 'LOT-MAIZ-2026-03', 40, 96.20),
(3, 'LOT-GIRA-2026-04', 60, 92.80);

INSERT INTO ordenes_preventa (productor_id, estado) VALUES
(1, 'RESERVADO');

INSERT INTO detalles_orden (orden_id, lote_id, cantidad_bolsas, precio_unitario_usd) VALUES
(1, 1, 20, 185.00);

-- 4. CONSULTA DE VERIFICACIÓN (JOIN)
SELECT 
    o.orden_id AS 'N° Orden',
    p.razon_social AS 'Productor',
    hn.codigo_variedad AS 'Híbrido Nidera',
    l.numero_lote AS 'N° Lote',
    d.cantidad_bolsas AS 'Bolsas Reservadas',
    d.precio_unitario_usd AS 'Precio Unit. (USD)',
    (d.cantidad_bolsas * d.precio_unitario_usd) AS 'Subtotal USD',
    o.estado AS 'Estado'
FROM ordenes_preventa o
JOIN productores p ON o.productor_id = p.productor_id
JOIN detalles_orden d ON o.orden_id = d.orden_id
JOIN lotes_insumo l ON d.lote_id = l.lote_id
JOIN hibridos_nidera hn ON l.hibrido_id = hn.hibrido_id;