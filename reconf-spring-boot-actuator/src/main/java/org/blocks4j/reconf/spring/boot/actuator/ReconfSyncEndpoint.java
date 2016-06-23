package org.blocks4j.reconf.spring.boot.actuator;

import org.apache.commons.lang3.StringUtils;
import org.blocks4j.reconf.client.factory.ConfigurationRepositoryFactory;
import org.blocks4j.reconf.spring.boot.actuator.domain.SyncResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "endpoints.reconf.sync", ignoreUnknownFields = true)
public class ReconfSyncEndpoint extends AbstractEndpoint<String> {

    private static final String RESULT_TEMPLATE = "{ \"repository\" : \"%s\", \"success\" : \"%s\" }";

    public static final String RECONF_SYNC_ENDPOINT_ID = "reconf_sync";

    @Autowired
    private List<ConfigurationRepositoryFactory> repositoryFactories;

    public ReconfSyncEndpoint() {
        super(RECONF_SYNC_ENDPOINT_ID);
    }

    @Override
    public String invoke() {
        List<SyncResult> result = this.repositoryFactories.stream()
                                                          .flatMap(configurationRepositoryFactory -> configurationRepositoryFactory.getUpdatersCreated().stream())
                                                          .parallel()
                                                          .map(updater -> {
                                                              SyncResult syncResult;
                                                              String repositoryName = updater.getRespositoryClass().getCanonicalName();
                                                              try {
                                                                  updater.syncNow();
                                                                  syncResult = new SyncResult(repositoryName);
                                                              } catch (Exception e) {
                                                                  syncResult = new SyncResult(repositoryName, e);
                                                              }
                                                              return syncResult;
                                                          })
                                                          .collect(Collectors.toList());

        List<String> syncResultMessage = new ArrayList<>();
        for (SyncResult syncResult : result) {
            if (syncResult == null) {
                continue;
            }
            syncResultMessage.add(String.format(RESULT_TEMPLATE, syncResult.getName(), syncResult.getThrowable() == null));
        }

        return StringUtils.join(syncResultMessage, ", ");
    }

}
