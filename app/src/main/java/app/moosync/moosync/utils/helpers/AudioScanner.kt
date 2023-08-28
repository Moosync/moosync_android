package app.moosync.moosync.utils.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.util.Log
import app.moosync.moosync.glide.AudioCover
import app.moosync.moosync.utils.PlayerTypes
import app.moosync.moosync.utils.models.Album
import app.moosync.moosync.utils.models.Artist
import app.moosync.moosync.utils.models.Genre
import app.moosync.moosync.utils.models.Song
import java.io.FileNotFoundException


class AudioScanner {
    fun readDirectory(mContext: Context): ArrayList<Song> {
        val songList: ArrayList<Song> = ArrayList()

        val proj = arrayListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DATE_MODIFIED,
        )

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            proj.add(MediaStore.Audio.Media.GENRE)
            proj.add(MediaStore.Audio.Media.GENRE_ID)
        }

        val cursor = mContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj.toTypedArray(),
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
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        songList.add(
                            Song(
                                _id = id.toString(),
                                title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)),
                                duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
                                artist = getArtist(cursor),
                                album = getAlbum(cursor),
                                genre = getGenre(cursor),
                                modified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)),
                                playbackUrl = id.toString(),
                                coverImage = AudioCover(id.toString()),
                                type = PlayerTypes.LOCAL
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
            return listOf(Artist(artistId.toString(), artistName, null))
        }
        return null
    }

    private fun getAlbum(cursor: Cursor): Album? {
        val albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
        val albumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

        if (albumId != 0L) {
            return Album(albumId.toString(), albumName)
        }
        return null
    }

    @SuppressLint("InlinedApi")
    private fun getGenre(cursor: Cursor): List<Genre>? {
        val genreIdIndex = cursor.getColumnIndex(MediaStore.Audio.Media.GENRE_ID)
        if (genreIdIndex >= 0) {
            val genreId = cursor.getLong(genreIdIndex)
            val genreName =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE))

            if (genreId != 0L) {
                return listOf(Genre(genreId.toString(), genreName))
            }
        }
        return null
    }
}