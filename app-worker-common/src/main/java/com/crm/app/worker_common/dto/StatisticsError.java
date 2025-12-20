package com.crm.app.worker_common.dto;

import com.crmmacher.error.ErrMsg;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatisticsError {
    long nEntries = 0;
    long nErrors = 0;

    public void setFromErrMsg(List<ErrMsg> errMsg) {
        nEntries = errMsg.size();
        nErrors = errMsg.stream()
                .filter(err -> !err.isWarning())
                .count();
    }
}
