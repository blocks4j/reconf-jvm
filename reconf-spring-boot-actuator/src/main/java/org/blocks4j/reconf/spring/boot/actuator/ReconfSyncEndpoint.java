package org.blocks4j.reconf.spring.boot.actuator;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "endpoints.reconf.sync", ignoreUnknownFields = true)
public class ReconfSyncEndpoint extends AbstractEndpoint<String> {

    private static final String RESULT_TEMPLATE = "{ \"repository\" : \"%s\", \"success\" : \"%s\" }";

    public static final String RECONF_SYNC_ENDPOINT_ID = "reconf_sync";

    public ReconfSyncEndpoint() {
        super(RECONF_SYNC_ENDPOINT_ID);
    }

    @Override
    public String invoke() {
        /*List<SyncResult> result = Environment.syncActiveConfigurationRepositoryUpdaters();

        List<String> syncResultMessage = new ArrayList<>();
        for (SyncResult syncResult : result) {
            if (syncResult == null) {
                continue;
            }
            syncResultMessage.add(String.format(RESULT_TEMPLATE, syncResult.getName(), syncResult.getThrowable() == null));
        }

        return StringUtils.join(syncResultMessage, ", ");*/
        return "";
    }

}
