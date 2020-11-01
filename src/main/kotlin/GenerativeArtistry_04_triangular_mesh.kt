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
import org.openrndr.math.transforms.transform
import org.openrndr.shape.contour
import java.lang.Math.abs
import java.lang.Math.max
import kotlin.random.Random


fun main() = application {
    configure {
        width = 1024
        height = 1024
        position = IntVector2(5, 5)
        windowResizable = false
    }



    oliveProgram {

        val gui = GUI()

        val settings = object {

            @IntParameter("Random Seed", 0, 100)
            var seed = 0

            @DoubleParameter("Stroke Width", 0.1, 30.0)
            var stroke = 4.0

            @IntParameter("Margin Width", 10, 200)
            var margin = 90

            @DoubleParameter("JitterMax", 1.0, 300.0)
            var jittermax = 100.0

            @DoubleParameter("SpinMax", 1.0, 720.0)
            var SpinMax = 45.0

            @IntParameter("Breadth", 9, 20)
            var breadth = 9


        }.addTo(gui, "Configuration")

        extend {
            // Like the rest of these things, we need a random number generator
            val rand = Random(0)
            val lines = mutableListOf<MutableList<Vector2>>()
            val size = width-(2*settings.margin)
            val rectsize = (size/settings.breadth).toDouble()
            // Let's setup some nice defaults for fill/stroke/color/etc.
            drawer.clear(ColorRGBa.WHITE)
            drawer.strokeWeight = settings.stroke
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.TRANSPARENT



            // Once I get to a certain level of complexity, I find it really useful to
            // 'rubber duck' my plans for an algorithm into plain text. It makes it
            // easier to find my way back to the state of mind when I wrote it, and helps
            // reason about what I am doing during the process.


            // Before we do anything else, translate the entire drawing in the view, instead
            // of adding offsets to the coords. Makes it easier to reason about, and keeps
            // the cruft out of our actual design
            drawer.view*= transform{
                translate(settings.margin*1.0, settings.margin*1.0)
            }

            // These triangles can be mentally modelled as a bunch of rows of squares,
            // each of which is bisected diagonally from bottom left to top right. Let's
            // create a grid of vertices, broken into a pretty standard array of rows and columns

            // We've got the same config from the cubic disarray, because of how funamentally similar
            // these art pieces are, so we use those settings.
            for (yc in 0..(settings.breadth - 1)) {
                lines.add(((0..settings.breadth-1).map{ xc -> Vector2(xc*rectsize, yc*rectsize) }).toMutableList())
            }
            // ... we'll temporarily draw dots at those vertices to verify that it looks right.
            /*
            for(line in lines)
                for(point in line) {
                    drawer.circle(point, 3.0)
                }
            */


            // Time Travel! We're coming back and perturbing those linestrips...
            // But first, we slide each alternating row over by half the width of the squares...
            for(lineidx in 0..(lines.count()-1))
                if(lineidx % 2 == 1){
                    val line = lines[lineidx]
                    for(colidx in 0..(line.count()-1))
                        line[colidx] = Vector2(line[colidx].x+rectsize/2.0, line[colidx].y)
                }



            for(line in lines) {
                val iter = line.listIterator()
                while(iter.hasNext()){
                    val item = iter.next()
                    iter.set(Vector2(item.x+rand.nextFloat()*settings.jittermax, item.y+rand.nextFloat()*settings.jittermax))
                }
            }

            // Now, draw those squares; we iterate through the lines, starting from the first, until the second
            // to last. Each one, draw the square by TWO triangles, like this:
            //            --b--
            //            |  /|
            //            a e c
            //            |/  |
            //            ~~d~~
            //
            // And we also need to alternate e every other line, so it's even triangles...
            //
            // Once we 've done that^, we can go back and add a transformation to the lines to add the desired
            // amount of "skew" to get the even grid of triangles.

            for(xc in 0..(settings.breadth-2))
                for(yc in 0..(settings.breadth-2)){
                    // Oooh look... we can draw this as a single strip!
                    if (yc % 2 == 0){
                        drawer.lineStrip(
                                listOf(lines[yc+1][xc], lines[yc][xc],
                                        lines[yc][xc+1],
                                        lines[yc+1][xc+1],
                                        lines[yc+1][xc],
                                        lines[yc][xc+1]
                                ))

                    } else {
                        drawer.lineStrip(
                                listOf(
                                        lines[yc+1][xc+1],
                                        lines[yc][xc+1],
                                        lines[yc][xc],
                                        lines[yc+1][xc],
                                        lines[yc+1][xc+1],
                                        lines[yc][xc]
                                ))

                    }


                }

        }
        extend(gui)
    }
}