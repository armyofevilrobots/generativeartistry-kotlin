import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.contour
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
            var jittermax = 45.0

            @IntParameter("Breadth", 5, 30)
            var breadth = 9


        }.addTo(gui, "Configuration")

        extend {
            // Like the rest of these things, we need a random number generator
            val rand = Random(settings.seed)
            val lines = mutableListOf<MutableList<Vector2>>()
            val size = width-(2.0*settings.margin)
            val rectsize = (size/settings.breadth).toDouble()
            // Let's setup some nice defaults for fill/stroke/color/etc.
            drawer.clear(ColorRGBa.WHITE)
            drawer.strokeWeight = settings.stroke
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.TRANSPARENT
            // If we don't round off the corners, those overlapping triangles look spiky.
            drawer.lineCap = LineCap.ROUND
            drawer.lineJoin = LineJoin.ROUND

            // Once I get to a certain level of complexity, I find it really useful to
            // 'rubber duck' my plans for an algorithm into plain text. It makes it
            // easier to find my way back to the state of mind when I wrote it, and helps
            // reason about what I am doing during the process.


            // Before we do anything else, translate the entire drawing in the view, instead
            // of adding offsets to the coords. Makes it easier to reason about, and keeps
            // the cruft out of our actual design
            drawer.view *= transform{
                translate(settings.margin*1.25, settings.margin*1.25)
            }

            // These triangles can be mentally modelled as a bunch of rows of squares,
            // each of which is bisected diagonally from bottom left to top right. Let's
            // create a grid of vertices, broken into a pretty standard array of rows and columns

            // We've got the same config from the cubic disarray, because of how funamentally similar
            // these art pieces are, so we use those settings.
            for (yc in 0..(settings.breadth - 1)) {
                lines.add(((0..settings.breadth-1).map{ xc ->
                    Vector2(xc*rectsize, yc*rectsize) }).toMutableList())
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
//                        println()
                        line[colidx] = Vector2(line[colidx].x+rectsize/2.0, line[colidx].y)
                }

            for(line in lines) {
                val iter = line.listIterator()
                while(iter.hasNext()){
                    val item = iter.next()
                    iter.set(Vector2(item.x+rand.nextFloat()*settings.jittermax-settings.jittermax/2.0, item.y+rand.nextFloat()*settings.jittermax-settings.jittermax/2.0))
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
            //            --b--
            //            |\  |
            //            a e c
            //            |  \|
            //            ~~d~~
            //
            // Remember, we've shifted every other line DOWN, so those shifted triangles are now perfectly
            // symmetrical (turn the jitter to zero if you don't believe me).
            //
            // Once we 've done that^, we can go back and add a transformation to the lines to add the desired
            // amount of "skew" to get the even grid of triangles.

            for(xc in 0..(settings.breadth-2))
                for(yc in 0..(settings.breadth-2)){
                    // Setup those grays up front...
                    val gray1 = (rand.nextInt(0,15) * 16)/256.0
                    val gray2 = (rand.nextInt(0,15) * 16)/256.0
                    // Oooh look... we can draw this as a single strip!
                    if (yc % 2 == 0){
                        val t1 = contour {
                            moveTo(lines[yc+1][xc])
                            lineTo(lines[yc][xc])
                            lineTo(lines[yc][xc+1])
                            close()
                        }
                        val t2 = contour {
                            moveTo(lines[yc+1][xc])
                            lineTo(lines[yc+1][xc+1])
                            lineTo(lines[yc][xc+1])
                            close()
                        }
                        drawer.fill = ColorRGBa(gray1,gray1, gray1, 1.0)
                        drawer.contour(t1)
                        drawer.fill = ColorRGBa(gray2,gray2, gray2, 1.0)
                        drawer.contour(t2)

                    } else {
                        val t1 = contour {
                            moveTo(lines[yc+1][xc])
                            lineTo(lines[yc+1][xc+1])
                            lineTo(lines[yc][xc])
                            close()
                        }
                        val t2 = contour {
                            moveTo(lines[yc][xc])
                            lineTo(lines[yc][xc+1])
                            lineTo(lines[yc+1][xc+1])
                            close()
                        }
                        drawer.fill = ColorRGBa(gray1,gray1, gray1, 1.0)
                        drawer.contour(t1)
                        drawer.fill = ColorRGBa(gray2,gray2, gray2, 1.0)
                        drawer.contour(t2)
                    }


                }

        }
        extend(gui)
    }
}