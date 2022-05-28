package isel.leic.pc.coroutines3.music_api

import data.Album
import data.Artist
import data.ArtistDetail
import data.Track
import kotlinx.coroutines.suspendCancellableCoroutine

import java.security.InvalidParameterException
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class MusicApiCrImpl(val api : MusicApi) : MusicApiCR {
    override suspend fun getArtist(name: String) : Artist {
        return api.getArtist(name).await();
    }

    override suspend fun getArtistDetail(id: Int) : ArtistDetail {
        return api.getArtistDetail(id).await();
    }

    override suspend fun getArtistAlbums(id : Int) : List<Album> {
        return api.getArtistAlbums(id).await();
    }

    override suspend fun getAlbumTracks(id : Int) : List<Track> {
        return api.getAlbumTracks(id).await();
    }

}


suspend  fun <T> CompletableFuture<T>.await() : T {
    return suspendCancellableCoroutine { cont ->

        cont.invokeOnCancellation {
            this.cancel(true)
        }
        whenComplete {
                t, err ->
            if (err != null) {
                cont.resumeWithException(err)
            }
            else {
                if (t == null)
                    cont.resumeWithException(InvalidParameterException("Unknown"))
                else
                    cont.resume(t)
            }
        }
    }
}
