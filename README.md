Upload Files Softtek a una StorageAccount de Azure

Es necesario configurar las siguientes propiedades del sistema:

	upload.app.user: Usuario para acceder a la apliación
	upload.app.pwd: Password para acceder a la aplicación
	upload.app.accountName: Nombre de la cuenta de Storage de Azure
	upload.app.accountKey: Clave para acceder a la Cuenta de Azure

Para acceder posteriormente a la aplicación:

GET http://{hostname}/storageAccountAzure/index.html

POST http://{hostname}/storageAccountAzure/uploadFile