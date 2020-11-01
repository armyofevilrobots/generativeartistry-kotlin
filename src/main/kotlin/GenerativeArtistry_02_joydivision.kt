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
import org.openrndr.shape.contour
import java.lang.Math.abs
import java.lang.Math.max
import kotlin.random.Random


fun main() = application {
    configure {
        width =  1024
        height = 1024
        position = IntVector2(5, 5)
        windowResizable=false
    }



    oliveProgram {

        val NUMLINES = 25
        val NUMPOINTS = 30
        val rand = Random(0)
        val linePoints = (0..NUMLINES).map{line->
            (0..NUMPOINTS).map{step->
                Vector2((1.0 * width * step) / (1.0 * NUMPOINTS),
                        ( -10.0*max(((NUMPOINTS/2.0)-(NUMPOINTS/8.0)-abs(step-(NUMPOINTS/2.0)))/NUMPOINTS, 0.0)
                                *rand.nextFloat()*(1.0*height/NUMLINES))
                                +(1.0 * width * line) / (1.0 * NUMLINES))}
                as MutableList }

        val gui = GUI()


        extend {
            drawer.clear(ColorRGBa.WHITE)
            drawer.strokeWeight = 4.0
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.WHITE
            for(line in linePoints.subList(3,NUMLINES)){

                val c = contour{
                    moveTo(line[0])
                    for(i in 0..(NUMPOINTS-1)){
                        val pos = Vector2((line[i].x+line[i+1].x)/2.0, (line[i].y+line[i+1].y)/2.0)
                        curveTo(line[i],pos )
                    }
                }
                val f = contour{
                    moveTo(line[0])
                    for(i in 0..(NUMPOINTS-1)){
                        val pos = Vector2((line[i].x+line[i+1].x)/2.0, (line[i].y+line[i+1].y)/2.0)
                        curveTo(line[i],pos )
                    }
                    close()
                }
                drawer.fill = ColorRGBa.WHITE
                drawer.stroke = ColorRGBa.WHITE
                drawer.strokeWeight = 0.0
                drawer.contour(f)
                drawer.strokeWeight = 4.0
                drawer.stroke = ColorRGBa.BLACK
                drawer.contour(c)

            }

        }
    }

//        extend(gui)
}