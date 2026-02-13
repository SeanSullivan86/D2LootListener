package org.sully.d2.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.sully.d2.DataSnapshot;
import org.sully.d2.SingleConsumerDataWithItems;
import org.sully.d2.itemtracking.DropContextEnum;


@Controller
public class WebResponsesController {

    private final SnapshotAccessor snapshotAccessor;

    public WebResponsesController(SnapshotAccessor snapshotAccessor) {
        this.snapshotAccessor = snapshotAccessor;
    }

    @RequestMapping("/summary")
    @ResponseBody
    public WebResponse getSummary() {
        DataSnapshot snapshot = snapshotAccessor.getSnapshot();

        SummaryInfo summary = snapshot.toSummaryObject();

        return WebResponse.builder()
                .snapshotId(snapshot.getId())
                .response(summary)
                .build();
    }

    @RequestMapping("/dropContexts/{dropContextName}/consumers/{consumerId}")
    @ResponseBody
    public WebResponse getDataForConsumer(@PathVariable String dropContextName, @PathVariable String consumerId) {
        DataSnapshot snapshot = snapshotAccessor.getSnapshot();

        SingleConsumerDataWithItems data = snapshot.getSingleConsumerData(DropContextEnum.valueOf(dropContextName), consumerId);

        if (data == null) {
            throw new RuntimeException("No data found for " + dropContextName + " : " + consumerId);
        }

        return WebResponse.builder()
                .snapshotId(snapshot.getId())
                .response(data)
                .build();
    }

}

