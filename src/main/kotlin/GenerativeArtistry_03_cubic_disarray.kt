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
import java.lang.Math.abs
import java.lang.Math.max
import kotlin.random.Random


fun main() = application {
    configure {
        width =  1024
        height = width
        position = IntVector2(5, 5)
        windowResizable=false
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
            val wide = width-(2*settings.margin)
            val high = wide
            val rectsize = (wide/settings.breadth).toDouble()
            val rand = Random(settings.seed)
            val jittermax=rectsize/2
            drawer.clear(ColorRGBa.WHITE)
            drawer.strokeWeight = settings.stroke
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = ColorRGBa.TRANSPARENT

            drawer.view*=transform{
                translate(settings.margin*1.0, settings.margin*1.0)
            }
            for(yb in 0..(settings.breadth-1)){
                for(xb in 0..(settings.breadth-1)){
                    val rot = (rand.nextFloat()-0.5)*(yb*settings.SpinMax/settings.breadth)
                    val shiftx = (rand.nextFloat()-0.5)*(yb*settings.jittermax/settings.breadth)
                    val shifty = 0.0//(rand.nextFloat()-0.5)*(yb*settings.jittermax/settings.breadth)
                    drawer.pushView()
                    drawer.view *= transform{
                        translate(Vector2(shiftx, shifty))
                        translate(Vector2(rectsize*xb+rectsize/2, rectsize*yb+rectsize/2))
                        rotate(Vector3(0.0,0.0, 1.0), rot)
                    }
                    drawer.rectangle(-rectsize/2, -rectsize/2, rectsize, rectsize)
                    drawer.popView()
                }
            }
        }
        extend(gui)
    }

}