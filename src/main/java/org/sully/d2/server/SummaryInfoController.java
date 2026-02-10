package org.sully.d2.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class SummaryInfoController {

    private final SnapshotAccessor snapshotAccessor;

    public SummaryInfoController(SnapshotAccessor snapshotAccessor) {
        this.snapshotAccessor = snapshotAccessor;
    }

    @RequestMapping("/summary")
    @ResponseBody
    public SummaryInfo getSummary() {
        return snapshotAccessor.getSnapshot().toSummaryObject();
    }

}

@Controller
class CategorizedTopNController {
    private final SnapshotAccessor snapshotAccessor;

    public CategorizedTopNController(SnapshotAccessor snapshotAccessor) {
        this.snapshotAccessor = snapshotAccessor;
    }

    @RequestMapping("/categorizedTopN/{consumerName}/{categoryName}")
    @ResponseBody
    public String getDataForCategory(@PathVariable String consumerName, @PathVariable String categoryName) {
        return null; // todo
    }
}