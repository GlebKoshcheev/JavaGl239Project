package app;

import Misc.Misc;
import Misc.CoordinateSystem2i;
import io.github.humbleui.jwm.*;
import io.github.humbleui.jwm.skija.EventFrameSkija;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.RRect;
import io.github.humbleui.skija.Surface;

import java.io.File;
import java.util.function.Consumer;

import static app.Colors.APP_BACKGROUND_COLOR;

/**
 * Класс окна приложения
 */
public class Application implements Consumer<Event> {
    /**
     * окно приложения
     */
    private final Window window;

    /**
     * Конструктор окна приложения
     */
    public Application(){
        // создаём окно
        window = App.makeWindow();

        // задаём обработчиком событий текущий объект
        window.setEventListener(this);

        // задаём заголовок окна
        window.setTitle("window");

        // задаём размер окна
        window.setWindowSize(900, 900);

        // задаём положение окна
        window.setWindowPosition(100, 100);

        // задаём иконку
        switch (Platform.CURRENT) {
            case WINDOWS -> window.setIcon(new File("src/main/resources/windows.ico"));
            case MACOS -> window.setIcon(new File("src/main/resources/macos.icns"));
        }

        // названия слоёв, которые будем перебирать
        String[] layerNames = new String[]{
                "LayerGLSkija", "LayerRasterSkija"
        };

        // перебираем слои
        for (String layerName : layerNames) {
            String className = "io.github.humbleui.jwm.skija." + layerName;
            try {
                Layer layer = (Layer) Class.forName(className).getDeclaredConstructor().newInstance();
                window.setLayer(layer);
                break;
            } catch (Exception e) {
                System.out.println("Ошибка создания слоя " + className);
            }
        }

        // если окну не присвоен ни один из слоёв
        if (window._layer == null)
            throw new RuntimeException("Нет доступных слоёв для создания");

        // делаем окно видимым
        window.setVisible(true);
    }

    /**
     * Рисование
     *
     * @param canvas   низкоуровневый инструмент рисования примитивов от Skija
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // запоминаем изменения (пока что там просто заливка цветом)
        canvas.save();

        // очищаем канвас
        canvas.clear(APP_BACKGROUND_COLOR);

        // координаты левого верхнего края окна
        int rX = windowCS.getSize().x / 3;
        int rY = windowCS.getSize().y / 3;

        // ширина и высота
        int rWidth =  windowCS.getSize().x  / 3;
        int rHeight = windowCS.getSize().y  / 3;

        // создаём кисть
        Paint paint = new Paint();

        // задаём цвет рисования
        paint.setColor(Misc.getColor(100, 255, 255, 255));

        // рисуем квадрат
        canvas.drawRRect(RRect.makeXYWH(rX, rY, rWidth, rHeight, 4), paint);

        // восстанавливаем состояние канваса
        canvas.restore();
    }

    /**
     * Обработчик событий
     *
     * @param event событие
     */
    @Override
    public void accept(Event event) {
        // если событие - это закрытие окна
        if(event instanceof EventWindowClose){
            // завершаем работу приложения
            App.terminate();
        }else if (event instanceof EventWindowCloseRequest) {
            window.close();
        }else if (event instanceof EventFrameSkija ee) {
            Surface s = ee.getSurface();
            paint(s.getCanvas(), new CoordinateSystem2i(s.getWidth(), s.getHeight()));
        }
    }
}
