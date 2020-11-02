import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.extra.compositor.*
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.fx.color.ColorCorrection
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.fx.patterns.Checkers
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.noise.Random.perlin
import org.openrndr.extra.noise.perlin
import org.openrndr.extra.noise.simplex
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.vfx.Contour
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.Vector3
import org.openrndr.math.transforms.transform
import org.openrndr.shape.contour
import java.lang.Math.*
import kotlin.random.Random


fun main() = application {
    configure {
        width =  1024
        height = 1024
        position = IntVector2(5, 5)
        windowResizable=false
    }



    oliveProgram {
        // 15 x 15 blocks.
        val cols = 15
        val rows = 15

        val gui = GUI()

        val settings = object {

            @IntParameter("Random Seed", 0, 100)
            var seed = 0

            @DoubleParameter("Stroke Width", 0.1, 30.0)
            var stroke = 4.0

            @IntParameter("Margin Width", 10, 200)
            var margin = 30

            @DoubleParameter("JitterMax", 1.0, 300.0)
            var jittermax = 20.0

            @DoubleParameter("RotateMax", 1.0, 300.0)
            var rotatemax = 45.0


        }.addTo(gui, "Configuration")


        extend {
            val wide = width-(2*settings.margin)
            val high = height-(2*settings.margin)
            val squaresize = (wide/15).toDouble()
            val rand = Random(settings.seed)
            drawer.clear(ColorRGBa.WHITE)
            drawer.strokeWeight = settings.stroke
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.WHITE
            drawer.lineCap = LineCap.ROUND

            /*
            OK, we're gonna draw a 15x15 grid of 'lined squares'. Each square has either 1, 2, or 3 vertical
            lines, equally distributed across it's width.
            Those lines will be rotated a random amount, and displaced a bit. In all, this is very similar to
            the cubic disarray exercise.
            We'll use row%5 to get the number of lines.
            We'll just hhammer the whole thhing into translation blocks just like cubic disarray.
             */

            // Translate up front so thhat it's easy to draw everything in absolute coords.
            drawer.view*= transform{
                translate(settings.margin*1.0, settings.margin*1.0)
            }

            for(row in 0..14) {
                val numlines = floor(row / 5.0).toInt() + 1 // 5 rows each of 1,2,3 vertical lines
                for (col in 0..14) {
                    // Get a random rotation/shift value for the square we're drawing the lines in.
                    val rot = (rand.nextFloat()-0.5)*settings.rotatemax
                    val shiftx = (rand.nextFloat()-0.5)*(settings.jittermax)
                    val shifty = (rand.nextFloat()-0.5)*(settings.jittermax)
                    // Push a new transform on the stack, then pop when we're done, for each square.
                    drawer.pushView()
                    drawer.view *= transform{
                        translate(Vector2(shiftx, shifty))
                        translate(Vector2((squaresize*col)+(squaresize/2), (squaresize*row)+(squaresize/2)))
                        rotate(Vector3(0.0,0.0, 1.0), rot)
                    }

                    // Draw between 1 and 3 lines, equally spaced.
                    for(i in 1..numlines){
                        drawer.lineSegment(
                                -(squaresize/2) + squaresize*(i/(numlines+1.0)),
                                -(squaresize/2),
                                -(squaresize/2) + squaresize*(i/(numlines+1.0)),
                                (squaresize/2))
                    }
                    // Back to abs coords
                    drawer.popView()
                }
            }
        }
        extend(gui)
    }

}