package com.hootor.tmc_2.data.media

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Base64
import androidx.camera.core.ImageProxy
import java.io.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import androidx.exifinterface.media.ExifInterface
import java.nio.ByteBuffer

object MediaHelper {

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    fun decodeBitmap(uri: Uri, context: Context): Bitmap? {
        val IMG_MAX_SIDE_SIZE = 2000
        return try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
            var scale = 1
            while (options.outWidth / scale / 2 >= IMG_MAX_SIDE_SIZE && options.outHeight / scale / 2 >= IMG_MAX_SIDE_SIZE) scale *= 2

            val scaleOptions = BitmapFactory.Options()
            scaleOptions.inSampleSize = scale
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, scaleOptions)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    fun uriToBitmap(context: Context, uri: Uri?): Bitmap? {
        if (uri == null) return null
//        val inputStream = context.contentResolver.openInputStream(uri)
//        return BitmapFactory.decodeStream(inputStream)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val sours = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(sours)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri);
        }

    }

    fun encodeToBase64(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat,
        quality: Int
    ): String {
        val byteArrayOS = ByteArrayOutputStream()
        bitmap.compress(compressFormat, quality, byteArrayOS)
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
    }

    fun saveBitmapToFile(file: File): Bitmap? {
        // BitmapFactory options to downsize the image
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        o.inSampleSize = 6

        var inputStream = FileInputStream(file)
        BitmapFactory.decodeStream(inputStream, null, o)
        inputStream.close()

        // The new size we want to scale to
        val requiredSize = 75

        // Find the correct scale value. It should be the power of 2.
        var scale = 1
        while (o.outWidth / scale / 2 >= requiredSize && o.outHeight / scale / 2 >= requiredSize) {
            scale *= 2
        }

        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        inputStream = FileInputStream(file)

        val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
        inputStream.close()

        // Overriding the original image file
        file.createNewFile()
        val outputStream = FileOutputStream(file)

        if (selectedBitmap == null) {
            return null
        }

        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        return selectedBitmap
    }

    fun getPath(context: Context, uri: Uri): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val mInputPFD = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val fileDescriptor = mInputPFD.fileDescriptor

            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            // val tempUri = getImageUri(context, image)

            return getAbsolutePath(context, uri)

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                    context,
                    uri
                )
            ) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    if ("primary".equals(type, ignoreCase = true)) {

                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getAbsolutePath(context, contentUri)

                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split =
                        docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getAbsolutePath(
                        context,
                        contentUri,
                        selection,
                        selectionArgs
                    )
                }
            } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getAbsolutePath(
                    context,
                    uri
                )

            } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
                return uri.path
            }
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun getAbsolutePath(
        context: Context,
        uri: Uri?,
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): String? {
        if (uri == null) {
            return null
        }

        var path: String? = null

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            path = cursor.getString(columnIndex)
        }
        cursor?.close()
        return path
    }

}

object ImageOptimizer {

    /**
     * @param context the application environment
     * @param imageUri the input image uri. usually "content://..."
     * @param compressFormat the output image file format
     * @param maxWidth the output image max width
     * @param maxHeight the output image max height
     * @param useMaxScale determine whether to use the bigger dimension
     * between [maxWidth] or [maxHeight]
     * @param quality the output image compress quality
     * @param minWidth the output image min width
     * @param minHeight the output image min height
     *
     * @return output image [android.net.Uri]
     */
    fun optimize(
        context: Context,
        imageUri: Uri,
        compressFormat: Bitmap.CompressFormat,
        maxWidth: Float,
        maxHeight: Float,
        useMaxScale: Boolean,
        quality: Int,
        minWidth: Int,
        minHeight: Int
    ): Uri? {
        /**
         * Decode uri bitmap from activity result using content provider
         */
        val bmOptions: BitmapFactory.Options = decodeBitmapFromUri(context, imageUri)

        /**
         * Calculate scale factor of the bitmap relative to [maxWidth] and [maxHeight]
         */
        val scaleDownFactor: Float = calculateScaleDownFactor(
            bmOptions, useMaxScale, maxWidth, maxHeight
        )

        /**
         * Since [BitmapFactory.Options.inSampleSize] only accept value with power of 2,
         * we calculate the nearest power of 2 to the previously calculated scaleDownFactor
         * check doc [BitmapFactory.Options.inSampleSize]
         */
        setNearestInSampleSize(bmOptions, scaleDownFactor)

        /**
         * 2 things we do here with image matrix:
         * - Adjust image rotation
         * - Scale image matrix based on remaining [scaleDownFactor / bmOption.inSampleSize]
         */
        val matrix: Matrix = calculateImageMatrix(
            context, imageUri, scaleDownFactor, bmOptions
        ) ?: return null

        /**
         * Create new bitmap based on defined bmOptions and calculated matrix
         */
        val newBitmap: Bitmap = generateNewBitmap(
            context, imageUri, bmOptions, matrix
        ) ?: return null
        val newBitmapWidth = newBitmap.width
        val newBitmapHeight = newBitmap.height

        /**
         * Determine whether to scale up the image or not if the
         * image width and height is below minimum dimension
         */
        val shouldScaleUp: Boolean = shouldScaleUp(
            newBitmapWidth, newBitmapHeight, minWidth, minHeight
        )

        /**
         * Calculate the final scaleUpFactor if the image need to be scaled up.
         */
        val scaleUpFactor: Float = calculateScaleUpFactor(
            newBitmapWidth.toFloat(), newBitmapHeight.toFloat(), maxWidth, maxHeight,
            minWidth, minHeight, shouldScaleUp
        )

        /**
         * calculate the final width and height based on final scaleUpFactor
         */
        val finalWidth: Int = finalWidth(newBitmapWidth.toFloat(), scaleUpFactor)
        val finalHeight: Int = finalHeight(newBitmapHeight.toFloat(), scaleUpFactor)

        /**
         * Generate the final bitmap, by scaling up if needed
         */
        val finalBitmap: Bitmap = scaleUpBitmapIfNeeded(
            newBitmap, finalWidth, finalHeight, scaleUpFactor, shouldScaleUp
        )

        /**
         * compress and save image
         */
        val imageFilePath: String = compressAndSaveImage(
            finalBitmap, compressFormat, quality
        ) ?: return null

        return Uri.fromFile(File(imageFilePath))
    }

    private fun decodeBitmapFromUri(
        context: Context,
        imageUri: Uri
    ): BitmapFactory.Options {
        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        val input: InputStream? = context.contentResolver.openInputStream(imageUri)
        BitmapFactory.decodeStream(input, null, bmOptions)
        input?.close()
        return bmOptions
    }

    private fun calculateScaleDownFactor(
        bmOptions: BitmapFactory.Options,
        useMaxScale: Boolean,
        maxWidth: Float,
        maxHeight: Float
    ): Float {
        val photoW = bmOptions.outWidth.toFloat()
        val photoH = bmOptions.outHeight.toFloat()
        val widthRatio = photoW / maxWidth
        val heightRatio = photoH / maxHeight
        var scaleFactor = if (useMaxScale) {
            max(widthRatio, heightRatio)
        } else {
            min(widthRatio, heightRatio)
        }
        if (scaleFactor < 1) {
            scaleFactor = 1f
        }
        return scaleFactor
    }

    private fun setNearestInSampleSize(
        bmOptions: BitmapFactory.Options,
        scaleFactor: Float
    ) {
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor.toInt()
        if (bmOptions.inSampleSize % 2 != 0) { // check if sample size is divisible by 2
            var sample = 1
            while (sample * 2 < bmOptions.inSampleSize) {
                sample *= 2
            }
            bmOptions.inSampleSize = sample
        }
    }

    private fun calculateImageMatrix(
        context: Context,
        imageUri: Uri,
        scaleFactor: Float,
        bmOptions: BitmapFactory.Options
    ): Matrix? {
        val input: InputStream = context.contentResolver.openInputStream(imageUri) ?: return null
        val exif = ExifInterface(input)
        val matrix = Matrix()
        val orientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(
                90f
            )
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(
                180f
            )
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(
                270f
            )
        }
        val remainingScaleFactor = scaleFactor / bmOptions.inSampleSize.toFloat()
        if (remainingScaleFactor > 1) {
            matrix.postScale(1.0f / remainingScaleFactor, 1.0f / remainingScaleFactor)
        }
        input.close()
        return matrix
    }

    private fun generateNewBitmap(
        context: Context,
        imageUri: Uri,
        bmOptions: BitmapFactory.Options,
        matrix: Matrix
    ): Bitmap? {
        var bitmap: Bitmap? = null
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        try {
            bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions)
            if (bitmap != null) {
                val matrixScaledBitmap: Bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (matrixScaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = matrixScaledBitmap
                }
            }
            inputStream?.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun shouldScaleUp(
        photoW: Int,
        photoH: Int,
        minWidth: Int,
        minHeight: Int
    ): Boolean {
        return (minWidth != 0 && minHeight != 0 && (photoW < minWidth || photoH < minHeight))
    }

    private fun calculateScaleUpFactor(
        photoW: Float,
        photoH: Float,
        maxWidth: Float,
        maxHeight: Float,
        minWidth: Int,
        minHeight: Int,
        shouldScaleUp: Boolean
    ): Float {
        var scaleUpFactor: Float = max(photoW / maxWidth, photoH / maxHeight)
        if (shouldScaleUp) {
            scaleUpFactor = if (photoW < minWidth && photoH > minHeight) {
                photoW / minWidth
            } else if (photoW > minWidth && photoH < minHeight) {
                photoH / minHeight
            } else {
                max(photoW / minWidth, photoH / minHeight)
            }
        }
        return scaleUpFactor
    }

    private fun finalWidth(
        photoW: Float, scaleUpFactor: Float
    ): Int {
        return (photoW / scaleUpFactor).toInt()
    }

    private fun finalHeight(
        photoH: Float, scaleUpFactor: Float
    ): Int {
        return (photoH / scaleUpFactor).toInt()
    }

    private fun scaleUpBitmapIfNeeded(
        bitmap: Bitmap,
        finalWidth: Int,
        finalHeight: Int,
        scaleUpFactor: Float,
        shouldScaleUp: Boolean
    ): Bitmap {
        val scaledBitmap: Bitmap = if (scaleUpFactor > 1 || shouldScaleUp) {
            Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
        } else {
            bitmap
        }
        if (scaledBitmap != bitmap) {
            bitmap.recycle()
        }
        return scaledBitmap
    }

    private fun compressAndSaveImage(
        bitmap: Bitmap,
        compressFormat: Bitmap.CompressFormat?,
        quality: Int,
    ): String? {
        val uniqueID = UUID.randomUUID().toString()
        val fileName = "test_optimization_$uniqueID.jpg"
        val fileDir = File("/storage/emulated/0/Download/")
        val imageFile = File(fileDir, fileName)
        val stream = FileOutputStream(imageFile)
        bitmap.compress(compressFormat, quality, stream)
        stream.close()
        bitmap.recycle()
        return imageFile.absolutePath
    }


}