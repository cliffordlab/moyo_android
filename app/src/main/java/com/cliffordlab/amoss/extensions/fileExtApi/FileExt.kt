package com.cliffordlab.amoss.extensions.fileExtApi

import java.io.BufferedWriter
import java.io.File
import java.io.RandomAccessFile

/**
 * Created by ChristopherWainwrightAaron on 5/21/17.
 */

//if file is null when access is called return null
fun File.makeAvailableWithHeader(fileType: FileType) {
    println("started access function")
    if (!this.exists()) {
        println("should create file in access")
        this.createNewFile()
    }
    this.setHeader(fileType)
}

//use buffered writer with automatic new line
fun BufferedWriter.writeLn(line: String) {
    this.write(line)
    this.newLine()
}

//set the header of a file if file is blank
private fun File.setHeader(headerType: FileType) {
    //check if file is empty and if it is set header of file
    if (this.bufferedReader().readLine() == null) {
        val randomAccessFile = RandomAccessFile(this, "rw")
        randomAccessFile.writeHeaderLn(FileHeaders.header(headerType))
    }
}

//seek to beginning of file and write string as header
private fun RandomAccessFile.writeHeaderLn(line: String) {
    this.seek(0)
    this.writeUTF("$line\n")
    this.close()
}

