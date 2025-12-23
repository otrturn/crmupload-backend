package com.crm.app.e2e.e2e_page_visited;

import com.crm.app.e2e.E2eAbstract;
import com.crm.app.e2e.client.PageVisitsClient;
import com.crm.app.e2e.client.PageVisitsResult;
import com.crm.app.e2e.config.E2eProperties;
import com.crm.app.e2e.config.E2eTestConfig;
import com.crm.app.e2e.database.CustomerHandling;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("e2e")
@Import(E2eTestConfig.class)
class TestE2ePageVisits extends E2eAbstract {

    @Autowired
    private E2eProperties e2eProperties;

    @Test
    void registerCustomer_pageVisited() {

        PageVisitsClient pageVisitsClient = new PageVisitsClient(e2eProperties);
        PageVisitsResult pageVisitsResult;

        /*
        Page visited
         */
        pageVisitsResult = pageVisitsClient.pageVisited("malaysia");
        assertThat(pageVisitsResult).isInstanceOf(PageVisitsResult.Success.class);
        String pageId = CustomerHandling.getPageVisits(dataSource);
        assertEquals("malaysia", pageId);
    }
}