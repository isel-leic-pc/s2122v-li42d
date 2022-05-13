package isel.leic.pc.completablefutures.service

import completablefutures.api.Album
import completablefutures.api.Artist
import completablefutures.api.MusicApi
import completablefutures.api.Track
import java.util.concurrent.CompletableFuture

class MusicService(val api: MusicApi) {

    fun getArtistGenre(artistName : String) : CompletableFuture<String> {
       return api.getArtist(artistName)
            .thenApply {
                it.genre
            }
    }

    fun getArtistAlbums(artistName: String) : CompletableFuture<List<Album>> {
         return api.getArtist(artistName)
                 .thenCompose {
                     api.getArtistAlbums(it.id)
                 }
    }

    fun getArtistListenersNumber(artistName: String)
            : CompletableFuture<Int> {
       return api.getArtist(artistName)
            .thenCompose {
                api.getArtistDetail(it.id)
            }
            .thenApply {
                it.listeners
            }

    }

    fun areArtistsSameGenre(artistName1: String, artistName2: String)
            : CompletableFuture<Boolean> {
       return api.getArtist(artistName1)
               .thenCompose {
                   a1 ->
                  api.getArtist(artistName2)
                      .thenApply {
                          a2 ->
                            a1.genre == a2.genre
                      }
               }
    }

    fun areArtistsSameGenre2(artistName1: String, artistName2: String)
            : CompletableFuture<Boolean> {
      return api.getArtist(artistName1)
            .thenCombine(api.getArtist(artistName2)) {
                a1, a2 ->  a1.genre == a2.genre
            }
    }

    fun getArtistsAlbums(artistNames: List<String>) : CompletableFuture<List<Album>> {
        val result =
            artistNames.map {
                api.getArtist(it)
                .thenCompose {
                    api.getArtistAlbums((it.id))
                }
            }

        return CompletableFuture.allOf(*result.toTypedArray())
        .thenApply {
           result.flatMap {
               it.get()
           }
        }
    }
}