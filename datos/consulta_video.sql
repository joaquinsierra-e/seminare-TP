SELECT o.orden_id, o.estado, p.razon_social, l.numero_lote, d.cantidad_bolsas,
       l.bolsas_disponibles, h.codigo_variedad, h.precio_usd
FROM seminare_db.ordenes_preventa o
JOIN seminare_db.productores p     ON p.productor_id = o.productor_id
JOIN seminare_db.detalles_orden d  ON d.orden_id = o.orden_id
JOIN seminare_db.lotes_insumo l    ON l.lote_id = d.lote_id
JOIN seminare_db.hibridos_nidera h ON h.hibrido_id = l.hibrido_id
ORDER BY o.orden_id;
