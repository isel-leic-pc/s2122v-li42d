package isel.leic.pc.coroutines3


import isel.leic.pc.coroutines3.music_api.MusicApiCrImpl
import isel.leic.pc.coroutines3.music_api.MusicApiFakeImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test

class MusicApiCrTests {
    @Test
    fun get_bowie_info() {
        runBlocking {
            val api = MusicApiCrImpl(MusicApiFakeImpl())

            val bowie = api.getArtist("David Bowie")
            println(bowie)
        }
    }

    @Test
    fun get_coldplay_info() {
        runBlocking {
            val api = MusicApiCrImpl(MusicApiFakeImpl())

            val coldplay = api.getArtist("ColdPlay")
            println(coldplay)
        }
    }

}