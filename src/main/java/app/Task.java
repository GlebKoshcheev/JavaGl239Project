package app;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dialogs.PanelInfo;
import io.github.humbleui.jwm.MouseButton;
import io.github.humbleui.skija.*;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;
import misc.Vector2i;
import panels.PanelLog;

import java.util.ArrayList;

import static app.Colors.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Класс задачи
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class Task {
    /**
     * Текст задачи
     */
    public static final String TASK_TEXT = """
            ПОСТАНОВКА ЗАДАЧИ:
            На плоскости задано множество точек, и "параллельный"
            прямоугольник. Множество точек образует все
            возможные прямые, которые могут быть построены
            парами точек множества. Найти такую прямую (и
            такие две точки, через которые она проходит), что
            эта прямая пересекает указанный прямоугольник,и при
            этом длина отрезка прямой, находящейся внутри
            прямоугольника, максимальна. В качестве ответа:
            выделить найденные две точки, нарисовать прямую,
            которая через них проходит, а также выделить на этой
            прямой отрезок между двумя найденными точками
            пересечения.""";

    /**
     *  коэффициент колёсика мыши
     */
    private static final float WHEEL_SENSITIVE = 0.001f;

    /**
     * Вещественная система координат задачи
     */
    private final CoordinateSystem2d ownCS;
    /**
     * Список точек, через которые мы проводим прямые
     */
    private final ArrayList<Point> points;
    /**
     * Список вершин прямоугольника
     */
    private final ArrayList<Point> tops;
    /**
     * Список точек, являющихся решением нашей задачи
     */
    private final ArrayList<Point> sPoints;
    /**
     * Список точек пересечения прямой и прямоугольника
     */
    private final ArrayList<Vector2d> crossPoints;
    /**
     * Размер точки
     */
    private static final int POINT_SIZE = 3;
    /**
     * Последняя СК окна
     */
    private CoordinateSystem2i lastWindowCS;
    /**
     * Флаг, решена ли задача
     */
    private boolean solved;
    /**
     * Флаг задан ли прямоугольник
     */
    public boolean rectangle;
    /**
     * Порядок разделителя сетки, т.е. раз в сколько отсечек
     * будет нарисована увеличенная
     */
    private static final int DELIMITER_ORDER = 10;

    /**
     * Задача
     *
     * @param ownCS  СК задачи
     * @param points массив точек
     * @param tops вершины прямоугольника
     */
    @JsonCreator
    public Task(@JsonProperty("ownCS") CoordinateSystem2d ownCS, @JsonProperty("points") ArrayList<Point> points,
                @JsonProperty("rect") ArrayList<Point> tops) {
        this.ownCS = ownCS;
        this.points = points;
        this.tops = tops;
        this.sPoints = new ArrayList<>();
        this.crossPoints = new ArrayList<>();
    }

    /**
     * Рисование
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void paint(Canvas canvas, CoordinateSystem2i windowCS) {
        // Сохраняем последнюю СК
        lastWindowCS = windowCS;
        // рисуем координатную сетку
        renderGrid(canvas, lastWindowCS);
        // рисуем задачу
        renderTask(canvas, windowCS);
    }

    /**
     * Рисование задачи
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    private void renderTask(Canvas canvas, CoordinateSystem2i windowCS) {
        canvas.save();
        // создаём перо
        try (var paint = new Paint()) {
                for (Point p : points) {
                    if (!solved) {
                        paint.setColor(p.getColor());
                    }else{
                        if(sPoints.contains(p)){
                            paint.setColor(S_POINTS_COLOR);
                        }else {
                            paint.setColor(p.getColor());
                        }
                    }
                    // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                    // а в классическом представлении - вверх
                    Vector2i windowPos = windowCS.getCoords(p.pos.x, -p.pos.y, ownCS);
                    // рисуем точку
                    canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE,
                        POINT_SIZE * 2, POINT_SIZE * 2), paint);
                }
                if(tops.size() == 2) {
                Point pointA = tops.get(0);
                Point pointC = tops.get(1);

                Vector2i posA = windowCS.getCoords(pointA.getPos().x, -pointA.getPos().y, ownCS);
                Vector2i posC = windowCS.getCoords(pointC.getPos().x, -pointC.getPos().y, ownCS);

                //выбираем цвет пера
                paint.setColor(RECTANGLE_COLOR);

                //рисуем прямоугольник
                canvas.drawLine(posA.x, posA.y, posA.x, posC.y, paint);
                canvas.drawLine(posA.x, posC.y, posC.x, posC.y, paint);
                canvas.drawLine(posC.x, posC.y, posC.x, posA.y, paint);
                canvas.drawLine(posC.x, posA.y, posA.x, posA.y, paint);

                rectangle = true;
            }
            for (Point p : tops){
                paint.setColor(TOPS_COLOR);
                // y-координату разворачиваем, потому что у СК окна ось y направлена вниз,
                // а в классическом представлении - вверх
                Vector2i windowPos = windowCS.getCoords(p.pos.x, -p.pos.y, ownCS);
                // рисуем точку
                canvas.drawRect(Rect.makeXYWH(windowPos.x - POINT_SIZE, windowPos.y - POINT_SIZE, POINT_SIZE * 2, POINT_SIZE * 2), paint);
            }

            // проводим прямую и выделяем точки пересечения прямой и прямоугольника
            if(sPoints.size() == 2){
                Point point1 = sPoints.get(0);
                Point point2 = sPoints.get(1);
                Vector2i pointA = windowCS.getCoords(point1.getPos().x, -point1.getPos().y, ownCS);
                Vector2i pointB = windowCS.getCoords(point2.getPos().x, -point2.getPos().y, ownCS);

                // вектор, ведущий из точки A в точку B
                Vector2i delta = Vector2i.subtract(pointA, pointB);
                // получаем максимальную длину отрезка на экране, как длину диагонали экрана
                int maxDistance = (int) windowCS.getSize().length();
                // получаем новые точки для рисования, которые гарантируют, что линия
                // будет нарисована до границ экрана
                Vector2i renderPointA = Vector2i.sum(pointA, Vector2i.mult(delta, maxDistance));
                Vector2i renderPointB = Vector2i.sum(pointA, Vector2i.mult(delta, -maxDistance));
                // рисуем линию
                paint.setColor(FIELD_BACKGROUND_COLOR);
                canvas.drawLine(renderPointA.x, renderPointA.y, renderPointB.x, renderPointB.y, paint);

                // выделяем отрезок, образованный точками пересечения прямой и прямоугольника
                Vector2i crossPointA = windowCS.getCoords(crossPoints.get(0).x, -crossPoints.get(0).y, ownCS);
                Vector2i crossPointB = windowCS.getCoords(crossPoints.get(1).x, -crossPoints.get(1).y, ownCS);
                paint.setStrokeWidth(3);
                canvas.drawLine(crossPointA.x, crossPointA.y, crossPointB.x, crossPointB.y, paint);
            }
        }
        canvas.restore();
    }

    /**
     * Добавить точку
     *
     * @param pos      положение
     */
    public void addPoint(Vector2d pos) {
        solved = false;
        Point newPoint = new Point(pos);
        points.add(newPoint);
        PanelLog.info("точка " + newPoint + " добавлена во " + newPoint.getSetName());
    }

    /**
     * Добавить вершину прямоугольника
     *
     * @param pos      положение
     */
    public void addTop(Vector2d pos) {
        solved = false;
        Point newPoint = new Point(pos);
        if(tops.size() == 1){
            Point PointA = tops.get(0);
            if( PointA.getPos().x == pos.x || PointA.getPos().y == pos.y){
                PanelLog.warning("точка " + newPoint + " не является противоположной вершиной прямоугольника");
                return;
            }
        }
        if(tops.size() == 2){
            PanelLog.warning("Прямоугольник уже был задан при помощи 2 противоположных вершин");
            return;
        }
        tops.add(newPoint);
        PanelLog.info("точка " + newPoint + " установлена вершиной прямоугольника");
    }

    /**
     * Клик мыши по пространству задачи
     *
     * @param pos         положение мыши
     * @param mouseButton кнопка мыши
     */
    public void click(Vector2i pos, MouseButton mouseButton) {
        if (lastWindowCS == null) return;
        // получаем положение на экране
        Vector2d taskPos = ownCS.getCoords(pos, lastWindowCS);
        if (mouseButton.equals(MouseButton.PRIMARY) || mouseButton.equals(MouseButton.SECONDARY)) {
            addPoint(taskPos);
        }
    }


    /**
     * Добавить случайные точки
     *
     * @param cnt кол-во случайных точек
     */
    public void addRandomPoints(int cnt) {
        CoordinateSystem2i addGrid = new CoordinateSystem2i(30, 30);

        for (int i = 0; i < cnt; i++) {
            Vector2i gridPos = addGrid.getRandomCoords();
            Vector2d pos = ownCS.getCoords(gridPos, addGrid);
            // сработает примерно в половине случаев
            addPoint(pos);
        }
    }


    /**
     * Рисование сетки
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    public void renderGrid(Canvas canvas, CoordinateSystem2i windowCS) {
        // сохраняем область рисования
        canvas.save();
        // получаем ширину штриха(т.е. по факту толщину линии)
        float strokeWidth = 0.03f / (float) ownCS.getSimilarity(windowCS).y + 0.5f;
        // создаём перо соответствующей толщины
        try (var paint = new Paint().setMode(PaintMode.STROKE).setStrokeWidth(strokeWidth).setColor(TASK_GRID_COLOR)) {
            // перебираем все целочисленные отсчёты нашей СК по оси X
            for (int i = (int) (ownCS.getMin().x); i <= (int) (ownCS.getMax().x); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(i, 0, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % DELIMITER_ORDER == 0 ? 5 : 2;
                // рисуем вертикальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y + strokeHeight, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x, windowPos.y - strokeHeight, paint);
            }
            // перебираем все целочисленные отсчёты нашей СК по оси Y
            for (int i = (int) (ownCS.getMin().y); i <= (int) (ownCS.getMax().y); i++) {
                // находим положение этих штрихов на экране
                Vector2i windowPos = windowCS.getCoords(0, i, ownCS);
                // каждый 10 штрих увеличенного размера
                float strokeHeight = i % 10 == 0 ? 5 : 2;
                // рисуем горизонтальный штрих
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x + strokeHeight, windowPos.y, paint);
                canvas.drawLine(windowPos.x, windowPos.y, windowPos.x - strokeHeight, windowPos.y, paint);
            }
        }
        // восстанавливаем область рисования
        canvas.restore();
    }

    /**
     * Очистить задачу
     */
    public void clear() {
        points.clear();
        tops.clear();
        sPoints.clear();
        solved = false;
        rectangle = false;
    }

    /**
     * Решить задачу
     */
    public void solve() {
        if(tops.isEmpty() || tops.size() == 1){
            PanelLog.error("Задача не может быть решена, потому что прямоугольник не был задан");
            rectangle = false;
        }else if(points.size() >= 2){
            rectangle = true;
            // k - коэффициент наклона прямой
            double k;
            // c - свободный член
            double c;
            // создаем переменную, в которую мы будем записывать длины отрезков, расположенных внутри прямоугольника
            double lengthMax = -1;
            double length = -1;

            double _top = max(tops.get(0).getPos().y, tops.get(1).getPos().y);
            double _bottom = min(tops.get(0).getPos().y, tops.get(1).getPos().y);
            double _right = max(tops.get(0).getPos().x, tops.get(1).getPos().x);
            double _left = min(tops.get(0).getPos().x, tops.get(1).getPos().x);

            Point aMax = null;
            Point bMax = null;
            Vector2d crossA = null;
            Vector2d crossB = null;
            Vector2d crossAmax = null;
            Vector2d crossBmax = null;

            // перебираем пары точек
            for (int i = 0; i < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                    // сохраняем точки
                    Point a = points.get(i);
                    Point b = points.get(j);

                    // случай, когда прямая параллельна ординате
                    if (a.getPos().x == b.getPos().x) {
                        // проверяем, пересекает ли прямая прямоугольник
                        if (a.getPos().x >= _left && a.getPos().x <= _right) {
                            length = _top - _bottom;
                            crossA = new Vector2d(a.getPos().x, _bottom);
                            crossB = new Vector2d(a.getPos().x, _top);
                        }
                    } else {
                        k = (a.getPos().y - b.getPos().y) / (a.getPos().x - b.getPos().x);
                        // ищем коэффициент наклона прямой и свободный член
                        c = a.getPos().y - a.getPos().x * k;

                        // ищем точку пересечения прямой и нашего прямоугольника

                        // случай, когда прямая параллельна абсциссе
                        if (k == 0) {
                            if (a.getPos().y >= _bottom && a.getPos().y <= _top) {
                                length = _right - _left;
                                crossA = new Vector2d(_left, a.getPos().y);
                                crossB = new Vector2d(_right, a.getPos().y);
                            }
                        } else {
                            double yLeft = k * _left + c;
                            double yRight = k * _right + c;
                            double xTop = (_top - c) / k;
                            double xBottom = (_bottom - c) / k;

                            if (yLeft <= _top && yLeft >= _bottom) {
                                Vector2d v = new Vector2d(_left, yLeft);
                                crossA = v;
                                if (yRight <= _top && yRight >= _bottom) {
                                    Vector2d v2 = new Vector2d(_right, yRight);
                                    crossB = v2;
                                    length = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
                                } else if (xBottom >= _left && xBottom <= _right) {
                                    Vector2d v2 = new Vector2d(xBottom, _bottom);
                                    crossB = v2;
                                    length = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
                                } else if (xTop >= _left && xTop <= _right) {
                                    Vector2d v2 = new Vector2d(xTop, _top);
                                    crossB = v2;
                                    length = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
                                }
                            } else if (yRight <= _top && yRight >= _bottom) {
                                Vector2d v = new Vector2d(_right, yRight);
                                crossA = v;
                                if (xBottom >= _left && xBottom <= _right) {
                                    Vector2d v2 = new Vector2d(xBottom, _bottom);
                                    crossB = v2;
                                    length = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
                                } else if (xTop >= _left && xTop <= _right) {
                                    Vector2d v2 = new Vector2d(xTop, _top);
                                    crossB = v2;
                                    length = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
                                }
                            } else if (xBottom >= _left && xBottom <= _right) {
                                Vector2d v = new Vector2d(xBottom, _bottom);
                                Vector2d v2 = new Vector2d(xTop, _top);
                                crossA = v;
                                crossB = v2;
                                length = Math.sqrt((v2.x - v.x) * (v2.x - v.x) + (v2.y - v.y) * (v2.y - v.y));
                            }
                        }
                        if (length > lengthMax) {
                            lengthMax = length;
                            aMax = a;
                            bMax = b;
                            crossAmax = crossA;
                            crossBmax = crossB;
                        }
                    }
                }
            }
            // задача решена
            solved = true;

            if (aMax != null && bMax != null) {
                sPoints.add(aMax);
                sPoints.add(bMax);
                crossPoints.add(crossAmax);
                crossPoints.add(crossBmax);

                // выводим на панель лога координаты точек пересечения
                PanelLog.info("Точка {" + crossPoints.get(0).x + ';' + crossPoints.get(0).y +
                        "} является точкой пересечения прямой и прямоугольника");
                PanelLog.info("Точка {" + crossPoints.get(1).x + ';' + crossPoints.get(1).y +
                        "} является точкой пересечения прямой и прямоугольника");
            }
        }
    }

    /**
     * Получить тип мира
     *
     * @return тип мира
     */
    public CoordinateSystem2d getOwnCS() {
        return ownCS;
    }

    /**
     * Получить название мира
     *
     * @return название мира
     */
    public ArrayList<Point> getPoints() {
        return points;
    }

    /**
     * Отмена решения задачи
     */
    public void cancel() {
        solved = false;
        sPoints.clear();
        crossPoints.clear();
    }

    /**
     * проверка, решена ли задача
     *
     * @return флаг
     */
    public boolean isSolved() {
        return solved;
    }

    /**
     * проверка, нарисован ли прямоугольник
     * @return флаг
     */
    public boolean isRectanglePaint(){
        return rectangle;
    }

    /**
     * проверка, можно ли провести прямую
     * @return флаг
     */
    public boolean isLineAble(){
        return points.size() >= 2;
    }

    /**
     * проверка, пересекает ли прямоугольник прямая, параллельная одной из осей координат
     */
    public boolean isParallelLineCross(){
        return sPoints.size() == 0 && solved;
    }

    /**
     * Масштабирование области просмотра задачи
     *
     * @param delta  прокрутка колеса
     * @param center центр масштабирования
     */
    public void scale(float delta, Vector2i center) {
        if (lastWindowCS == null) return;
        // получаем координаты центра масштабирования в СК задачи
        Vector2d realCenter = ownCS.getCoords(center, lastWindowCS);
        // выполняем масштабирование
        ownCS.scale(1 + delta * WHEEL_SENSITIVE, realCenter);
    }

    /**
     * Получить положение курсора мыши в СК задачи
     *
     * @param x        координата X курсора
     * @param y        координата Y курсора
     * @param windowCS СК окна
     * @return вещественный вектор положения в СК задачи
     */
    @JsonIgnore
    public Vector2d getRealPos(int x, int y, CoordinateSystem2i windowCS) {
        return ownCS.getCoords(x, y, windowCS);
    }

    /**
     * Получить список точек, являющихся решением
     */
     public ArrayList<Point> getsPoints(){
         return sPoints;
     }

    /**
     * Получить список точек, являющихся пересечением прямой и прямоугольника
     */
    public ArrayList<Vector2d> getCrossPoints(){
        return crossPoints;
    }

    /**
     * Рисование курсора мыши
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     * @param font     шрифт
     * @param pos      положение курсора мыши
     */
    public void paintMouse(Canvas canvas, CoordinateSystem2i windowCS, Font font, Vector2i pos) {
        // создаём перо
        try (var paint = new Paint().setColor(TASK_GRID_COLOR)) {
            // сохраняем область рисования
            canvas.save();
            // рисуем перекрестие
            canvas.drawRect(Rect.makeXYWH(0, pos.y - 1, windowCS.getSize().x, 2), paint);
            canvas.drawRect(Rect.makeXYWH(pos.x - 1, 0, 2, windowCS.getSize().y), paint);
            // смещаемся немного для красивого вывода текста
            canvas.translate(pos.x + 3, pos.y - 5);
            // положение курсора в пространстве задачи
            Vector2d realPos = getRealPos(pos.x, pos.y, lastWindowCS);
            // выводим координаты
            canvas.drawString(realPos.toString(), 0, 0, font, paint);
            // восстанавливаем область рисования
            canvas.restore();
        }
    }

}
