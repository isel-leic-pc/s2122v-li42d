package isel.leic.pc.coroutines3.music_api

import data.Album
import isel.leic.pc.coroutines2.list
import kotlinx.coroutines.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MusicServiceCR(val api: MusicApiCR) {

    suspend fun getArtistGenre(artistName : String) : String {
        return api.getArtist(artistName).genre
    }

    suspend fun getArtistAlbums(artistName: String) : List<Album> {
        val artist =  api.getArtist(artistName)
        return api.getArtistAlbums(artist.id)
    }

    suspend fun getArtistsAlbums(artistNames: List<String>) : List<Album> =
                                        coroutineScope {
        logger.info("start getArtistsAlbums")
        list()

        val launchers = artistNames.map {
            logger.info("call api.getArtist at ${System.currentTimeMillis()}")
            api.getArtist(it)
        }
        .map {artist ->
            async { api.getArtistAlbums(artist.id) }
        }
        .flatMap { deferred ->
            deferred.await()
        }
        logger.info("start getArtistsAlbums")
        launchers
    }

    suspend fun getArtistListeners(artistName: String) : Int {
        val artist = api.getArtist(artistName)
        return  api.getArtistDetail(artist.id).listeners
    }

    suspend fun areArtistsSameGenre(artistName1: String, artistName2: String)
            : Boolean {
        val artist1 = api.getArtist(artistName1)
        val artist2 = api.getArtist(artistName2)
        return artist1.genre == artist2.genre
    }

    suspend fun areArtistSameGenre2(artistName1: String, artistName2: String)
            : Boolean  = coroutineScope {
        val artist1 =  async {
            api.getArtist(artistName1)
        }

        val artist2 =  async {
            api.getArtist(artistName2)
        }

        artist1.await().genre == artist2.await().genre
    }

}