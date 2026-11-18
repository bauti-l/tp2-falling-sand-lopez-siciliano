package fallingsand;

import java.util.List;

public interface Motor {
    int ancho();

    int alto();

    List<String> materiales();

    void pintar(String material, int radio, int x0, int y0, int x1, int y1);

    void alternarPausa();

    void avanzarUnPaso();

    void limpiar();

    void pulso();

    Cuadro cuadro();

    void detener();
}