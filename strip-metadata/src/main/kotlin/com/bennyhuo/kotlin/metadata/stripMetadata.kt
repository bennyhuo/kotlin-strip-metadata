package com.bennyhuo.kotlin.metadata

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.jar.JarFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


object StripMetadata {
    private val CONSTANT_TIME_FOR_ZIP_ENTRIES = GregorianCalendar(1980, 1, 1, 0, 0, 0).timeInMillis

    /**
     * Removes @kotlin.Metadata annotations from a compiled Kotlin class file.
     */
    fun stripClass(
        inputClassFile: File,
        outputClassFile: File
    ) {
        val outBytes = stripClass(inputClassFile.readBytes())
        outputClassFile.writeBytes(outBytes)
    }

    /**
     * Removes @kotlin.Metadata annotations from compiled Kotlin class content.
     */
    fun stripClass(
        inputClassBytes: ByteArray
    ): ByteArray {
        var changed = false
        val classWriter = ClassWriter(0)
        val classVisitor = object : ClassVisitor(Opcodes.ASM9, classWriter) {
            override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
                if (Type.getType(desc).internalName == "kotlin/Metadata") {
                    changed = true
                    return null
                }
                return super.visitAnnotation(desc, visible)
            }
        }
        ClassReader(inputClassBytes).accept(classVisitor, 0)
        if (!changed) return inputClassBytes

        return classWriter.toByteArray()
    }

    fun stripJar(
        inputJarFile: File,
        outputJarFile: File,
        preserveFileTimestamps: Boolean = true,
        includeClassFilePattern: String? = null
    ) {
        val classRegex = includeClassFilePattern?.toRegex()

        assert(inputJarFile.exists()) { "Input file not found at $inputJarFile" }

        ZipOutputStream(BufferedOutputStream(FileOutputStream(outputJarFile))).use { outJar ->
            JarFile(inputJarFile).use { inJar ->
                for (entry in inJar.entries()) {
                    val inBytes = inJar.getInputStream(entry).readBytes()
                    val outBytes = if (
                        !entry.name.endsWith(".class") ||
                        classRegex?.matches(entry.name.removeSuffix(".class")) == false
                    ) {
                        inBytes
                    } else {
                        stripClass(inBytes)
                    }

                    if (inBytes.size < outBytes.size) {
                        error("Size increased for ${entry.name}: was ${inBytes.size} bytes, became ${outBytes.size} bytes")
                    }

                    val newEntry = ZipEntry(entry.name)
                    if (!preserveFileTimestamps) {
                        newEntry.time = CONSTANT_TIME_FOR_ZIP_ENTRIES
                    }
                    outJar.putNextEntry(newEntry)
                    outJar.write(outBytes)
                    outJar.closeEntry()
                }
            }
        }
    }
}