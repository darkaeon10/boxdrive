package job;

import conn.Connection;

public abstract class Job implements Comparable<Job> {
	private Connection connection;
	private final long createTime = System.currentTimeMillis();
	private boolean toSend = true;
	
	Job(Connection connection) {
		this.connection = connection;
	}
	
	
	public String execute(JobManager jobManager) {
		if (toSend) {
			connection.write(getJson());
			return null;
		} else {
			return executeLocal(jobManager);
		}
	}
	
	public abstract String executeLocal(JobManager jobManager);
	public abstract String getJson();
	
	public long getCreateTime() {
		return createTime;
	}
	
	public void setForReceiving() {
		toSend = false;
	}
	
	public boolean isToSend() {
		return toSend;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public int compareTo(Job other) {
		int comparison = 0;
		if (createTime > other.createTime) {
			comparison = 1;
		} else if (createTime < other.createTime) {
			comparison = -1;
		}
		return comparison;
	}
}
