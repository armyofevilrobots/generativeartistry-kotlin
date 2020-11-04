import kotlinx.coroutines.yield
import org.openrndr.PresentationMode
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.contour
import kotlin.math.sqrt
import kotlin.random.Random


fun main() = application {
    configure {
        width =  1024
        height = 1024
        position = IntVector2(5, 5)
        windowResizable=false
    }



    oliveProgram {

        val circles = mutableListOf<GenCircle>()

        val gui = GUI()

        val settings = object {

            @IntParameter("Random Seed", 0, 100)
            var seed = 0

            @IntParameter("Grid Size", 5, 20)
            var gridSize = 7

            @IntParameter("Final Size", 2, 10)
            var finalSize = 10

            @IntParameter("Stroke Width", 1, 30)
            var stroke = 3

            @IntParameter("Margin Width", 10, 50)
            var margin = 20

            @IntParameter("Max Steps", 4, 10)
            var maxSteps = 5;


        }.addTo(gui, "Configuration")

        /*
        This one is basically a verbatim copy of: https://generativeartistry.com/tutorials/hypnotic-squares/
        although I'll do a bit of extra transformation to get a nice margin around it.

        We're using recursion to draw squares inside of squares inside of squares, with an
        offset on each internal square to push it closer to the edge in a consistent direction.
         */

        extend {
            val rand = Random(settings.seed)
            drawer.clear(ColorRGBa.GRAY)
            drawer.strokeWeight = settings.stroke.toDouble()
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.TRANSPARENT

            val squareSize = (width-(2.0*settings.margin))/settings.gridSize

            // Recursively draw nested squares, with the described offsets.
            fun square(x: Double, y: Double, size: Double, dx: Double, dy: Double, steps: Int, initSteps: Int){
                drawer.rectangle(x, y, size+settings.stroke, size+settings.stroke)
                val newSize = (squareSize * (1.0*(steps-1)/initSteps) + settings.finalSize + settings.stroke)

                var newX = (x+(size-newSize)/2.0)
                newX = newX - ((x - newX) / (steps + settings.stroke/2.0)) * dx

                var newY = y+(size-newSize)/2.0
                newY = newY - ((y - newY) / (steps + settings.stroke/2.0)) * dy

                // If we're not out, start again.
                if(steps>0)
                    square(newX, newY, newSize, dx, dy, steps-1, initSteps)
            }

            // Translate up front so thhat it's easy to draw everything in absolute coords.
            drawer.view*= transform{
                translate(1.0*settings.margin, 1.0*settings.margin)
            }

            for(x in 0..(settings.gridSize-1))
                for(y in 0..(settings.gridSize-1)){
                    val steps = rand.nextInt(3, settings.maxSteps)
                    square(x*squareSize, y*squareSize, squareSize,
                            rand.nextInt(-1,1).toDouble(), rand.nextInt(-1,1).toDouble(), steps, steps)

                }

        }
        extend(gui)

    }

}