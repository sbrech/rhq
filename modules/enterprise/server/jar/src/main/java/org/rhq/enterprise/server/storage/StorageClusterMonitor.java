package org.rhq.enterprise.server.storage;

import java.net.InetAddress;

import com.datastax.driver.core.exceptions.NoHostAvailableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.rhq.enterprise.server.cloud.StorageNodeManagerLocal;
import org.rhq.enterprise.server.util.LookupUtil;
import org.rhq.server.metrics.StorageStateListener;

/**
 * @author John Sanda
 */
public class StorageClusterMonitor implements StorageStateListener {

    private Log log = LogFactory.getLog(StorageClusterMonitor.class);

    private boolean isClusterAvailable = true;

    public boolean isClusterAvailable() {
        return isClusterAvailable;
    }

    @Override
    public void onStorageNodeUp(InetAddress address) {
        log.info("Storage node at " + address.getHostAddress() + " is up");

        isClusterAvailable = true;

        StorageNodeManagerLocal storageNodeManager = LookupUtil.getStorageNodeManager();
        if (storageNodeManager.isAddNodeMaintenanceInProgress()) {
            log.info("Scheduling cluster maintenance...");
            StorageNodeOperationsHandlerLocal storageOperationsHandler = LookupUtil.getStorageNodeOperationsHandler();
            storageOperationsHandler.performAddNodeMaintenance(address);
        }
    }

    @Override
    public void onStorageNodeDown(InetAddress address) {
        log.info("Storage node at " + address.getHostAddress() + " is down");
    }

    @Override
    public void onStorageNodeRemoved(InetAddress address) {
        log.info("Storage node at " + address.getHostAddress() + " has been removed from the cluster");
    }

    @Override
    public void onStorageClusterDown(NoHostAvailableException e) {
        isClusterAvailable = false;
    }
}
