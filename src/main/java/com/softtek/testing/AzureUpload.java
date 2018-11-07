package com.softtek.testing;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import org.apache.log4j.Logger;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class AzureUpload {

	private CloudStorageAccount storageAccount;
	private CloudBlobClient blobClient = null;
	private CloudBlobContainer container = null;

	private final static Logger log = Logger.getLogger(AzureUpload.class);

	public AzureUpload(String accountName, String accountKey, String endpointSuffix, String containerName)
			throws InvalidKeyException, URISyntaxException, StorageException {

		String storageConnectionString = this.getStorageConnectionString(accountName, accountKey, endpointSuffix);
		storageAccount = CloudStorageAccount.parse(storageConnectionString);
		log.info("CloudStorageAccount creada");

		blobClient = storageAccount.createCloudBlobClient();
		log.info("CloudBlobClient creado");

		container = blobClient.getContainerReference(containerName);
		log.info("CloudBlobContainer creada");
	}

	public void uploadBlob(String name, InputStream sourceStream, long length)
			throws URISyntaxException, StorageException, IOException {

		log.info("Iniciando uploadBlob...");

		CloudBlockBlob blob = container.getBlockBlobReference(name);
		log.info("CloudBlockBlob creado");

		blob.upload(sourceStream, length);

		log.info("Blob " + name + " subido OK");
	}

	private String getStorageConnectionString(String accountName, String accountKey, String endpointSuffix) {
		String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=" + accountName + ";AccountKey="
				+ accountKey + ";EndpointSuffix=" + endpointSuffix;
		return storageConnectionString;
	}
}
