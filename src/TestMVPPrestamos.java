import com.prestamos.dao.*;
import com.prestamos.modelo.*;
import com.prestamos.util.ValidacionesNegocio;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Programa de prueba MVP para demostrar la funcionalidad completa
 * del sistema de gestión de artículos y préstamos.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-23
 */
public class TestMVPPrestamos {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║     SISTEMA DE GESTIÓN DE CASA DE EMPEÑO - MVP              ║");
        System.out.println("║     Prueba de Artículos y Préstamos                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // Instanciar DAOs
        ArticuloDAO articuloDAO = new ArticuloDAO();
        PrestamoDAO prestamoDAO = new PrestamoDAO();
        ClienteDAO clienteDAO = new ClienteDAO();

        try {
            // ========================================================
            // PARTE 1: GESTIÓN DE ARTÍCULOS
            // ========================================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PARTE 1: GESTIÓN DE ARTÍCULOS");
            System.out.println("=".repeat(60) + "\n");

            // Crear un artículo de prueba
            System.out.println("📦 CREANDO ARTÍCULO DE PRUEBA...\n");

            Articulo articulo = new Articulo();
            articulo.setIdArticulo(articuloDAO.obtenerSiguienteId());
            articulo.setTipoArticulo("ELECTRODOMESTICO");
            articulo.setDescripcion("Laptop Dell Inspiron 15, Intel i7, 16GB RAM");
            articulo.setEstado("OPTIMO");
            articulo.setValorTasado(8000.0);
            articulo.setPrecioMercadoBase(10000.0);
            articulo.setPorcentajeTasacion(80.0);
            articulo.setFechaAvaluo(new Date());

            boolean articuloCreado = articuloDAO.insertarArticulo(articulo);

            if (articuloCreado) {
                System.out.println("✅ Artículo creado exitosamente!");
                System.out.println("   ID: " + articulo.getIdArticulo());
                System.out.println("   Tipo: " + articulo.getTipoArticulo());
                System.out.println("   Estado: " + articulo.getEstado());
                System.out.println("   Valor Tasado: $" + articulo.getValorTasado());
            }

            // Listar artículos disponibles
            System.out.println("\n📋 LISTANDO ARTÍCULOS DISPONIBLES...\n");
            List<Articulo> articulosDisponibles = articuloDAO.listarArticulosDisponibles();

            System.out.println("Artículos disponibles para préstamo: " + articulosDisponibles.size());
            for (Articulo art : articulosDisponibles) {
                System.out.println("  • ID: " + art.getIdArticulo() +
                                 " | Tipo: " + art.getTipoArticulo() +
                                 " | Estado: " + art.getEstado() +
                                 " | Valor: $" + art.getValorTasado());
            }

            // ========================================================
            // PARTE 2: VALIDACIONES DE NEGOCIO
            // ========================================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PARTE 2: VALIDACIONES DE NEGOCIO");
            System.out.println("=".repeat(60) + "\n");

            System.out.println("🔍 VALIDANDO ARTÍCULO PARA PRÉSTAMO...\n");
            boolean articuloValido = ValidacionesNegocio.validarArticuloParaPrestamo(articulo);
            System.out.println("   Resultado: " + (articuloValido ? " VÁLIDO" : " NO VÁLIDO"));

            System.out.println("\n💰 CALCULANDO MONTO MÁXIMO PERMITIDO...\n");
            double montoMaximo = ValidacionesNegocio.calcularMontoMaximo(articulo.getValorTasado());
            System.out.println("   Valor del artículo: $" + articulo.getValorTasado());
            System.out.println("   Monto máximo (80%): $" + montoMaximo);

            System.out.println("\n📅 CALCULANDO TASA DE INTERÉS...\n");
            Date fechaInicio = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(fechaInicio);
            cal.add(Calendar.MONTH, 4); // 4 meses
            Date fechaVencimiento = cal.getTime();

            double tasaInteres = ValidacionesNegocio.calcularTasaInteres(fechaInicio, fechaVencimiento);
            System.out.println("   Plazo: 4 meses (120 días)");
            System.out.println("   Tasa de interés: " + (tasaInteres * 100) + "%");

            double montoSolicitado = 6000.0;
            double interesGenerado = ValidacionesNegocio.calcularInteresGenerado(montoSolicitado, tasaInteres);
            System.out.println("   Monto solicitado: $" + montoSolicitado);
            System.out.println("   Interés generado: $" + interesGenerado);
            System.out.println("   Total a pagar: $" + (montoSolicitado + interesGenerado));

            // ========================================================
            // PARTE 3: GESTIÓN DE PRÉSTAMOS
            // ========================================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PARTE 3: GESTIÓN DE PRÉSTAMOS");
            System.out.println("=".repeat(60) + "\n");

            // Verificar que existe un cliente de prueba
            System.out.println("👤 VERIFICANDO CLIENTE...\n");
            Cliente cliente = clienteDAO.obtenerClientePorId(1);

            if (cliente != null) {
                System.out.println("   Cliente encontrado:");
                System.out.println("   ID: " + cliente.getIdPersona());
                System.out.println("   Nombre: " + cliente.getNombrePersona());
                System.out.println("   Calificación: " + cliente.getCalificacion());

                // Crear préstamo
                System.out.println("\n💳 CREANDO PRÉSTAMO...\n");

                Prestamo prestamo = new Prestamo();
                prestamo.setIdPrestamo(prestamoDAO.obtenerSiguienteId());
                prestamo.setIdCliente(cliente.getIdCliente());
                prestamo.setIdArticulo(articulo.getIdArticulo());
                prestamo.setIdAsesor(1); // Asesor de prueba
                prestamo.setMonto(montoSolicitado);
                prestamo.setFechaPrestamo(fechaInicio);
                prestamo.setFechaVencimiento(fechaVencimiento);

                boolean prestamoCreado = prestamoDAO.crearPrestamo(prestamo);

                if (prestamoCreado) {
                    System.out.println("✅ Préstamo creado exitosamente!");
                    System.out.println("   ID Préstamo: " + prestamo.getIdPrestamo());
                    System.out.println("   Cliente: " + cliente.getNombrePersona());
                    System.out.println("   Monto: $" + prestamo.getMonto());
                    System.out.println("   Fecha Inicio: " + sdf.format(prestamo.getFechaPrestamo()));
                    System.out.println("   Fecha Vencimiento: " + sdf.format(prestamo.getFechaVencimiento()));
                    System.out.println("   Estado: ACTIVO");

                    // Listar préstamos del cliente
                    System.out.println("\n📊 PRÉSTAMOS DEL CLIENTE...\n");
                    List<Prestamo> prestamosCliente = prestamoDAO.listarPrestamosPorCliente(cliente.getIdCliente());

                    System.out.println("Total de préstamos: " + prestamosCliente.size());
                    for (Prestamo p : prestamosCliente) {
                        double total = ValidacionesNegocio.calcularTotalAPagar(
                            p.getMonto(), p.getInteresGenerado(), p.getMulta()
                        );
                        System.out.println("  • ID: " + p.getIdPrestamo() +
                                         " | Monto: $" + p.getMonto() +
                                         " | Interés: $" + p.getInteresGenerado() +
                                         " | Total: $" + total +
                                         " | Estado: " + p.getEstadoPrestamo());
                    }
                }
            } else {
                System.out.println("⚠️  No se encontró cliente de prueba.");
                System.out.println("   Sugerencia: Crear un cliente primero usando ClienteDAO");
            }

            // ========================================================
            // PARTE 4: RESUMEN Y ESTADÍSTICAS
            // ========================================================
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PARTE 4: RESUMEN Y ESTADÍSTICAS");
            System.out.println("=".repeat(60) + "\n");

            List<Articulo> todosArticulos = articuloDAO.listarTodosArticulos();
            List<Prestamo> todosPrestamos = prestamoDAO.listarTodosPrestamos();
            List<Prestamo> prestamosActivos = prestamoDAO.listarPrestamosPorEstado("ACTIVO");

            System.out.println("📈 ESTADÍSTICAS DEL SISTEMA:");
            System.out.println("   Total de artículos: " + todosArticulos.size());
            System.out.println("   Artículos disponibles: " + articulosDisponibles.size());
            System.out.println("   Total de préstamos: " + todosPrestamos.size());
            System.out.println("   Préstamos activos: " + prestamosActivos.size());

            // Calcular totales
            double totalPrestado = 0;
            double totalIntereses = 0;
            for (Prestamo p : todosPrestamos) {
                totalPrestado += p.getMonto();
                totalIntereses += p.getInteresGenerado();
            }

            System.out.println("\n💵 INFORMACIÓN FINANCIERA:");
            System.out.println("   Capital prestado: $" + String.format("%.2f", totalPrestado));
            System.out.println("   Intereses generados: $" + String.format("%.2f", totalIntereses));
            System.out.println("   Total en circulación: $" + String.format("%.2f", (totalPrestado + totalIntereses)));

        } catch (Exception e) {
            System.err.println("\n❌ ERROR EN LA PRUEBA:");
            System.err.println("   " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ PRUEBA MVP COMPLETADA");
        System.out.println("=".repeat(60) + "\n");
    }
}

