package job;

import org.vertx.java.core.json.JsonObject;
import commons.Constants;
import conn.Connection;
import filemanager.FileBean;
import filemanager.FileManager;

public class FileJob extends BasicJob {
	private String fileByteString;

	/**
	 * Constructor for receiving.
	 * 
	 * @param json
	 * @param connection
	 */
	FileJob(JsonObject json, Connection connection) {
		super(json, connection);
		JsonObject body = json.getObject(Constants.JSON.BODY);
		fileByteString = body.getString(Constants.Body.FILEBYTES);
	}

	/**
	 * Constructor for sending.
	 * 
	 * @param path
	 * @param connection
	 */
	public FileJob(FileBean file, Connection connection, FileManager filemanager) {
		super(file, connection);
		filemanager.getFileBytes(file);
	}

	@Override
	public Job execute(FileManager filemanager) {
		// TODO handle newer existing file
		Job toSend = null;
		boolean success = filemanager.createFile(file, fileByteString);
		if (success) {
			// TODO Broadcast
		}
		return toSend;

	}

	@Override
	public String getJson() {
		JsonObject json = new JsonObject();
		json.putString(Constants.JSON.TYPE, Constants.Type.FILE);
		JsonObject body = file.getJsonObject();
		body.putString(Constants.Body.FILEBYTES, fileByteString);
		json.putObject(Constants.JSON.BODY, body);
		return json.encode();
	}
}
