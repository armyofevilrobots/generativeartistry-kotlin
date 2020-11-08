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
import org.openrndr.shape.contour
import kotlin.math.sqrt
import kotlin.random.Random

data class Square(val x: Double, val y: Double, val width: Double, val height: Double, var color: ColorRGBa = ColorRGBa.TRANSPARENT)

fun splitSquare(square:Square, x:Double?=null, y:Double?=null): List<Square>?{
    """Split on either a vertical(X) line or horizontal(Y) one"""
    x?.let{ xsplit->
        assert(y==null)
        if(xsplit>square.x && xsplit<(square.x+square.width)){
            return listOf(
                    Square(square.x,square.y, xsplit-square.x, square.height),
                    Square(xsplit,square.y, (square.x+square.width)-xsplit, square.height)
            )
        }
    }
    y?.let{ ysplit->
        assert(x==null)
        if(ysplit>square.y && ysplit<(square.y+square.height)){
            return listOf(
                    Square(square.x,square.y, square.width, ysplit-square.y),
                    Square(square.x,ysplit, square.width, (square.y+square.height)-ysplit)
            )
        }

    }
    return null
}

fun main() = application {
    configure {
        width =  1024
        height = 1024
        position = IntVector2(5, 5)
        windowResizable=false
    }



    oliveProgram {


        val gui = GUI()

        val settings = object {

            @IntParameter("Stroke Width", 1, 30)
            var stroke = 6

            @IntParameter("Seed", 0, 1024)
            var seed=0

            @IntParameter("Divisions", 4, 30)
            var divisions = 7

            @DoubleParameter("Split Probability", 0.0001, 0.9999)
            var splitProbability=0.5

        }.addTo(gui, "Configuration")

//        window.presentationMode = PresentationMode.MANUAL
//        window.requestDraw()


        val colors = listOf("#D40920", "#1356A2", "#F7D842")

        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.strokeWeight = settings.stroke.toDouble()
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.TRANSPARENT
            val rand = Random(settings.seed)

            val step=width.toDouble()/settings.divisions
//            println("Division width is ${step}")
            var squares = mutableListOf(Square(0.0,0.0, width.toDouble(), height.toDouble()))

            var newSquares = mutableListOf<Square>()

            // Iterate over X
//            println("Squares are: ${squares}")
//            println("We will split ${settings.divisions} times")
            var i = 0
//            for(i in 0..(squares.size-1)) {
            while(i < squares.size){
                for (j in 0 until (settings.divisions-1)){
                    if (rand.nextFloat() < settings.splitProbability) {
//                        println("Splitting square at division y:$yd")
                        val newSquares = splitSquare(squares[i], null, j * step)
//                        println("New squares for Y(${yd*step}) split: ${squares[i]}: ${newSquares}")
                        if (newSquares != null) {
                            // Delete the old square
                            squares.removeAt(i)
                            // Add the new ones on the end
                            squares.addAll(newSquares)
                        }
                    }
                    if (rand.nextFloat() < settings.splitProbability) {
                        val newSquares = splitSquare(squares[i], j * step, null)
//                        println("New square for X(${xd*step}) split: ${squares[i]}: ${newSquares}")
                        if (newSquares != null) {
                            // Delete the old square
                            squares.removeAt(i)
                            // Add the new ones on the end
                            squares.addAll(newSquares)
                        }
                    }
                }

                i++ // End of loop, increment
            }
            for(color in colors){
                if(squares.size>1)
                    squares[rand.nextInt(0, squares.size-1)].color = ColorRGBa.fromHex(color)
            }

            // And just draw all the squares
            for (square in squares){

                drawer.fill=square.color
                drawer.rectangle(square.x, square.y, square.width, square.height)
            }

        }
        extend(gui)

    }

}