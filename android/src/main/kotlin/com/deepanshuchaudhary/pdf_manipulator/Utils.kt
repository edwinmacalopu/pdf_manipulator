package com.deepanshuchaudhary.pdf_manipulator

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import io.flutter.plugin.common.MethodChannel
import java.io.*

class Utils {

    fun deleteTempFiles(listOfTempFiles: List<File>) {
        listOfTempFiles.forEach { tempFile ->
            tempFile.delete()
        }
    }

    fun copyDataFromSourceToDestDocument(
        sourceFileUri: Uri, destinationFileUri: Uri, contentResolver: ContentResolver
    ) {

        // its important to truncate an output file to size zero before writing to it
        // as user may have selected an old file to overwrite which need to be cleaned before writing
        truncateDocumentToZeroSize(
            uri = destinationFileUri, contentResolver = contentResolver
        )

        try {
            contentResolver.openInputStream(sourceFileUri).use { inputStream ->
                contentResolver.openOutputStream(destinationFileUri).use { outputStream ->
                    if (inputStream != null && outputStream != null) {
                        try {
                            inputStream.copyTo(outputStream)
                            inputStream.close()
                            outputStream.close()
                            println("Data successfully copied from one file to another")
                        } catch (e: Exception) {
                            inputStream.close()
                            outputStream.close()
                            println(e)
                            e.printStackTrace()
                        }
                    } else {
                        println("Either inputStream or outputStream has null value")
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun truncateDocumentToZeroSize(uri: Uri, contentResolver: ContentResolver) {
        try {
            contentResolver.openFileDescriptor(uri, "wt")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                    PrintWriter(fileOutputStream).use { printWriter ->
                        printWriter.close()
                    }
                    fileOutputStream.close()
                }
                parcelFileDescriptor.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getURI(uri: String): Uri {
        val parsed: Uri = Uri.parse(uri)
        val parsedScheme: String? = parsed.scheme
        return if ((parsedScheme == null) || parsedScheme.isEmpty()) {
            Uri.fromFile(File(uri))
        } else parsed
    }

    fun getFileNameFromPickedDocumentUri(uri: Uri, context: Activity): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val contentResolver: ContentResolver = context.contentResolver
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    fileName =
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }

        return cleanupFileName(fileName)
    }

    private fun cleanupFileName(fileName: String?): String? {
        // https://stackoverflow.com/questions/2679699/what-characters-allowed-in-file-names-on-android
        return fileName?.replace(Regex("[\\\\/:*?\"<>|\\[\\]]"), "_")
    }

    fun finishSuccessfullyWithString(
        result: String?, resultCallback: MethodChannel.Result?
    ) {
        resultCallback?.success(result)
    }

    fun finishSplitSuccessfullyWithListOfString(
        result: List<String>?, resultCallback: MethodChannel.Result?
    ) {
        resultCallback?.success(result)
    }

    fun finishSplitSuccessfullyWithListOfListOfDouble(
        result: List<List<Double>>?, resultCallback: MethodChannel.Result?
    ) {
        resultCallback?.success(result)
    }


    fun finishSplitSuccessfullyWithListOfBoolean(
        result: List<Boolean?>, resultCallback: MethodChannel.Result?
    ) {
        resultCallback?.success(result)
    }

    fun finishWithError(
        errorCode: String,
        errorMessage: String?,
        errorDetails: String?,
        resultCallback: MethodChannel.Result?
    ) {
        resultCallback?.error(errorCode, errorMessage, errorDetails)
    }

//    fun clearPDFFilesFromCache(context: Activity) {
//        val cacheDir: File = context.cacheDir
//        if (cacheDir.exists()) {
//            val cacheFilesArray: Array<out File>? = cacheDir.listFiles()
//            val cacheFilesList: List<File> = cacheFilesArray?.toList() ?: listOf()
//            cacheFilesList.forEach { cacheFile ->
//                if (cacheFile.name.contains(".pdf", ignoreCase = true)) {
//                    println(cacheFile.name + " deleted")
//                    cacheFile.delete()
//                }
//            }
//        } else {
//            cacheDir.mkdirs()
//        }
//    }

}