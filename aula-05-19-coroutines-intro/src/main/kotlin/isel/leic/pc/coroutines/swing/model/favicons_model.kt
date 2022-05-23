package isel.leic.pc.coroutines.swing.model

import isel.leic.pc.coroutines.swing.view.FavIconsApp
import isel.leic.pc.coroutines.swing.view.logger
import kotlinx.coroutines.*
import pc.li52d.web.getIcon
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.DefaultFocusManager
import javax.swing.ImageIcon
import javax.swing.JLabel

internal class GetIconParams(
    val url: String,
    val delay: Long,
    val label: JLabel
)

internal suspend fun processShowIconsSerial(vararg requests : GetIconParams)  {
    val icons = mutableListOf<ImageIcon>()
    try {
        coroutineScope {
            for (req in requests) {
                logger.info("start one more load")
                val img = getIcon(req.url, req.delay )
                req.label.icon  =
                    ImageIcon(
                       img
                        .getScaledInstance(120, 120, Image.SCALE_SMOOTH)
                    )
            }
            logger.info("after loads")
        }
    }
    catch(e: Exception) {
        logger.info("error: ${e.message}")
    }
}

internal suspend fun processShowIconsParallel0(vararg requests : GetIconParams) {

    val defereds : MutableList<Deferred<BufferedImage>> = mutableListOf( )
    try {
        coroutineScope {
            logger.info("event handler in thread ${Thread.currentThread().name}")

            for (req in requests) {

                val def = async  {
                    logger.info("load image ${req.url}")
                    val img = getIcon(req.url, req.delay )
                    logger.info("one more load")
                    img
                }
                defereds.add(def)
            }
            
            for (i in 0 until requests.size) {
                val local = i
                logger.info("set icon for ${requests[i].url}")
                requests[local].label.icon  =
                    ImageIcon(
                        defereds[local].await()
                        .getScaledInstance(120, 120, Image.SCALE_SMOOTH)
                    )

            }
        }

        logger.info("after loads")
    }
    catch(e: Exception) {
        logger.info("exception : $e}")
    }
}

internal suspend fun processShowIconsParallel(vararg requests : GetIconParams) {

    try {
        coroutineScope {
            logger.info("event handler in thread ${Thread.currentThread().name}")

            val res = requests
            .asIterable()
            .map {
                async {
                    logger.info("load image ${it.url}")
                    val img = getIcon(it.url, it.delay )
                    logger.info("one more load")
                    img
                }
            }
            .zip(requests) { d, r ->
                r.label.icon  = ImageIcon(
                    d.await()
                    .getScaledInstance(120, 120, Image.SCALE_SMOOTH)
                )
            }

        }

        logger.info("after loads")
    }
    catch(e: Exception) {
        logger.info("exception : $e}")
    }
}
