import javax.swing.*;
import com.prestamos.vista.FrmLogin;

/**
 * Clase principal para iniciar el Sistema de Préstamos.
 * Punto de entrada de la aplicación.
 *
 * @author Sistema de Préstamos
 * @version 1.0
 * @since 2025-11-22
 */
public class Main {

    /**
     * Método principal que inicia la aplicación.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Configurar Look and Feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("✓ Look and Feel del sistema configurado");
        } catch (Exception e) {
            System.err.println("⚠ No se pudo configurar el Look and Feel del sistema");
            e.printStackTrace();
        }

        // Iniciar aplicación en Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
            System.out.println("║          SISTEMA DE PRÉSTAMOS - CASA DE EMPEÑO              ║");
            System.out.println("║                     Versión 1.0                              ║");
            System.out.println("╚════════════════════════════════════════════════════════════════╝");
            System.out.println("\n→ Iniciando aplicación...");

            FrmLogin login = new FrmLogin();
            login.setVisible(true);

            System.out.println("✓ Formulario de login inicializado");
            System.out.println("→ Sistema listo para usar\n");
        });
    }
}

