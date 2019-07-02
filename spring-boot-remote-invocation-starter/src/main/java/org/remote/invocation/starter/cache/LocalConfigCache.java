package org.remote.invocation.starter.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LocalConfigCache {
    private static class LazyHolder {
        private static final LocalConfigCache INSTANCE = new LocalConfigCache();
    }

    private LocalConfigCache() {

    }

    public static final LocalConfigCache getInstance() {
        return LocalConfigCache.LazyHolder.INSTANCE;
    }


    int leaderPort;
    String netSyncIp;
    String netIp;
    String localIp;
}
