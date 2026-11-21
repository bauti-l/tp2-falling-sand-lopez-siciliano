package fallingsand.ui;

import fallingsand.Cuadro;
import fallingsand.Motor;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

final class PanelControles {
    private static final double ANCHO = 200;
    static final double MARGEN = 10;
    private static final double SEPARACION = 10;
    private static final double SEPARACION_GRUPO = 2;
    private static final String ESTILO_INDICADORES = "-fx-font-size: 0.9em;";
    private static final int RADIO_MINIMO = 1;
    private static final int RADIO_MAXIMO = 12;
    private static final int RADIO_INICIAL = 3;

    private final ToggleGroup selector = new ToggleGroup();

    private final Slider deslizadorRadio =
            new Slider(RADIO_MINIMO, RADIO_MAXIMO, RADIO_INICIAL);

    private final Button botonPausa = new Button();

    private final Button botonPaso = new Button("Paso");

    private final Button botonLimpiar = new Button("Limpiar");

    private final Label etiquetaPaso = new Label();

    private final Label etiquetaParticulas = new Label();

    private final VBox nodo;

    PanelControles(Motor motor) {
        VBox opciones = new VBox(SEPARACION_GRUPO);
        for (String material : motor.materiales()) {
            ToggleButton opcion = new ToggleButton(material);
            opcion.setUserData(material);
            opcion.setToggleGroup(selector);
            opciones.getChildren().add(opcion);
        }
        selector.selectToggle(selector.getToggles().get(0));
        selector.selectedToggleProperty().addListener((propiedad, anterior, nuevo) -> {
            if (nuevo == null) {
                selector.selectToggle(anterior);
            }
        });

        deslizadorRadio.setMajorTickUnit(RADIO_MAXIMO - RADIO_MINIMO);
        deslizadorRadio.setMinorTickCount(0);
        deslizadorRadio.setShowTickMarks(true);
        deslizadorRadio.setShowTickLabels(true);
        deslizadorRadio.setBlockIncrement(1);
        deslizadorRadio.valueProperty().addListener((propiedad, anterior, nuevo) ->
                deslizadorRadio.setValue(Math.round(nuevo.doubleValue())));

        botonPausa.setOnAction(evento -> motor.alternarPausa());
        botonPaso.setOnAction(evento -> motor.avanzarUnPaso());
        botonLimpiar.setOnAction(evento -> motor.limpiar());

        VBox indicadores = new VBox(SEPARACION_GRUPO, etiquetaPaso, etiquetaParticulas);
        indicadores.setStyle(ESTILO_INDICADORES);

        nodo = new VBox(SEPARACION,
                new Label("Material"), opciones,
                new Label("Radio"), deslizadorRadio,
                botonPausa, botonPaso, botonLimpiar,
                indicadores);
        nodo.setPadding(new Insets(MARGEN, MARGEN, MARGEN, 0));
        nodo.setPrefWidth(ANCHO);
    }

    Node nodo() {
        return nodo;
    }

    String materialSeleccionado() {
        return (String) selector.getSelectedToggle().getUserData();
    }

    int radio() {
        return (int) Math.round(deslizadorRadio.getValue());
    }

    void actualizar(Cuadro cuadro) {
        botonPausa.setText(cuadro.enPausa() ? "Reanudar" : "Pausa");
        botonPaso.setDisable(!cuadro.enPausa());
        etiquetaPaso.setText("paso: " + cuadro.paso());
        etiquetaParticulas.setText("partículas: " + cuadro.particulas());
    }
}