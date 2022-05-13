package completablefutures.api

import java.util.concurrent.CompletableFuture

interface MusicApi {
    fun getArtist(name: String) : CompletableFuture<Artist>

    fun getArtistDetail(id: Int) : CompletableFuture<ArtistDetail>

    fun getArtistAlbums(id : Int) : CompletableFuture<List<Album>>

    fun getAlbumTracks(id : Int) : CompletableFuture<List<Track>>

}