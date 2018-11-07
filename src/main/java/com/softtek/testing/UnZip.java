package com.softtek.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnZip {

    private static Logger log = LoggerFactory.getLogger(UnZip.class);

    public void unZipIt(InputStream inputStream, AzureUpload azureUpload) {

	byte[] buffer = new byte[2048];

	try {
	    log.info("Se va a descomprimir fichero zip");
	    ZipInputStream zis = new ZipInputStream(inputStream);

	    ZipEntry ze = zis.getNextEntry();

	    while (ze != null) {

		String fileName = ze.getName();

		if (!ze.isDirectory()) {
		    log.info("Fichero a dentro de zip,nombre = " + fileName);

		    ByteArrayOutputStream fos = new ByteArrayOutputStream();
		    int len;
		    while ((len = zis.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		    }

		    log.info("Se obtiene ByteArrayOutputStream de la entrada del zip");

		    InputStream sourceStream = new ByteArrayInputStream(fos.toByteArray());
		    try {

			log.info("Antes de iniciar subida de fichero uploadBlob");

			azureUpload.uploadBlob(fileName, sourceStream, ze.getSize());

			log.info("Blob almacenado: " + fileName);

		    } catch (Exception e) {
			log.error("Error subiendo blob: " + fileName, e);
		    } finally {
			fos.close();
			sourceStream.close();
		    }

		} else {
		    log.info("Directorio dentro de zip: " + fileName);
		}
		ze = zis.getNextEntry();
	    }

	    zis.closeEntry();
	    zis.close();

	    log.info("Done unzip");
	} catch (IOException ex) {

	    log.error("Erro descomprimiendo fichero", ex);
	}

    }

}