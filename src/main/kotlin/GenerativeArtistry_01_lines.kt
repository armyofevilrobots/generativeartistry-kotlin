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
import org.openrndr.math.IntVector2
import org.openrndr.shape.contour
import kotlin.random.Random

/**
 *  This is a template for a live program.
 *
 *  It uses oliveProgram {} instead of program {}. All code inside the
 *  oliveProgram {} can be changed while the program is running.
 */

val X_CELLS = 32
val Y_CELLS = 32
val CELL_DIM = 32

enum class Tile {
    FOO, BAR
}

fun main() = application {
    configure {
        width =  CELL_DIM * X_CELLS
        height = CELL_DIM * Y_CELLS
        position = IntVector2(5, 5)
        windowResizable=true
    }
    oliveProgram {
        val rand = Random(32)
        val state = (0..Y_CELLS).map{
            ((0..X_CELLS).map{
                if(rand.nextBoolean()) Tile.FOO else Tile.BAR
            }) as MutableList<Tile>
        }

        val gui = GUI()

        val cp = compose{

            layer{
                draw{
                    drawer.clear(ColorRGBa.WHITE)
                }
            }
            layer{
                draw{
                    // Actual boxen
                    drawer.lineCap = LineCap.ROUND
                    drawer.strokeWeight = 5.0
                    drawer.stroke = ColorRGBa.BLACK
                    for(x in 0..32){
                        for(y in 0..32) {
                            if (state[y][x] == Tile.FOO) {
                                drawer.lineSegment(x * 32.0, y * 32.0, (x + 1) * 32.0, (y + 1) * 32.0)
                            } else {
                                drawer.lineSegment(x * 32.0, (y + 1) * 32.0, (x + 1) * 32.0, y * 32.0)
                            }
                        }
                    }
                }
            }
        }

        extend {
            val x = rand.nextInt(0,32)
            val y = rand.nextInt(0,32)
            state[y][x] = if(rand.nextBoolean()) Tile.FOO else Tile.BAR // Mutate one cell per run

            cp.draw(drawer)

        }
    }

//        extend(gui)
}