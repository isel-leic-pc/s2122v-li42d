package isel.leic.pc.coroutines


import isel.leic.pc.coroutines3.music_api.MusicApiCrImpl
import isel.leic.pc.coroutines3.music_api.MusicApiFakeImpl
import isel.leic.pc.coroutines3.music_api.MusicServiceCR
import kotlinx.coroutines.*


import mu.KotlinLogging
import org.junit.Test
import java.lang.Thread.sleep

private val logger = KotlinLogging.logger {}

class MusicServiceTests {

    @Test
    fun bowie_listeners() {
        runBlocking {

            val service = MusicServiceCR(MusicApiCrImpl(MusicApiFakeImpl()))

            val l = service.getArtistListeners("David Bowie")
            println(l)
        }
    }

    @Test
    fun bowie_coldplay_albums() {
        runBlocking {
            logger.info("start test")
            val service =   MusicServiceCR(
                                MusicApiCrImpl(
                                        MusicApiFakeImpl()
                                )
                            )

            val startTime = System.currentTimeMillis()

            try {
                val albums = service
                    .getArtistsAlbums(
                        listOf("David Bowie", "Coldplay")
                    )
                val endTime = System.currentTimeMillis()
                println("done in ${endTime - startTime}ms")

                for (album in albums) {
                    println(album)
                }

            }
            catch(e: Exception) {
                println("getArtistsAlbums terminated with exception: $e")
            }


        }

        logger.info("Test done")
    }

}