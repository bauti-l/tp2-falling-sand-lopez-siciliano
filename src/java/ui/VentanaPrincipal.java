package fallingsand.ui;

import fallingsand.Cuadro;
import fallingsand.Motor;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public final class VentanaPrincipal {
    private static final String TITULO = "Falling Sand";
    private final Motor motor;
    private final Stage escenario;
    private final VistaGrilla grilla;
    private final PanelControles controles;
    private final Timeline reloj;

    public VentanaPrincipal(Motor motor) {
        this.motor = motor;
        this.controles = new PanelControles(motor);
        this.grilla = new VistaGrilla(motor, controles);
    }
}
