package app.moosync.moosync.utils.helpers

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import app.moosync.moosync.utils.models.Album
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Genre
import app.moosync.moosync.utils.models.Song
import java.io.FileNotFoundException


class AudioScanner {
    fun readDirectory(mContext: Context): ArrayList<Song> {
        val songList: ArrayList<Song> = ArrayList()

        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.GENRE_ID,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )
        val cursor = mContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj,
            null,
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        )

        if (cursor != null && cursor.moveToFirst()) {
            do {

                val isMusic =
                    cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC))

                if (isMusic != 0) {
                    try {
                        songList.add(
                            Song(
                                _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)),
                                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                                artist = getArtist(cursor),
                                album = getAlbum(cursor),
                                genre = getGenre(cursor),
                                modified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
                            )
                        )
                    } catch (e: FileNotFoundException) {
                        Log.e("TAG", "readDirectory: ", e)
                    }
                }
            } while (cursor.moveToNext())

            cursor.close()
        }

        return songList
    }

    private fun getArtist(cursor: Cursor): List<Artist>? {
        val artistId =
            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
        val artistName =
            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))

        if (artistId != 0L) {
            return listOf(Artist(artistId, artistName))
        }
        return null
    }

    private fun getAlbum(cursor: Cursor): Album? {
        val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

        if (albumId != 0L) {
            return Album(albumId, albumName)
        }
        return null
    }

    private fun getGenre(cursor: Cursor): List<Genre>? {
        val genreId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE_ID))
        val genreName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))

        if (genreId != 0L) {
            return listOf(Genre(genreId, genreName))

        }
        return null
    }
}