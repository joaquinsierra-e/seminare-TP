package src.dao.memoria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;
import src.modelo.LoteInsumo;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;
import src.modelo.Productor;

/**
 * "Base de datos" en memoria para ejecutar el prototipo sin MySQL (demostración / pruebas).
 * Los datos de ejemplo son equivalentes a los de database/script_seminare.sql (sin la orden de ejemplo).
 */
public class AlmacenMemoria {

    final List<Productor> productores = new ArrayList<>();
    final List<HibridoNidera> hibridos = new ArrayList<>();
    final List<LoteInsumo> lotes = new ArrayList<>();
    final List<OrdenPreventa> ordenes = new ArrayList<>();
    final List<OrdenDespacho> despachos = new ArrayList<>();

    private int sigProductor = 1;
    private int sigLote = 1;
    private int sigOrden = 1;
    private int sigDespacho = 1;

    int nuevoIdProductor() {
        return sigProductor++;
    }

    int nuevoIdLote() {
        return sigLote++;
    }

    int nuevoIdOrden() {
        return sigOrden++;
    }

    int nuevoIdDespacho() {
        return sigDespacho++;
    }

    public static AlmacenMemoria conDatosDemo() {
        AlmacenMemoria a = new AlmacenMemoria();
        try {
            Productor p1 = new Productor(a.nuevoIdProductor(), "30-71123456-6", "Agropecuaria El Imperio S.A.", "Río Cuarto", "0358-4640000");
            Productor p2 = new Productor(a.nuevoIdProductor(), "30-65432109-0", "Estancia Las Lilas SRL", "Higueras", "0358-4651122");
            a.productores.add(p1);
            a.productores.add(p2);

            HibridoNidera maiz = HibridoNidera.crear(1, "AX 7784 VT3P", "Maíz", "Acceleron Premium", new BigDecimal("185.00"));
            HibridoNidera soja = HibridoNidera.crear(2, "NS 5025 VIP3", "Soja", "Plenus completo", new BigDecimal("62.00"));
            HibridoNidera girasol = HibridoNidera.crear(3, "NIDERA SOL 340 CL", "Girasol", "Curasemilla estándar", new BigDecimal("98.50"));
            a.hibridos.add(maiz);
            a.hibridos.add(soja);
            a.hibridos.add(girasol);

            a.lotes.add(new LoteInsumo(a.nuevoIdLote(), "LOT-MAIZ-2026-01", maiz, 100, 98.50));
            a.lotes.add(new LoteInsumo(a.nuevoIdLote(), "LOT-SOJA-2026-02", soja, 250, 95.00));
            a.lotes.add(new LoteInsumo(a.nuevoIdLote(), "LOT-MAIZ-2026-03", maiz, 40, 96.20));
            a.lotes.add(new LoteInsumo(a.nuevoIdLote(), "LOT-GIRA-2026-04", girasol, 60, 92.80));
        } catch (SeminareException e) {
            throw new IllegalStateException("Los datos de demostración deben ser válidos", e);
        }
        return a;
    }
}
