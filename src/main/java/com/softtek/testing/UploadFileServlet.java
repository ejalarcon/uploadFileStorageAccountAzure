package com.softtek.testing;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.microsoft.azure.storage.core.Base64;

@WebServlet({ "/uploadFile" })
public class UploadFileServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private ServletFileUpload uploader = null;

	private UnZip unZip = null;
	private AzureUpload azureUpload = null;
	private boolean configProxy = false;
	private final Hashtable<String, String> users = new Hashtable<String, String>();

	private ResourceBundle config = ResourceBundle.getBundle("config");

	private final static Logger log = Logger.getLogger(UploadFileServlet.class);

	public void init() throws ServletException {

		log.info("Inicializando UploadFileServlet...");
		final FileItemFactory fileFactory = new DiskFileItemFactory();
		this.uploader = new ServletFileUpload(fileFactory);

		this.unZip = new UnZip();

		config = ResourceBundle.getBundle("config");
		String key = System.getenv("APPSETTING_upload.app.user") + ":" + System.getenv("APPSETTING_upload.app.pwd");

		log.info("---Aqui1:" + System.getenv("APPSETTING_upload.app.user"));
		log.info("---Aqui2:" + System.getenv("upload.app.user"));
		this.users.put(key, "allowed");

		log.info("UploadFileServlet inicializado OK...");

	}

	private void inicializaProxy(String hostname) {
		if ("localhost".equals(hostname)) {
			ProxyUtils.initializeProxyAuthenticator();
		}
		configProxy = true;
		log.info("Proxy inicializado!");
	}

	protected boolean allowUser(String auth) throws IOException {
		if (auth == null) {
			return false;
		}
		if (!auth.toUpperCase().startsWith("BASIC ")) {
			return false;
		}

		String userpassEncoded = auth.substring(6);

		String userpassDecoded = new String(Base64.decode(userpassEncoded), "UTF-8");

		if ("allowed".equals(this.users.get(userpassDecoded))) {
			return true;
		}
		log.warn("No permitido acceso a usuario  " + auth);
		return false;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (!ServletFileUpload.isMultipartContent(request)) {
			log.error("No es contenido multipart");
			throw new ServletException("Content type is not multipart/form-data");
		}

		String auth = request.getHeader("Authorization");

		if (!allowUser(auth)) {
			response.setContentType("text/plain");
			response.setHeader("WWW-Authenticate", "BASIC realm=\"users\"");
			response.sendError(401);
		} else {

			if (!configProxy) {
				inicializaProxy(request.getServerName());
			}

			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.write("<html><head></head><body>");

			try {
				log.info("Se va a parser request...");
				List<FileItem> fileItemsList = this.uploader.parseRequest(request);

				Iterator<FileItem> fileItemsIterator = fileItemsList.iterator();

				log.info("Fichero obtenido...");

				String endpointSuffix = config.getString("upload.app.endpointSuffix");
				String containerName = config.getString("upload.app.containerName");
				log.info("Nombre de container: " + containerName);

				String accountName = System.getenv("APPSETTING_upload.app.accountName");
				String accountKey = System.getenv("APPSETTING_upload.app.accountKey");

				log.info("Nombre de accountName: " + accountName);

				azureUpload = new AzureUpload(accountName, accountKey, endpointSuffix, containerName);

				log.info("AzureUpload inicializado...");

				while (fileItemsIterator.hasNext()) {

					FileItem fileItem = (FileItem) fileItemsIterator.next();
					log.info("FieldName=" + fileItem.getFieldName());
					log.info("FileName=" + fileItem.getName());
					log.info("ContentType=" + fileItem.getContentType());
					log.info("Size in bytes=" + fileItem.getSize());

					String fileName = fileItem.getName();

					log.info("Fichero a subir: " + fileName);

					if (fileName.toUpperCase().endsWith("ZIP")) {

						unZip.unZipIt(fileItem.getInputStream(), azureUpload);

					} else {
						azureUpload.uploadBlob(fileName, fileItem.getInputStream(), fileItem.getSize());
					}

					out.write("File " + fileItem.getName() + " uploaded successfully.");
					out.write("<br>");

				}
			} catch (FileUploadException e) {
				out.write("Exception in uploading file.");
				log.error("Error subiendo fichero", e);
			} catch (Exception e) {
				out.write("Exception in uploading file.");
				log.error("Error general subiendo fichero", e);
			}
			out.write("</body></html>");
		}
	}
}
