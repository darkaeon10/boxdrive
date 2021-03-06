package serverjobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import job.BasicJob;
import job.DeleteJob;
import job.Job;
import job.JobManager;
import server_manager.FileDirectory;
import conn.Connection;
import conn.ConnectionManager;

public class CoordinatorJobManager extends JobManager {
	private FileDirectory fileDirectory;
	private Map<Connection, List<Job>> lastProcessedJobs;
	private static int HISTORY_SIZE = 10;

	public CoordinatorJobManager(ConnectionManager connMgrClients, ConnectionManager connMgrStorageServers) {
		super(connMgrClients, connMgrStorageServers);

		fileDirectory = new FileDirectory();
		lastProcessedJobs = new HashMap<>();
	}

	public void updateFileDirectory() {
		Connection conn = connMgrStorageServers.getLastConnection();
		if (conn != null && fileDirectory.addServer(conn))
			lastProcessedJobs.put(conn, new ArrayList<Job>());
	}

	public String forwardJob(Job job) {
		return null;
	}

	@Override
	protected synchronized void processMessages() {
		while (jobQueue.size() > 0) {
			Job job = this.dequeue(0);
			job.getConnection();

			if (connMgrClients.hasConnection(job.getConnection())) {
				// the job came from clients

				if (job instanceof BasicJob) {
					BasicJob basicJob = (BasicJob)job;
					
					// get serverlist from fileDirectory
					List<Connection> connList = fileDirectory.getServerListForFile(basicJob.file);
					if(job instanceof DeleteJob)
						fileDirectory.removeFileFromList(basicJob.file);
					
					// send to storage server group
					for (Connection conn : connList)
						conn.write(job.getJson());
				} else {
					// this is list job. don't let it reach the storage servers.
					// execute here
					job.execute(this);
				}
			} else {
				// the job came from storage servers

				if (isBroadcastJob(job)) {
					connMgrClients.broadcast(job.getJson());
				} else {
					addJobToHistory(job);
					// TODO: send to specific client

					// temporarily sends to all clients
					connMgrClients.broadcast(job.getJson());
				}
			}
		}
	}

	private boolean isBroadcastJob(Job job) {
		List<Job> list = lastProcessedJobs.get(job.getConnection());
		return list.contains(job);
	}

	private void removeJobFromHistory(Job job) {
		List<Job> list = lastProcessedJobs.get(job.getConnection());
		list.remove(job);
	}

	private void addJobToHistory(Job job) {
		List<Job> list = lastProcessedJobs.get(job.getConnection());
		list.add(job);
		while (list.size() > this.HISTORY_SIZE)
			list.remove(0);
	}

	public FileDirectory getFileDirectory() {
		return fileDirectory;
	}
}
