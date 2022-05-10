import app.Point;
import app.Task;
import misc.CoordinateSystem2d;
import misc.Vector2d;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс тестирования
 */
public class UnitTest {

    /**
     * Тест
     *
     * @param points        список точек
     * @param tops          вершины прямоугольника
     */
    private static void test(CoordinateSystem2d ownCS, ArrayList<Point> points, ArrayList<Point> tops,
                             ArrayList<Point> sPoints, ArrayList<Vector2d> crossPoints) {
        Task task = new Task(ownCS, points, tops);
        task.solve();

        // проверяем, правильно ли мы нашли точки, через которые будем проводить прямую
        for(Point p : sPoints){
            assert task.getsPoints().contains(p);
        }

        // проверяем, правильно ли мы нашли точки пересечения прямой и прямоугольника
        for(Vector2d p : crossPoints){
            assert task.getCrossPoints().contains(p);
        }

    }


    /**
     * Первый тест
     */
    @Test
    public void test1() {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Point> tops = new ArrayList<>();
        CoordinateSystem2d ownCS = new CoordinateSystem2d(-10, -10, 20, 20);
        ArrayList<Point> sPoints = new ArrayList<>();
        ArrayList<Vector2d> crossPoints = new ArrayList<>();

        points.add(new Point(new Vector2d(1, 1)));
        points.add(new Point(new Vector2d(9, 9)));
        points.add(new Point(new Vector2d(6, 1)));
        points.add(new Point(new Vector2d(6, 9)));

        tops.add(new Point(new Vector2d(9, 8)));
        tops.add(new Point(new Vector2d(2, 3)));

        sPoints.add(new Point(new Vector2d(1,1)));
        sPoints.add(new Point(new Vector2d(9,9)));

        crossPoints.add(new Vector2d(3,3));
        crossPoints.add(new Vector2d(8,8));

        test(ownCS, points, tops, sPoints, crossPoints);
    }

    /**
     * Второй тест
     */
    @Test
    public void test2() {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Point> tops = new ArrayList<>();
        CoordinateSystem2d ownCS = new CoordinateSystem2d(-10, -10, 20, 20);
        ArrayList<Point> sPoints = new ArrayList<>();
        ArrayList<Vector2d> crossPoints = new ArrayList<>();

        points.add(new Point(new Vector2d(-5, 1)));
        points.add(new Point(new Vector2d(-5, -8)));

        tops.add(new Point(new Vector2d(1, 1)));
        tops.add(new Point(new Vector2d(9, 9)));

        test(ownCS, points, tops, sPoints, crossPoints);
    }

    /**
     * Третий тест
     */
    @Test
    public void test3() {
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Point> tops = new ArrayList<>();
        CoordinateSystem2d ownCS = new CoordinateSystem2d(-10, -10, 20, 20);
        ArrayList<Point> sPoints = new ArrayList<>();
        ArrayList<Vector2d> crossPoints = new ArrayList<>();

        points.add(new Point(new Vector2d(6, 9)));
        points.add(new Point(new Vector2d(0, -9)));

        tops.add(new Point(new Vector2d(1, 1)));
        tops.add(new Point(new Vector2d(6, 7)));

        test(ownCS, points, tops, sPoints, crossPoints);
    }
}
