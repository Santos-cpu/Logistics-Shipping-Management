package com.logistica;

import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Controller {

    private static final String AYUDA_DEFECTO = "Selecciona un estado para ver su descripción operativa.";

    @FXML
    private TextField txtReferencia;

    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private Label lblAyudaEstado;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnGuardar;

    @FXML
    private Label lblMensaje;

    @FXML
    private Label lblContador;

    @FXML
    private ListView<String> lstEnvios;

    private final ObservableList<String> envios = FXCollections.observableArrayList();
    private final Set<String> referenciasRegistradas = new HashSet<>();

    @FXML
    public void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList(
                "Pendiente",
                "En preparación",
                "En tránsito",
                "Entregado",
                "Incidencia"
        ));

        lstEnvios.setItems(envios);

        // Evento reactivo al cambiar el estado en el ComboBox (RA 1)
        cmbEstado.valueProperty().addListener((obs, valorAnterior, nuevoValor) -> {
            actualizarAyudaEstado(nuevoValor);
            if (nuevoValor != null && lblMensaje.getStyleClass().contains("feedback-error")) {
                ocultarMensaje();
            }
        });

        // Mejora de usabilidad con teclado: abrir/cerrar el ComboBox con Espacio (RA 4)
        cmbEstado.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE) {
                if (!cmbEstado.isShowing()) {
                    cmbEstado.show();
                } else {
                    cmbEstado.hide();
                }
                event.consume();
            }
        });

        // Oculta errores previos en cuanto el usuario empieza a corregir la referencia
        txtReferencia.textProperty().addListener((obs, valorAnterior, nuevoValor) -> {
            if (lblMensaje.getStyleClass().contains("feedback-error")) {
                ocultarMensaje();
            }
        });

        // Registro de atajos manteniendo Alt pulsado + foco inicial (RA 4)
        Platform.runLater(() -> {
            registrarAtajosTeclado(txtReferencia.getScene());
            txtReferencia.requestFocus();
        });
    }

    private void registrarAtajosTeclado(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isAltDown() && !event.isControlDown() && !event.isShiftDown() && !event.isMetaDown()) {
                switch (event.getCode()) {
                    case R -> {
                        cmbEstado.hide();
                        txtReferencia.requestFocus();
                        txtReferencia.selectAll();
                        event.consume();
                    }
                    case E -> {
                        cmbEstado.requestFocus();
                        if (!cmbEstado.isShowing()) {
                            cmbEstado.show();
                        }
                        event.consume();
                    }
                    case G -> {
                        cmbEstado.hide();
                        handleGuardar();
                        event.consume();
                    }
                    case L -> {
                        cmbEstado.hide();
                        handleLimpiar();
                        event.consume();
                    }
                    default -> {
                    }
                }
            }
        });
    }

    @FXML
    private void handleGuardar() {
        String referencia = txtReferencia.getText() == null ? "" : txtReferencia.getText().trim().toUpperCase();
        String estado = cmbEstado.getValue();

        if (referencia.isEmpty()) {
            mostrarMensaje("Error: introduce una referencia de envío antes de guardar.", true);
            txtReferencia.requestFocus();
            return;
        }

        if (referenciasRegistradas.contains(referencia)) {
            mostrarMensaje("Error: la referencia " + referencia + " ya está registrada.", true);
            txtReferencia.requestFocus();
            txtReferencia.selectAll();
            return;
        }

        if (estado == null || estado.isBlank()) {
            mostrarMensaje("Error: selecciona un estado para el envío.", true);
            cmbEstado.requestFocus();
            return;
        }

        String registro = referencia + "  —  [" + estado + "]";
        envios.add(0, registro);
        referenciasRegistradas.add(referencia);
        actualizarContador();

        txtReferencia.clear();
        cmbEstado.setValue(null);
        mostrarMensaje("Envío " + referencia + " registrado correctamente.", false);
        txtReferencia.requestFocus();
    }

    @FXML
    private void handleLimpiar() {
        txtReferencia.clear();
        cmbEstado.setValue(null);
        lstEnvios.getSelectionModel().clearSelection();
        lblAyudaEstado.setText(AYUDA_DEFECTO);
        ocultarMensaje();
        txtReferencia.requestFocus();
    }

    private void actualizarAyudaEstado(String estado) {
        if (estado == null) {
            lblAyudaEstado.setText(AYUDA_DEFECTO);
            return;
        }

        String descripcion = switch (estado) {
            case "Pendiente" -> "Pendiente: orden recibida, a la espera de asignación en almacén.";
            case "En preparación" -> "En preparación: mercancía en proceso de empaquetado y etiquetado.";
            case "En tránsito" -> "En tránsito: expedición en ruta hacia el destino final.";
            case "Entregado" -> "Entregado: paquete recepcionado y confirmado por el destinatario.";
            case "Incidencia" -> "Incidencia: requiere revisión logística o contacto con el cliente.";
            default -> AYUDA_DEFECTO;
        };
        lblAyudaEstado.setText(descripcion);
    }

    private void mostrarMensaje(String texto, boolean esError) {
        lblMensaje.setText(texto);
        lblMensaje.getStyleClass().removeAll("feedback-success", "feedback-error");
        lblMensaje.getStyleClass().add(esError ? "feedback-error" : "feedback-success");
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void ocultarMensaje() {
        lblMensaje.setText("");
        lblMensaje.getStyleClass().removeAll("feedback-success", "feedback-error");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }

    private void actualizarContador() {
        int total = envios.size();
        lblContador.setText(total == 1 ? "1 envío" : total + " envíos");
    }
}