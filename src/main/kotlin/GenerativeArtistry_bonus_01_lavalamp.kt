import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.LineCap
import org.openrndr.draw.LineJoin
import org.openrndr.draw.isolated
import org.openrndr.extra.compositor.*
import org.openrndr.extra.fx.blend.Darken
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.extra.fx.blur.GaussianBloom
import org.openrndr.extra.fx.color.ColorCorrection
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.fx.patterns.Checkers
import org.openrndr.extra.fx.shadow.DropShadow
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.noise.Random.perlin
import org.openrndr.extra.noise.perlin
import org.openrndr.extra.noise.simplex
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.math.IntVector2
import org.openrndr.shape.contour

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

fun main() = application {
    configure {
        width = 1920 /2 - 10
        height = 1080 - 10
        position = IntVector2(5, 5)
        windowResizable=true
    }
    oliveProgram {

        val gui = GUI()
        val settings = object{

            @DoubleParameter("Radius", 50.0, 200.0)
            var radius=200.0

            @DoubleParameter("Speed", 0.1, 10.0)
            var speed=1.0

            @IntParameter("Bloibs", 1,10)
            var bloibs = 3

        }.addTo(gui, "Settings")

        val cp = compose{

            layer{
                post(Checkers())
                draw{
                    drawer.clear(ColorRGBa.GRAY)
                }
            }
            // The balls each get a layer, and being composable functions is fucking awesome.
            for(thisLayer in 0..10)
            layer{
                draw {
//                    val thisLayer = layerId
                    if(thisLayer < settings.bloibs){
                        val red = (simplex(thisLayer+0, seconds*settings.speed) * 0.5) + 0.5
                        val green = (simplex(thisLayer+1, seconds*settings.speed) * 0.5) + 0.5
                        val blue = (simplex(thisLayer+2, seconds*settings.speed) * 0.5) + 0.5
                        val alpha = (simplex(thisLayer*3, seconds*settings.speed) * 0.1) + 0.9
                        val radius = settings.radius + (simplex(thisLayer+1, seconds*settings.speed) * settings.radius)
                        val xpos = (simplex(thisLayer+2, seconds*settings.speed) * width/3) + width/2
                        val ypos = (simplex(thisLayer+3, seconds*settings.speed) * height/3) + height/2
                        drawer.fill = ColorRGBa(red, green, blue, alpha)
                        drawer.strokeWeight=0.0
                        drawer.circle(xpos, ypos, radius)
                    }
                blend(Multiply())
                }
//                blend(Darken())
//            blend(org.openrndr.extra.fx.blend.ColorBurn())
            }
            post(DropShadow()){}.addTo(gui)
            post(Perturb()){}.addTo(gui)
            post(ColorCorrection()){}.addTo(gui)
            post(GaussianBloom()){}.addTo(gui)
        }


        extend {
            cp.draw(drawer)
        }

        extend(gui)
    }
}