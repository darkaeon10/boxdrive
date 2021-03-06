package job;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.vertx.java.core.json.JsonObject;

import client.filerecords.FileRecord;

import commons.Constants;
import commons.Util;

public class FileBean implements Comparable<FileBean> {
	private String filename;
	private long lastModified;
	private byte[] checksum;
	private boolean isDirectory = false;

	@Override
	public String toString() {
		return filename;
	}

	/**
	 * 
	 * @param path
	 *            A localized Path.
	 */
	public FileBean(Path path) {
		// Remove top level folder from filename
		filename = path.subpath(1, path.getNameCount()).toString();
		isDirectory = Files.isDirectory(path);
		checksum = Util.getChecksum(path.toString());

		try {
			lastModified = Files.getLastModifiedTime(path).toMillis();
		} catch (IOException e) {
			lastModified = 0;
		}
	}

	public FileBean(JsonObject body) {
		filename = body.getString(Constants.Body.FILENAME);
		lastModified = body.getLong(Constants.Body.LAST_MODIFIED);
		checksum = body.getBinary(Constants.Body.CHECKSUM);
		isDirectory = body.getBoolean(Constants.Body.IS_DIRECTORY);
	}

	/**
	 * Clone constructor.
	 * 
	 * @param file
	 */
	public FileBean(FileBean file) {
		this.filename = file.filename;
		this.lastModified = file.lastModified;
		this.checksum = file.checksum;
		this.isDirectory = file.isDirectory;
	}

	public String getFilename() {
		return filename;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	byte[] getChecksum() {
		return checksum;
	}

	boolean isDirectory() {
		return isDirectory;
	}

	public Path getLocalizedFile() {
		return Paths.get(Constants.FOLDER, filename);
	}

	public JsonObject getJsonObject() {
		JsonObject json = new JsonObject();

		json.putString(Constants.Body.FILENAME, filename);
		json.putNumber(Constants.Body.LAST_MODIFIED, lastModified);
		json.putBinary(Constants.Body.CHECKSUM, checksum);
		json.putBoolean(Constants.Body.IS_DIRECTORY, isDirectory);

		return json;
	}

	boolean hasSameContents(Path other) {
		boolean hasSame = false;
		byte[] otherChecksum = Util.getChecksum(other.toString());
		hasSame = Arrays.equals(checksum, otherChecksum);
		return hasSame;
	}

	/**
	 * Compares the last modified times of the received file with an existing
	 * file.
	 * 
	 * @param other
	 *            A localized Path to the other file.
	 * @return 0 if this file is modified at the same time as other, a value
	 *         less than 0 if this file is older than other, and a value greater
	 *         than 0 if this file is newer than other.
	 */
	int compareLastModifiedTime(Path other) {
		int comparison = -1;
		try {

			// System.out.println(Files.getLastModifiedTime(other) + " vs " +
			// FileTime.fromMillis(lastModified));

			long difference = Files.getLastModifiedTime(other).toMillis() - lastModified;

			if (difference > 0)
				return 1;

			if (difference < 0)
				return -1;

			return 0;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return comparison;
	}

	@Override
	public int hashCode() {
		return filename.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return filename.equals(((FileBean)o).filename);
	}

	@Override
	public int compareTo(FileBean o) {
		int comparison = 0;
		if (this.getLastModified() > o.getLastModified()) {
			comparison = 1;
		} else if (this.getLastModified() < o.getLastModified()) {
			comparison = -1;
		}
		return comparison;
	}

	public FileRecord toFileRecord() {
		return new FileRecord(this.filename, this.lastModified);
	}

}
