package isel.leic.pc.coroutines3.music_api

import data.Album
import data.Artist
import data.ArtistDetail
import data.Track
import mu.KotlinLogging
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

interface MusicApi {
    fun getArtist(name: String) : CompletableFuture<Artist>
    fun getArtistDetail(id: Int) : CompletableFuture<ArtistDetail>
    fun getArtistAlbums(id : Int) : CompletableFuture<List<Album>>

    fun getAlbumTracks(id : Int) : CompletableFuture<List<Track>>
}


class MusicApiFakeImpl : MusicApi {
    private val random = Random.Default

    private val artists = mapOf(
        Pair("David Bowie", Artist("David Bowie", 1, "Pop")),
        Pair("Police", Artist("Police", 2, "Rock")),
        Pair("Diana Krall", Artist("Diana Krall", 3, "Jazz")),
        Pair("Coldplay", Artist("Coldplay", 4,  "Pop")),
        Pair("Ben Webster", Artist("Ben Webster", 5, "Jazz")),
        Pair("Genesis", Artist("Genesis", 6, "Rock")),
        Pair("Neil Young", Artist("Neil Young", 7, "Folk")),
        Pair("Bob Dylan", Artist("Bob Dylan", 8, "Folk")),
        Pair("Leonard Cohen", Artist("Leonard Cohen", 9, "Folk"))
    )

    private val bowie_albums = listOf(
        Album("The man who sold the world", 1, 1, 1970, 9 ),
        Album("Heroes", 2, 1, 1977, 10)
    )

    private val coldplay_albums = listOf(
        Album("Ghost stories", 3, 4, 2014, 9 ),
        Album("A head full of dreams", 4, 4, 2015, 11 )
    )

    private val neil_albums = listOf<Album>(

    )

    private val ben_albums = listOf<Album>(

    )

    private val leonard_albums = listOf<Album>(

    )

    private val bob_albums = listOf<Album>(

    )

    private val genesis_albums = listOf<Album>(

    )

    private val police_albums = listOf<Album>(

    )

    private val diana_albums = listOf<Album>(

    )


    private val albums =  mapOf<Int, List<Album>>(
        Pair(1, bowie_albums),
        Pair(2, police_albums),
        Pair(3, diana_albums),
        Pair(4, coldplay_albums),
        Pair(5, ben_albums),
        Pair(6, genesis_albums),
        Pair(7, neil_albums),
        Pair(8, bob_albums),
        Pair(9, leonard_albums)
    )

    val tracks = mapOf<Int, List<Track>>()

    override fun getArtist(name: String): CompletableFuture<Artist> {
         return CompletableFuture.supplyAsync<Artist> {
             //sleep(random.nextLong(4) + 2)
             artists[name]
         }
    }

    override fun getArtistDetail(artistId: Int): CompletableFuture<ArtistDetail> {
        return CompletableFuture.supplyAsync<ArtistDetail> {
            sleep(random.nextLong(4) + 2)
            ArtistDetail(artistId, 1000000+ artistId)
        }
    }


    override fun getArtistAlbums(id: Int): CompletableFuture<List<Album>> {
        logger.info("start getArtistAlbums for cf at ${System.currentTimeMillis()}")
        val cf = CompletableFuture.supplyAsync<List<Album>> {
            logger.info("getArtistAlbums cf ")
            sleep(1000)
            albums[id]
        }
        logger.info("end getArtistAlbums for cf at ${System.currentTimeMillis()}")
        return cf
    }

    override fun getAlbumTracks(id: Int): CompletableFuture<List<Track>> {
        return CompletableFuture.supplyAsync<List<Track>> {
            sleep(random.nextLong(4) + 2)
            tracks[id]
        }
    }


}