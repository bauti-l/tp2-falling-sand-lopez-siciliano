package fallingsand.ui;

import fallingsand.Cuadro;
import fallingsand.Motor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

final class VistaGrilla {
    private static final int LADO_CELDA = 5;
    private final Motor motor;
    private final PanelControles controles;
    private final int ancho;
    private final int alto;
    private final WritableImage imagen;
    private final Canvas lienzo;
    private final GraphicsContext contexto;
    private int columnaAnterior;
    private int filaAnterior;

    VistaGrilla(Motor motor, PanelControles controles) {
        this.motor = motor;
        this.controles = controles;
        this.ancho = motor.ancho();
        this.alto = motor.alto();
        this.imagen = new WritableImage(ancho, alto);
        this.lienzo = new Canvas(ancho * LADO_CELDA, alto * LADO_CELDA);
        this.contexto = lienzo.getGraphicsContext2D();
        contexto.setImageSmoothing(false);
        lienzo.setOnMousePressed(this::alPresionar);
        lienzo.setOnMouseDragged(this::alArrastrar);
    }

    Node nodo() {
        return lienzo;
    }

    void dibujar(Cuadro cuadro) {
        imagen.getPixelWriter().setPixels(
                0, 0, ancho, alto, PixelFormat.getIntArgbInstance(), cuadro.argb(), 0, ancho);
        contexto.drawImage(imagen, 0, 0, lienzo.getWidth(), lienzo.getHeight());
    }

    private void alPresionar(MouseEvent evento) {
        if (evento.getButton() != MouseButton.PRIMARY) {
            return;
        }
        columnaAnterior = celda(evento.getX());
        filaAnterior = celda(evento.getY());
        trazarHasta(columnaAnterior, filaAnterior);
    }

    private void alArrastrar(MouseEvent evento) {
        if (evento.isPrimaryButtonDown()) {
            trazarHasta(celda(evento.getX()), celda(evento.getY()));
        }
    }

    private void trazarHasta(int columna, int fila) {
        motor.pintar(controles.materialSeleccionado(), controles.radio(),
                columnaAnterior, filaAnterior, columna, fila);
        columnaAnterior = columna;
        filaAnterior = fila;
    }

    private static int celda(double pixel) {
        return (int) Math.floor(pixel / LADO_CELDA);
    }
}