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
        this.reloj = new Timeline(new KeyFrame(Duration.seconds(1.0 / 60), evento -> alPulso()));
        reloj.setCycleCount(Animation.INDEFINITE);
        this.escenario = new Stage();
        escenario.setTitle(TITULO);
        HBox.setMargin(grilla.nodo(), new Insets(PanelControles.MARGEN));
        HBox raiz = new HBox(grilla.nodo(), controles.nodo());
        raiz.setAlignment(Pos.CENTER_LEFT);
        escenario.setScene(new Scene(raiz));
        escenario.setResizable(false);
        escenario.setOnHidden(evento -> alCerrar());
        dibujar(motor.cuadro());
    }

    public void mostrar() {
        escenario.show();
        reloj.play();
    }

    private void alPulso() {
        motor.pulso();
        dibujar(motor.cuadro());
    }

    private void dibujar(Cuadro cuadro) {
        grilla.dibujar(cuadro);
        controles.actualizar(cuadro);
    }

    private void alCerrar() {
        reloj.stop();
        motor.detener();
        Platform.runLater(Platform::exit);
    }
}