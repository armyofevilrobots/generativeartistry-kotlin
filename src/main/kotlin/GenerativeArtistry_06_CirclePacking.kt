import kotlinx.coroutines.yield
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.contour
import kotlin.math.sqrt
import kotlin.random.Random

data class GenCircle(val x: Double, val y:Double, val radius:Double)

fun circleCollides(circle: GenCircle, circles: List<GenCircle>): Boolean{
    // This reads pretty straightforward, but...
    // If the distance between the circle centers is less than the sum of
    // their radii, then one is inside the other, otherwise, not!
    for(ex in circles){
        val dx = ex.x-circle.x
        val dy = ex.y-circle.y
        val distance = sqrt((dx*dx)+(dy*dy))
        if(distance<(circle.radius+ex.radius))
            return true
    }
    return false
}

fun main() = application {
    configure {
        width =  1024
        height = 1024
        position = IntVector2(5, 5)
        windowResizable=false
    }



    oliveProgram {

        val circles = mutableListOf<GenCircle>()
        val rand = Random(System.currentTimeMillis())

//        val gui = GUI()


        extend {
            drawer.clear(ColorRGBa.GRAY)
            drawer.strokeWeight = 4.0
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.TRANSPARENT

            // Always just draw a single new circle per frame, add a max of 1024 circles
            while(circles.size < 1024){
                val newCircle = GenCircle(rand.nextDouble()*width, rand.nextDouble()*height, 6.0+rand.nextDouble()*width/2)
                if(!circleCollides(newCircle, circles)) {
                    circles.add(newCircle)
                    break
                }

            }
            for(circle in circles){
                val scale = sqrt(circle.radius/(6.0+(width/2)))
                drawer.fill = ColorRGBa(scale, scale, scale, 1.0)
                drawer.circle(circle.x, circle.y,circle.radius)
            }

        }

    }

}