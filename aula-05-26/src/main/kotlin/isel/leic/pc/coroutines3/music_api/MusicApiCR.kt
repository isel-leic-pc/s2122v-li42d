package isel.leic.pc.coroutines3.music_api;

import data.Album;
import data.Artist;
import data.ArtistDetail;
import data.Track;

interface MusicApiCR {

    suspend fun getArtist(name: String) : Artist

    suspend fun getArtistDetail(id: Int) : ArtistDetail

    suspend fun getArtistAlbums(id : Int) : List<Album>

    suspend fun getAlbumTracks(id : Int) : List<Track>
}
